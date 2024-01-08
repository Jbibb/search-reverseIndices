import org.javatuples.Pair;

import java.io.File;

/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

public class SearchApplication {
    public static void main(String[] args) {
        MorfologyTool mt = new MorfologyTool();
        SearchEngine se = new SearchEngine();
        String[] keyWords = se.readProfiles();
        MDictionary mDict = new MDictionary();
        for(String word : keyWords) {
            mDict.Add(word);
        }
        System.out.println("-------- In dictionary -------");
        for (String word : mDict.getWords())
            System.out.println(word);
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
        String word = "rakieta";
        System.out.println("--------- Files containing " + word + " --------");
        for(String file : se.getDocsContainingWord(word))
            System.out.println(file);
        // wyświetl pliki zawierające wszystkie słowa
        words = new String[] {"armia", "artyleria", "front", "wojsko"};
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
        words = new String[] { "agresja", "agresor", "amunicja", "armia", "artyleria", "artyleryjski", "balistyczny", "bitwa", "bojowy", "broń", "broń", "jądrowa", "brygada", "cywil", "cywilny", "czołg", "dowodzenie", "dowódca", "dowództwo", "dron", "dywizja", "front", "generał", "haubica", "inwazja", "kaliber", "kampania", "konflikt", "kontratak", "kontrofensywa", "krab", "leopard", "logistyka", "lotnictwo", "militarny", "minister ", "obrony", "ministerstwo", "obrony", "mobilizacja", "myśliwiec", "natarcie", "negocjacja", "nuklearny", "obrona", "obronny", "odwrót", "ofensywa", "ofiara", "oficer", "ogień", "okrążenie", "okupowany", "opancerzony", "operacja", "operacyjny", "ostrzał", "pancerny", "pancerz", "piechota", "piorun", "poborowy", "pobór", "pocisk", "pododdział", "porozumienie", "przeciwlotniczy", "przeciwnik", "przeciwpancerny", "przywódca", "radar", "rakieta", "rakietowy", "ranny", "rezerwa", "rezerwista", "reżim", "rosja", "rosjanin", "rosyjski", "samobieżny", "samolot", "sankcja", "siły", "zbrojne", "sojusz", "sojusznik", "sprzęt", "strategia", "strategiczny", "system", "rakietowy", "sztab", "służba", "śmierć", "śmigłowiec", "taktyczny", "taktyka", "terytorialny", "terytorium", "ukraina", "ukrainiec", "ukraiński", "uzbrojenie", "uzbrojony", "wojenny", "wojna", "wojsko", "wojskowy", "wróg", "wybuch", "wyposażenie", "wyrzutnia", "wyszkolony", "wywiad", "wywiadowczy", "wyzwolenie", "zaatakować", "zabity", "zaplecze", "zimna", "wojna", "zniszczenie", "zniszczony", "zniszczyć", "żołnierz", "żołnierski"};
        System.out.print("--------- Files containing max of: ");
        for(String w : words)
            System.out.print(w + " ");
        System.out.println(" --------");
        for(String file : se.getDocsWithMaxMatchingWords(words, 6))
            System.out.println(file);
        String profileName = "militaria";
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
