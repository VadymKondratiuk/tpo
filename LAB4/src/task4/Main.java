package task4;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        List<String> keywords = Arrays.asList("computer", "search", "technology", "information", "data");

        String directoryPath = "textfiles";
        List<String> files = getFilesFromDirectory(directoryPath);

        ForkJoinPool pool = new ForkJoinPool();

        List<SearchTask> tasks = new ArrayList<>();
        for (String file : files) {
            tasks.add(new SearchTask(file, keywords));
        }

        List<Map<String, Integer>> results = tasks.stream()
            .map(pool::invoke)  
            .collect(Collectors.toList()); 

        for (int i = 0; i < files.size(); i++) {
            System.out.print(files.get(i) + ": ");
            System.out.println(results.get(i));
        }

        pool.shutdown();  
    }

    private static List<String> getFilesFromDirectory(String directoryPath) throws IOException {
        try (var paths = Files.list(Paths.get(directoryPath))) {
            return paths
                .filter(Files::isRegularFile) 
                .map(Path::toString)
                .collect(Collectors.toList());
        }
    }
}

