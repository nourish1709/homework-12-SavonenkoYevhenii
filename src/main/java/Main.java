import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    public static final String REPLACE_TO = "****";
    public static final String REPLACE_FROM = "\\b[a-zA-Z]{1,3}\\b";
    static HashMap<String, Integer> mapWords = new HashMap<>();
    static List<String> text = new ArrayList<>();
    static List<String> obsceneWords = new ArrayList<>();
    static List<String> removedWords = new ArrayList<>();

    public static void main(String[] args) {
        getWords();
        getObsceneWords();
        replaceWordsInFile();
        writeRemovedWords();
        getInfo();

        Scanner scanner = new Scanner(System.in);
        System.out.print("\tHow much popular words do you want to see?\t");
        int numOfWords = scanner.nextInt();
        getPopularWords(numOfWords);
    }

    public static void replaceWordsInFile() {
        Path path = Paths.get("src/main/resources/test.txt");
        Charset charset = StandardCharsets.UTF_8;
        try {
            String content = new String(Files.readAllBytes(path), charset);

            for (String word : obsceneWords) {
                content = content.replaceAll(word, REPLACE_TO);
            }

            Pattern pattern = Pattern.compile(REPLACE_FROM);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                removedWords.add(matcher.group(0));
            }

            content = content.replaceAll(REPLACE_FROM, REPLACE_TO);

            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            System.out.println("[...Sorry, but I couldn't find the file...]");
        }
    }

    public static void writeRemovedWords() {
        for (String word : removedWords) {
            try {
                Files.write(Paths.get("src/main/resources/removedWords.txt"), word.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("[...Sorry, but I couldn't find the file...]");
            }
        }
    }

    public static void getObsceneWords() {
        try (Scanner scanner = new Scanner(new File("src/main/resources/obsceneWords.txt"))) {
            while (scanner.hasNext()) {
                String nextWord = scanner.nextLine();
                obsceneWords.add(nextWord);
                removedWords.add(nextWord);
            }
        } catch (FileNotFoundException e) {
            System.out.println("[...Sorry, but I couldn't find the file...]");
        }
    }

    public static void getWords() {
        try (Scanner scanner = new Scanner(new File("src/main/resources/test.txt"))) {
            while (scanner.hasNextLine()) {
                text.add(scanner.nextLine());
                getWords(text.get(text.size() - 1));
            }
        } catch (FileNotFoundException e) {
            System.out.println("[...Sorry, but I couldn't find the file...]");
        }
    }

    public static void getWords(String text) {
        String[] words = text.split("[^\\p{L}\\p{Nd}]+");

        for (String word : words) {
            if (mapWords.containsKey(word)) {
                mapWords.put(word, mapWords.get(word) + 1);
            } else {
                mapWords.put(word, 1);
            }
        }
        mapWords.remove("");
    }

    public static void getInfo() {
        System.out.println("\n\tThe total amount of words in the file: " + getTotalAmount());
        System.out.println("\tThe total amount of removed words in the file: " + removedWords.size());
    }

    public static int getTotalAmount() {
        int sum = 0;
        for (int i : mapWords.values()) {
            sum += i;
        }
        return sum;
    }

    public static void getPopularWords(int n) {
        List<String> popularWords = mapWords.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("\t" + n + " popular popular words in the file: " + ":\n\t" + popularWords);
    }
}
