package task3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class WordTask extends RecursiveTask<Set<String>> {
    private final String fileName;

    public WordTask(String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected Set<String> compute() {
        try {
            String text = Files.readString(Path.of(fileName));

            Set<String> words = new HashSet<>(Arrays.asList(text.toLowerCase().replaceAll("[^a-zA-Zа-яА-Я'-]", " ").split("\\s+")));
            words.remove(""); 
            return words;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
}
