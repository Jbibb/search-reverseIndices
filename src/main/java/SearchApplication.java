import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

public class SearchApplication {
    public static void main1(String[] args){
        try (BufferedReader br = new BufferedReader(new FileReader("files/Android będzie płatny, więc smartfony zdrożeją.txt"))) {
            while (br.ready()) {
                System.out.println(br.readLine());
                        //.replaceAll("[^\\x20A-Za-zĄąĆćĘęŁłÓóŚśŻżŹź\\xA5\\xB9\\xC6\\xE6\\xCA\\xEA\\xA3\\xB3\\xD3\\xF3\\x8C\\x9C\\x8F\\x9F\\xA1\\xB1]+", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        MorfologyTool mt = new MorfologyTool();
        SearchEngine se = new SearchEngine();
        String[] keyWords = se.readProfiles();
        MDictionary mDict = new MDictionary();

        String word1 = "";
        String word2 = "";
        String phrase = "";
        String[] words = null;
        if(SearchEngine.UPDATE_INDICES_FLAG) {
            for (String word : keyWords) {
                mDict.Add(word);
            }
            /*
            System.out.println("-------- In dictionary -------");
            for (String word : mDict.getWords()) {
                System.out.println(word);
            }
             */

            /*
            String[] ss = se.readFiles("files", mt);
            for (String s : ss)
                System.out.println(s);
            */

            File folder = new File("files");
            for (final File fileEntry : folder.listFiles()) {
                words = se.readFile(fileEntry);
                mDict.Reset();
                for (String word : words)
                    mDict.Find(word);
                Pair<String, Integer>[] wordsL = mDict.getAppearedWordsWithCount();
                se.makeIndex(fileEntry, wordsL);
            }

        }
        se.makeReverseIndices();

        // wyświetl pliki zawierające dane słowo
        String word = "rakieta";
        System.out.println("--------- Files containing " + word + " --------");
        long startMoje = System.nanoTime();
        String[] res = se.getDocsContainingWord(word);
        long endMoje = System.nanoTime();
        double timeMoje = (endMoje - startMoje *1.0)/1_000_000;
        System.out.println("Czas w ms = " + timeMoje);
        for(String file : res)
            System.out.println(file);
        // wyświetl pliki zawierające wszystkie słowa
        words = new String[] {"rakieta"};
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
        words = new String[] {"armia", "artyleria", "front", "generał", "wojsko", "broń", "bitwa", "atakować"};
        System.out.print("--------- Files containing max of: ");
        for(String w : words)
            System.out.print(w + " ");
        System.out.println(" --------");
        for(String file : se.getDocsWithMaxMatchingWords(words, 6))
            System.out.println(file);
        String profileName = "militaria";
        System.out.println("-------- Files closest to the profile: '"+ profileName + "' --------");
        start = System.nanoTime();
        Pair<String, Double> [] files = se.getDocsClosestToProfile(25, profileName);
        end = System.nanoTime();
        time = (end - start *1.0)/1_000_000;
        System.out.println("Czas w ms = " + time);
        for(Pair<String, Double> pair : files)
            System.out.println(pair.getValue0() + ": " + pair.getValue1());
    }
}
