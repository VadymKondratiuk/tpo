package task1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class WordLengthTask extends RecursiveTask<List<Integer>> {
    private final String filePath;

    WordLengthTask(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected List<Integer> compute() {
        try {
            String text = Files.readString(Path.of(filePath));
            String[] words = text.toLowerCase().replaceAll("[^a-zA-Zа-яА-Я]", " ").split("\\s+");
            List<Integer> lengths = new ArrayList<>();
            for (String word : words) {
                if (!word.isEmpty()) {
                    lengths.add(word.length());
                }
            }
            return lengths;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
