/*
javac -cp $MPJ_HOME/lib/mpj.jar blocking.java
mpjrun.sh -np 4 blocking
*/

import mpi.*;

public class blocking {

    static int SIZE = 4000; 

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

            for (int dest = 1; dest <= numworkers; dest++) {
                int rows = (dest <= extra) ? averow + 1 : averow;

                MPI.COMM_WORLD.Send(new int[]{offset}, 0, 1, MPI.INT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(new int[]{rows}, 0, 1, MPI.INT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(flattenMatrix(a, offset, rows, SIZE), 0, rows * SIZE, MPI.DOUBLE, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(flattenMatrix(b, 0, SIZE, SIZE), 0, SIZE * SIZE, MPI.DOUBLE, dest, FROM_MASTER);

                offset += rows;
            }

            for (int source = 1; source <= numworkers; source++) {
                int[] offsetArr = new int[1];
                int[] rowsArr = new int[1];
                MPI.COMM_WORLD.Recv(offsetArr, 0, 1, MPI.INT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(rowsArr, 0, 1, MPI.INT, source, FROM_WORKER);

                int offsetRecv = offsetArr[0];
                int rowsRecv = rowsArr[0];
                double[] cFlat = new double[rowsRecv * SIZE];
                MPI.COMM_WORLD.Recv(cFlat, 0, rowsRecv * SIZE, MPI.DOUBLE, source, FROM_WORKER);

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

            MPI.COMM_WORLD.Recv(offsetArr, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(rowsArr, 0, 1, MPI.INT, MASTER, FROM_MASTER);

            int offset = offsetArr[0];
            int rows = rowsArr[0];

            double[] aFlat = new double[rows * SIZE];
            double[] bFlat = new double[SIZE * SIZE];

            MPI.COMM_WORLD.Recv(aFlat, 0, rows * SIZE, MPI.DOUBLE, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(bFlat, 0, SIZE * SIZE, MPI.DOUBLE, MASTER, FROM_MASTER);

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

            MPI.COMM_WORLD.Send(new int[]{offset}, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(new int[]{rows}, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(flattenMatrix(cPart, 0, rows, SIZE), 0, rows * SIZE, MPI.DOUBLE, MASTER, FROM_WORKER);
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
