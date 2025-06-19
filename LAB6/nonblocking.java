/*
javac -cp $MPJ_HOME/lib/mpj.jar nonblocking.java
mpjrun.sh -np 4 nonblocking
*/

import mpi.*;

public class nonblocking {

    static int SIZE = 100;

    static final int MASTER = 0;
    static final int FROM_MASTER = 1;
    static final int FROM_WORKER = 2;

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int taskid = MPI.COMM_WORLD.Rank();
        int numtasks = MPI.COMM_WORLD.Size();

        if (numtasks < 2) {
            if (taskid == MASTER) {
                System.out.println("At least two processes are required.");
            }
            MPI.Finalize();
            return;
        }

        int numworkers = numtasks - 1;

        double[][] a = new double[SIZE][SIZE];
        double[][] b = new double[SIZE][SIZE];
        double[][] c = new double[SIZE][SIZE];

        if (taskid == MASTER) {
            fillMatrixRandom(a, 1, 100);
            fillMatrixRandom(b, 1, 100);

            long startTime = System.currentTimeMillis();

            int averow = SIZE / numworkers;
            int extra = SIZE % numworkers;
            int offset = 0;

            Request[] sendRequests = new Request[numworkers * 4];

            for (int dest = 1; dest <= numworkers; dest++) {
                int rows = (dest <= extra) ? averow + 1 : averow;

                double[] aFlat = flattenMatrix(a, offset, rows, SIZE);
                double[] bFlat = flattenMatrix(b, 0, SIZE, SIZE);

                sendRequests[(dest - 1) * 4] = MPI.COMM_WORLD.Isend(new int[]{offset}, 0, 1, MPI.INT, dest, FROM_MASTER);
                sendRequests[(dest - 1) * 4 + 1] = MPI.COMM_WORLD.Isend(new int[]{rows}, 0, 1, MPI.INT, dest, FROM_MASTER);
                sendRequests[(dest - 1) * 4 + 2] = MPI.COMM_WORLD.Isend(aFlat, 0, rows * SIZE, MPI.DOUBLE, dest, FROM_MASTER);
                sendRequests[(dest - 1) * 4 + 3] = MPI.COMM_WORLD.Isend(bFlat, 0, SIZE * SIZE, MPI.DOUBLE, dest, FROM_MASTER);

                offset += rows;
            }

            for (Request r : sendRequests) {
                if (r != null) r.Wait();
            }

            for (int source = 1; source <= numworkers; source++) {
                int[] offsetArr = new int[1];
                int[] rowsArr = new int[1];
                Request r1 = MPI.COMM_WORLD.Irecv(offsetArr, 0, 1, MPI.INT, source, FROM_WORKER);
                Request r2 = MPI.COMM_WORLD.Irecv(rowsArr, 0, 1, MPI.INT, source, FROM_WORKER);
                r1.Wait();
                r2.Wait();

                int offsetRecv = offsetArr[0];
                int rowsRecv = rowsArr[0];
                double[] cFlat = new double[rowsRecv * SIZE];
                Request r3 = MPI.COMM_WORLD.Irecv(cFlat, 0, rowsRecv * SIZE, MPI.DOUBLE, source, FROM_WORKER);
                r3.Wait();

                unflattenMatrix(cFlat, c, offsetRecv, rowsRecv, SIZE);
            }

            long endTime = System.currentTimeMillis();

            if(SIZE <= 50) {
                System.out.println("Result matrix C:");
                for (int i = 0; i < SIZE; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        System.out.printf("%6.2f ", c[i][j]);
                    }
                    System.out.println();
                }
            }
            System.out.println("Matrix Size: " + SIZE + "x" + SIZE + ", Workers: " + numworkers);
            System.out.println("Execution time: " + (endTime - startTime) + " ms");

        } else {
            int[] offsetArr = new int[1];
            int[] rowsArr = new int[1];

            Request r1 = MPI.COMM_WORLD.Irecv(offsetArr, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            Request r2 = MPI.COMM_WORLD.Irecv(rowsArr, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            r1.Wait();
            r2.Wait();

            int offset = offsetArr[0];
            int rows = rowsArr[0];

            double[] aFlat = new double[rows * SIZE];
            double[] bFlat = new double[SIZE * SIZE];

            Request r3 = MPI.COMM_WORLD.Irecv(aFlat, 0, rows * SIZE, MPI.DOUBLE, MASTER, FROM_MASTER);
            Request r4 = MPI.COMM_WORLD.Irecv(bFlat, 0, SIZE * SIZE, MPI.DOUBLE, MASTER, FROM_MASTER);
            r3.Wait();
            r4.Wait();

            double[][] aPart = new double[rows][SIZE];
            double[][] bMatrix = new double[SIZE][SIZE];
            double[][] cPart = new double[rows][SIZE];

            unflattenMatrix(aFlat, aPart, 0, rows, SIZE);
            unflattenMatrix(bFlat, bMatrix, 0, SIZE, SIZE);

            for (int k = 0; k < SIZE; k++) {
                for (int i = 0; i < rows; i++) {
                    cPart[i][k] = 0.0;
                    for (int j = 0; j < SIZE; j++) {
                        cPart[i][k] += aPart[i][j] * bMatrix[j][k];
                    }
                }
            }

            Request s1 = MPI.COMM_WORLD.Isend(new int[]{offset}, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            Request s2 = MPI.COMM_WORLD.Isend(new int[]{rows}, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            Request s3 = MPI.COMM_WORLD.Isend(flattenMatrix(cPart, 0, rows, SIZE), 0, rows * SIZE, MPI.DOUBLE, MASTER, FROM_WORKER);
            s1.Wait();
            s2.Wait();
            s3.Wait();
        }

        MPI.Finalize();
    }

    private static double[] flattenMatrix(double[][] matrix, int startRow, int numRows, int numCols) {
        double[] flat = new double[numRows * numCols];
        int index = 0;
        for (int i = startRow; i < startRow + numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                flat[index++] = matrix[i][j];
            }
        }
        return flat;
    }

    private static void unflattenMatrix(double[] flat, double[][] matrix, int startRow, int numRows, int numCols) {
        int index = 0;
        for (int i = startRow; i < startRow + numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                matrix[i][j] = flat[index++];
            }
        }
    }

    private static void fillMatrixRandom(double[][] matrix, int min, int max) {
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = min + (max - min) * random.nextDouble();
            }
        }
    }
}
