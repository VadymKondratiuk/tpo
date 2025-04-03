package task1;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class TextAnalysis {

    public static void main(String[] args) throws IOException {
        String filePath = "textfiles/file2.txt"; 
        try (ForkJoinPool pool = new ForkJoinPool()) {
            List<Integer> wordLengths = pool.invoke(new WordLengthTask(filePath));

            int count = wordLengths.size();
            double average = wordLengths.stream().mapToInt(Integer::intValue).average().orElse(0);
            double median = calculateMedian(wordLengths);
            double variance = calculateVariance(wordLengths, average);
            double stdDeviation = Math.sqrt(variance);
            
            System.out.println("Word count: " + count);
            System.out.println("Average word length: " + average);
            System.out.println("Median word length: " + median);
            System.out.println("Standard deviation: " + stdDeviation);

            Map<Integer, Long> frequencyMap = getWordLengthFrequency(wordLengths);
            System.out.println("\nWord length frequency histogram:");
            frequencyMap.forEach((length, frequency) -> {
                System.out.println("Length " + length + ": " + frequency);
            });
        }
    }

    private static double calculateMedian(List<Integer> values) {
        Collections.sort(values);
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }

    private static double calculateVariance(List<Integer> values, double mean) {
        return values.stream().mapToDouble(length -> Math.pow(length - mean, 2)).sum() / values.size();
    }

    private static Map<Integer, Long> getWordLengthFrequency(List<Integer> wordLengths) {
        Map<Integer, Long> frequencyMap = new HashMap<>();
        for (int length : wordLengths) {
            frequencyMap.put(length, frequencyMap.getOrDefault(length, 0L) + 1);
        }
        return frequencyMap;
    }
}
