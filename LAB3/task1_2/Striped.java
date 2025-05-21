package task1_2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Striped {
    public static class Result {
        private final int[][] resultMatrix;

        public Result(int rows, int cols) {
            this.resultMatrix = new int[rows][cols];
        }

        public int[][] getResultMatrix() {
            return resultMatrix;
        }

        public void setRow(int row, int[] rowData) {
            System.arraycopy(rowData, 0, resultMatrix[row], 0, rowData.length);
        }
    }

    public static Result multiplyMatrices(int[][] matrix1, int[][] matrix2, int numberOfThreads) {
        int m = matrix1.length;  
        int n = matrix1[0].length; 
        int p = matrix2[0].length; 
        Result result = new Result(m, p);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < m; i++) {
            executor.execute(new MultiplicationTask(matrix1, matrix2, result, i, n, p));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static class MultiplicationTask implements Runnable {
        private final int[][] matrix1;
        private final int[][] matrix2;
        private final Result result;
        private final int row;
        private final int n;
        private final int p;

        public MultiplicationTask(int[][] matrix1, int[][] matrix2, Result result, int row, int n, int p) {
            this.matrix1 = matrix1;
            this.matrix2 = matrix2;
            this.result = result;
            this.row = row;
            this.n = n;
            this.p = p;
        }

        @Override
        public void run() {
            int[] rowResult = new int[p];

            for (int j = 0; j < p; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += matrix1[row][k] * matrix2[k][j];
                }
                rowResult[j] = sum;
            }

            result.setRow(row, rowResult);
        }
    }
}
