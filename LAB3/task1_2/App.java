package task1_2;

import java.util.Random;

public class App {
    public static void main(String[] args) {
        System.out.println("First Matrix: ");
        int[][] matrix1 = generateRandomMatrix(5);
        showMatrix(matrix1);

        System.out.println("\nSecond Matrix: ");
        int[][] matrix2 = generateRandomMatrix(5);
        showMatrix(matrix2);

        int numberOfThreads = 4;

        Fox.Result foxResult = Fox.foxAlgorithmParallel(matrix1, matrix2, numberOfThreads);
        int[][] resultMatrix = foxResult.getResultMatrix();

        // Striped.Result stripedResult = Striped.multiplyMatrices(matrix1, matrix2, numberOfThreads);
        // int[][] resultMatrix = stripedResult.getResultMatrix();

        System.out.println("\nResult Matrix: ");
        showMatrix(resultMatrix);
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

    public static void showMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
}
