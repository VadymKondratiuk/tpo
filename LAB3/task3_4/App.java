package task3_4;

import java.util.Random;
import task1_2.Fox;
import task1_2.Striped;

public class App {
    public static void main(String[] args) {
        int[] matrixSizes = {100, 500, 1000, 2000, 4000};
        int[] threadCounts = {2, 4, 6, 8};

        for (int size : matrixSizes) {
            System.out.println("Testing matrix size: " + size);
            int[][] matrixA = generateRandomMatrix(size);
            int[][] matrixB = generateRandomMatrix(size);

            for (int threads : threadCounts) {
                System.out.println("Testing with " + threads + " threads...");

                long startTime = System.nanoTime();
                Fox.foxAlgorithmParallel(matrixA, matrixB, threads);
                long foxTime = System.nanoTime() - startTime;

                startTime = System.nanoTime();
                Striped.multiplyMatrices(matrixA, matrixB, threads);
                long stripedTime = System.nanoTime() - startTime;

                System.out.println("Fox algorithm time: " + foxTime + " ns");
                System.out.println("Striped algorithm time: " + stripedTime + " ns");

                if (foxTime < stripedTime) {
                    System.out.println("Fox algorithm is more efficient than Striped algorithm with " + threads + " threads");
                } else if (foxTime > stripedTime) {
                    System.out.println("Striped algorithm is more efficient than Fox algorithm with " + threads + " threads");
                } else {
                    System.out.println("Both algorithms have the same efficiency with " + threads + " threads");
                }
                System.out.println();
            }
            System.out.println("---------------------------------------------------");
        }
    }

    public static int[][] generateRandomMatrix(int size) {
        int[][] matrix = new int[size][size];
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(2000) - 1000; 
            }
        }

        return matrix;
    }
}
