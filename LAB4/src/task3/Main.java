package task3;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        String directoryPath = "textfiles"; 
        List<String> files = getFilesFromDirectory(directoryPath);

        List<Set<String>> results = new ArrayList<>();
        try (ForkJoinPool pool = new ForkJoinPool()) {
            for (String file : files) {
                results.add(pool.invoke(new WordTask(file)));
            }
        }

        Set<String> commonWords = findCommonWords(results);

        System.out.println("Common words: " + commonWords);
        System.out.println("Number of common words: " + commonWords.size());
    }

    private static List<String> getFilesFromDirectory(String directoryPath) throws IOException {
        try (var paths = Files.list(Paths.get(directoryPath))) {
            return paths
                .filter(Files::isRegularFile) 
                .map(Path::toString)
                .collect(Collectors.toList());
        }
    }

    private static Set<String> findCommonWords(List<Set<String>> wordSets) {
        Set<String> commonWords = new HashSet<>(wordSets.get(0));

        for (Set<String> wordSet : wordSets) {
            commonWords.retainAll(wordSet);  
        }

        return commonWords;
    }
}
