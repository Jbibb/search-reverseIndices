import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

public class SearchApplication {
    public static void main1(String[] args){
        int sinsCount = 0;
        for(File file : new File("profiles").listFiles()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (br.ready()) {
                    System.out.println(br.readLine().replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", ""));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        MorfologyTool mt = new MorfologyTool();
        SearchEngine se = new SearchEngine();
        String[] keyWords = se.readProfiles();
        MDictionary mDict = new MDictionary();
        for(String word : keyWords) {
            mDict.Add(word);
            if(word.equals("adobe"))
                System.out.println("adobe!!!");
        }
        System.out.println("-------- In dictionary -------");
        for (String word : mDict.getWords()) {
            System.out.println(word);
        }
        String word1 = "";
        String word2 = "";
        String phrase = "";
        String[] words = null;
        String[] ss = se.readFiles("files", mt);
        for(String s : ss)
          System.out.println(s);
        File folder = new File("files");
        for (final File fileEntry : folder.listFiles()) {
            words = se.readFile(fileEntry);
            mDict.Reset();
            for(String word : words)
                mDict.Find(word);
            Pair<String, Integer>[] wordsL = mDict.getAppearedWordsWithCount();
            se.makeIndex(fileEntry, wordsL);
        }
        // wyświetl pliki zawierające dane słowo
        String word = "haubica";
        System.out.println("--------- Files containing " + word + " --------");
        long startMoje = System.nanoTime();
        String[] res = se.getDocsContainingWord(word);
        long endMoje = System.nanoTime();
        double timeMoje = (endMoje - startMoje *1.0)/1_000_000;
        System.out.println("Czas w ms = " + timeMoje);
        for(String file : res)
            System.out.println(file);
        // wyświetl pliki zawierające wszystkie słowa
        words = new String[] {"drużyna", "finał", "hokej"};
        System.out.print("--------- Files containing ");
        for(String w : words)
            System.out.print(w + " ");
        System.out.println(" --------");
        long start = System.nanoTime();
        String[] strings = se.getDocsContainingWords(words);
        long end = System.nanoTime();
        double time = (end - start *1.0)/1_000_000;
        System.out.println("Czas w ms = " + time);
        for(String file : strings)
            System.out.println(file);
        // wyświetl pliki zawierające najwięcej z podanych słów
        words = new String[] {"agresia", "boisko", "bramkarz", "czerwona", "bramka"};
        System.out.print("--------- Files containing max of: ");
        for(String w : words)
            System.out.print(w + " ");
        System.out.println(" --------");
        for(String file : se.getDocsWithMaxMatchingWords(words, 6))
            System.out.println(file);
        String profileName = "sport";
        System.out.println("-------- Files closest to the profile: '"+ profileName + "' --------");
        start = System.nanoTime();
        Pair<String, Double> [] files = se.getDocsClosestToProfile(10, profileName);
        end = System.nanoTime();
        time = (end - start *1.0)/1_000_000;
        System.out.println("Czas w ms = " + time);
        for(Pair<String, Double> pair : files)
            System.out.println(pair.getValue0() + ": " + pair.getValue1());
    }
}
