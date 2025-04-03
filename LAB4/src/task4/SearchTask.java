package task4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class SearchTask extends RecursiveTask<Map<String, Integer>> {
    private final String fileName;
    private final List<String> keywords;

    public SearchTask(String fileName, List<String> keywords) {
        this.fileName = fileName;
        this.keywords = keywords;
    }

    @Override
    protected Map<String, Integer> compute() {
        Map<String, Integer> keywordCounts = new HashMap<>();
        for (String keyword : keywords) {
            keywordCounts.put(keyword, 0);
        }

        try {
            String text = Files.readString(Path.of(fileName));

            for (String keyword : keywords) {
                int count = countKeywordOccurrences(text, keyword);
                keywordCounts.put(keyword, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keywordCounts;
    }

    private int countKeywordOccurrences(String text, String keyword) {
        int count = 0;
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int index = 0;

        while ((index = lowerText.indexOf(lowerKeyword, index)) != -1) {
            count++;
            index += lowerKeyword.length();
        }
        return count;
    }
}
