import java.util.HashMap;
import java.util.Random;

public class Benchmark {
    static final int NUM_ELEMENTS = 30_000;
    public static void main(String[] args) {
        // Number of elements to test
        String[] testData = generateTestData(NUM_ELEMENTS);

        // Benchmark MDictionary
        MDictionary mDictionary = new MDictionary();
        long startTimeMDictionary = System.nanoTime();
        for (String word : testData) {
            mDictionary.Add(word);
        }
        //mDictionary.getAppearedWordsWithCount();
        long endTimeMDictionary = System.nanoTime();
        long durationMDictionary = endTimeMDictionary - startTimeMDictionary;

        // Benchmark HashMap
        HashMap<String, Integer> hashMap = new HashMap<>();
        long startTimeHashMap = System.nanoTime();
        for (String word : testData) {
            hashMap.put(word, hashMap.getOrDefault(word, 0) + 1);
        }
        //hashMap.entrySet();
        long endTimeHashMap = System.nanoTime();
        long durationHashMap = endTimeHashMap - startTimeHashMap;

        // Output the results
        System.out.println("MDictionary Time: " + durationMDictionary / 100_000 + " miliseconds");
        System.out.println("HashMap Time: " + durationHashMap / 100_000 + " miliseconds");
    }

    private static String[] generateTestData(int numElements) {
        String[] data = new String[numElements];
        Random random = new Random();
        for (int i = 0; i < numElements; i++) {
            data[i] = "word" + random.nextInt(NUM_ELEMENTS);
        }
        return data;
        /*SearchEngine se = new SearchEngine();
        String[] ss = se.readFiles("files", new MorfologyTool());
        return ss;*/
    }
}
