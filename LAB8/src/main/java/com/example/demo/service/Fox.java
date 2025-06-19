package com.example.demo.service;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class Fox {
    public static class Result {
        private final int[][] resultMatrix;

        public Result(int[][] resultMatrix) {
            this.resultMatrix = resultMatrix;
        }

        public int[][] getResultMatrix() {
            return resultMatrix;
        }
    }

    public static Result foxAlgorithmParallel(int[][] matrix1, int[][] matrix2, int numberOfThreads) {
        int m = matrix1.length;

        int blocksize = (int) Math.sqrt(m);
        while (m % blocksize != 0) {
            blocksize--;
        }

        int submatrixesCount = m / blocksize;
        int[][] resultMatrix = new int[m][m];

        ForkJoinPool pool = new ForkJoinPool(numberOfThreads);
        pool.invoke(new FoxMainTask(matrix1, matrix2, resultMatrix, blocksize, submatrixesCount));
        pool.shutdown();

        return new Result(resultMatrix);
    }

    private static class FoxMainTask extends RecursiveAction {
        private final int[][] matrix1;
        private final int[][] matrix2;
        private final int[][] result;
        private final int blocksize;
        private final int submatrixesCount;

        public FoxMainTask(int[][] matrix1, int[][] matrix2, int[][] result, int blocksize, int submatrixesCount) {
            this.matrix1 = matrix1;
            this.matrix2 = matrix2;
            this.result = result;
            this.blocksize = blocksize;
            this.submatrixesCount = submatrixesCount;
        }

        @Override
        protected void compute() {
            ForkJoinTask.invokeAll(createSubtasks());
        }

        private FoxTask[] createSubtasks() {
            int totalTasks = submatrixesCount * submatrixesCount * submatrixesCount;
            FoxTask[] tasks = new FoxTask[totalTasks];
            int index = 0;
            for (int k = 0; k < submatrixesCount; k++) {
                for (int i = 0; i < submatrixesCount; i++) {
                    for (int j = 0; j < submatrixesCount; j++) {
                        tasks[index++] = new FoxTask(matrix1, matrix2, result, k, i, j, blocksize);
                    }
                }
            }
            return tasks;
        }
    }

    private static class FoxTask extends RecursiveAction {
        private final int[][] matrix1;
        private final int[][] matrix2;
        private final int[][] result;
        private final int k;
        private final int i;
        private final int j;
        private final int blocksize;

        public FoxTask(int[][] matrix1, int[][] matrix2, int[][] result, int k, int i, int j, int blocksize) {
            this.matrix1 = matrix1;
            this.matrix2 = matrix2;
            this.result = result;
            this.k = k;
            this.i = i;
            this.j = j;
            this.blocksize = blocksize;
        }

        @Override
        protected void compute() {
            int startI = i * blocksize;
            int startJ = j * blocksize;
            int startK = k * blocksize;

            int[][] temp = new int[blocksize][blocksize];

            for (int ii = 0; ii < blocksize; ii++) {
                for (int jj = 0; jj < blocksize; jj++) {
                    temp[ii][jj] = 0;
                    for (int kk = 0; kk < blocksize; kk++) {
                        temp[ii][jj] += matrix1[startI + ii][startK + kk] * matrix2[startK + kk][startJ + jj];
                    }
                }
            }

            synchronized (result) {
                for (int ii = 0; ii < blocksize; ii++) {
                    for (int jj = 0; jj < blocksize; jj++) {
                        result[startI + ii][startJ + jj] += temp[ii][jj];
                    }
                }
            }
        }
    }
}
