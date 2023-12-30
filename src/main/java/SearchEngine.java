/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;

import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {

    public String[] readFiles(String directory, MorfologyTool mt) {

        File folder = new File("files");
        HashSet<String> set = new HashSet<>();
        for (final File file : folder.listFiles()) {
            //System.out.println(file.toString());
            String[] split = null;
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                String text = "";
                while ((line = br.readLine()) != null)
                    text += line + " ";
                br.close();
                text = text.replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", "");
                split = text.split("[^a-zA-Z0-9ąćęłńóśżźĄĆŁŃÓĘŚŻŹ]+");
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (String s : split)
                if (toString().trim().length() > 1)
                    set.add(mt.getConcept(s.toLowerCase()));
        }
        return set.toArray(String[]::new);
    }


    /**
     * Czytanie pliku i jego rozbiór morfologiczny
     * @param file
     * @return
     */
    public String[] readFile(File file) {
        String[] res = new String[Rozmiar.MAX_WORD];
        int resIndex = 0;
        MorfologyTool mt = new MorfologyTool();
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String[] split = null;
            String line;
            String text = "";
            while ((line = br.readLine()) != null)
                text += line + " ";
            //System.out.println("PRZED:");
            //System.out.println(text);
            text = text.replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", "");
            //System.out.println("PO:");
            //System.out.println(text);
            split = text.split("[^a-zA-Z0-9ąćęłńóśżźĄĆŁŃÓĘŚŻŹ]+");
            for (String s : split)
                if (toString().trim().length() > 1)
                    res[resIndex++] = mt.getConcept(s.toLowerCase());
        } catch (IOException e){
            e.printStackTrace();
        }

        return Arrays.copyOfRange(res, 0, resIndex);
    }


    /**
     * Czytanie profili i scalanie ich (merge). Metoda zwraca słownik główny
     *
     */
    public String[] readProfiles() {
        File profilesFolder = new File("profiles");
        String[] res = null;
        int i = 0;
        for(File file : profilesFolder.listFiles()) {
            if(i++ == 0)
                res = readProfile(file.getName());
            else res = merge(res, readProfile(file.getName()));
        }
        return res;
    }



    private String[] merge(String[] t1, String[] t2) {
        String[] res = new String[t1.length + t2.length];
        int resIndex = 0;
        for(String word : t1)
            res[resIndex++] = word;
        for(String word : t2)
            res[resIndex++] = word;
        return res;
    }



    public String[] readProfile(String profileName) {
        File file = new File("profiles/" + profileName);
        String[] res = new String[100];
        int resIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while(br.ready()) {
                if (resIndex >= res.length)
                    res = Arrays.copyOf(res, (int) (res.length * 1.25));
                res[resIndex++] = br.readLine().replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", ""); // to jest potrzebne bo w plikach profilowych jest jakieś zło które sprawia że .equals() nie działa
                //res[resIndex++] = br.readLine();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return Arrays.copyOfRange(res, 0, resIndex);
    }


    /**
     * Tworzy plik indeksowy dla danego pliku tekstowego: w każdym wierszu jest pojęcie oraz jego liczba wystąpień w pliku
     * @param fileEntry
     * @param wordsL
     */
    public void makeIndex(File fileEntry, Pair<String, Integer>[] wordsL) {
        File file = new File("indices/" + String.copyValueOf(fileEntry.getName().toCharArray(), 0, fileEntry.getName().length() - 4) + ".idx");

        try {
            if(!file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                fos.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(file)) {
            for(Pair pair : wordsL)
                pw.println(pair.getValue0() + " " + pair.getValue1());
        } catch (IOException e){
            e.printStackTrace();
        }

        makeReversedIndexes();

    }

    private void updateIndex(File fileEntry, String word) {
       //to do
    }



    /**
     * Tworzy pliki indeksowe dla słów kluczowych: w każdym wierszu jest nazwa pliku oraz liczba wystąpień słowa w pliku
     */
    public void makeReversedIndexes(){
        File folder = new File("indices/");
        String[] lineValues;

        File reverseIndicesFolder = new File("indices/reverseIndices/");
        reverseIndicesFolder.mkdir();

        if(reverseIndicesFolder.listFiles() != null)
            for(File file : reverseIndicesFolder.listFiles()) {
                try {
                    file.createNewFile();
                    if(!file.getName().equals("reverseIndices") && file.exists()) {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        for(File file : folder.listFiles()) {
            if(!file.getName().equals("reverseIndices")) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    while (br.ready()) {
                        lineValues = br.readLine().split(" ");
                        File indexFile = new File("indices/reverseIndices/" + lineValues[0] + ".idx");
                        indexFile.createNewFile();
                        PrintWriter pw = new PrintWriter(new FileOutputStream(indexFile, true));
                        pw.println((String.copyValueOf(file.getName().toCharArray(), 0, file.getName().length() - 4) + ".txt") + " " + lineValues[1]);
                        pw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * Zwraca pliki zawierające podane słowo
     * @param word wyszukiwane słowo
     * @return
     */
    public String[] getDocsContainingWord(String word) {
        String[] res = new String[new File("files").listFiles().length];
        int index = 0;


        try (BufferedReader br = new BufferedReader(new FileReader("indices/reverseIndices/" + word + ".idx"))) {
            while (br.ready()) {
                res[index++] = br.readLine().split("\\.txt")[0] + ".txt";
            }
        } catch (IOException e){
            e.printStackTrace();
        }


        return Arrays.copyOfRange(res, 0, index);
    }


    /**
     * Zwraca pliki zawierające wszystkie podane słowa
     * @param words wyszukiwane słowa
     * @return
     */
    public String[] getDocsContainingWords(String[] words) {
        MDictionary dictionary = new MDictionary();

        for(String word : words) { // O(docs * words)?
            try (BufferedReader br = new BufferedReader(new FileReader("indices/reverseIndices/" + word + ".idx"))) {
                while (br.ready()) {
                     dictionary.Add(br.readLine().split("\\.txt")[0] + ".txt");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Pair[] appeared = dictionary.getAppearedWordsWithCount();
        String[] res = new String[appeared.length];
        int index = 0;
        for(Pair<String, Integer> pair : appeared)
            if (pair.getValue1() == words.length)
                res[index++] = pair.getValue0();
        return Arrays.copyOfRange(res, 0, index);
    }



    /**
     * Zwraca n plików zawierających najwięcj poszukiwanych słów
     * @param words
     * @return
     */
    public String[] getDocsWithMaxMatchingWords(String[] words, int n) {
        MDictionary dictionary = new MDictionary();
        String[] fileValues;
        File reverseIndexFile;
        for(String word : words) { // O(docs * words)?
            reverseIndexFile = new File("indices/reverseIndices/" + word + ".idx");
            if(reverseIndexFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(reverseIndexFile))) {
                    while (br.ready()) {
                        fileValues = br.readLine().split("\\.txt ");
                        dictionary.Add(fileValues[0] + ".txt");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Pair<String, Integer>[] res = dictionary.getAppearedWordsWithCount();
        Arrays.sort(res, (pair2, pair1) -> pair1.getValue1() - pair2.getValue1());
        String[] newRes = new String[Math.min(n, res.length)];
        for (int i = 0; i < newRes.length; i++)
            newRes[i] = res[i].getValue1() + " " + res[i].getValue0();
        return newRes;
    }

    /**
     * Zwrócenie n dokumentów z największą zgodnościa z wybranym profilem
     * @param n
     * @return
     */
    public Pair<String,Double>[] getDocsClosestToProfile(int n, String profileName) {
        File fileFolder = new File("files/");
        int fileCount = fileFolder.listFiles().length;
        File[] files = fileFolder.listFiles();

        File profileFile = new File("profiles/" + profileName + ".txt");
        String[] profileKeyWords = readProfile(profileFile.getName());

        File reverseIndicesFolder = new File("indices/reverseIndices");
        Pair<String, Integer>[][] reverseIndices = new Pair[profileKeyWords.length][]; //odwrócone indeksy w postaci podtablic par reprezentujących każdy z plików



        //czej:
        int l = 0;
        for(String keyWord : profileKeyWords) {
            reverseIndices[l] = readIndex(new File("indices/reverseIndices/" + profileKeyWords[l] + ".idx"), 0);
            l++;
        }
        //TU SKOŃCZYŁEŚ. \/\/\/ DOPASOWYWUJESZ Z LISTY WSZYSTKICH PLIKÓW INDEKSÓW ODWRÓCONYCH TYLKO TE KTÓRE ODPOWIADAJĄ SŁOWOM KLUCZOWYM Z PROFILU DLA KTÓREGO SZUKASZ PLIKÓW \/\/\/\/\/
        //DLACZEGO NIE ITERUJESZ POPROSTU PO LIŚCIE SŁÓW KLUCZOWYCH I NIE OTWIERASZ PLIKÓW KTÓRE MAJĄ W NAZWIE SŁOWA KLUCZOWE? NIE WIEM.
        /*for(File reverseIndexFile : reverseIndicesFolder.listFiles()) {
            if(new File("indices/reverseIndices/" + profileKeyWords[l] + ".idx").exists()) {
                System.out.println(reverseIndexFile.getName().split(".idx")[0] + " ==== " + profileKeyWords[l]);
                if (reverseIndexFile.getName().split(".idx")[0].equals(profileKeyWords[l]))
                    reverseIndices[l++] = readIndex(reverseIndexFile, 0);
            } else l++;
        }*/
        // TU /\/\/\/\/\/\/\/\/\/\/\/\

        Pair<String, Double>[] pairs = new Pair[fileCount];

        double logResult;

        for(int i = 0; i < fileCount; i++) { //1
            for(String keyWord : profileKeyWords) {
                for(Pair<String, Integer>[] reverseIndex : reverseIndices) {
                    for(Pair<String, Integer> pair : reverseIndex) {
                        if (pair.getValue0().equals(files[i].getName())) {
                            logResult = Math.log10(pair.getValue1());
                            if (pairs[i] == null) {
                                pairs[i] = new Pair<>(pair.getValue0(), logResult);
                            } else {
                                pairs[i] = pairs[i].setAt1(pairs[i].getValue1() + logResult);
                            }
                            break;
                        }
                    }
                }
            }
        }
        int nulls = 0;
        for (int j = 0; j < pairs.length; j++)
            if(pairs[j] == null)
                nulls++;

        Pair<String, Double>[] newPairs = new Pair[pairs.length - nulls];
        for (int j = 0, k = 0; j < pairs.length; j++) {
            if(pairs[j] != null)
                newPairs[k++] = pairs[j];
        }

        double rounded;
        for (int j = 0; j < newPairs.length; j++){
            rounded = (double) Math.round(newPairs[j].getValue1() / profileKeyWords.length * 100) / 100;
            newPairs[j] = newPairs[j].setAt1(rounded);
        }

        Arrays.sort(newPairs, (pair1, pair2) -> Double.compare(pair2.getValue1(), pair1.getValue1()));
        return Arrays.copyOfRange(newPairs, 0, n);
    }

    private Pair<String, Integer>[] readIndex(File file, int count) {
        Pair<String, Integer>[] res = new Pair[30];
        String[] fileValues;
        int resIndex = 0;
        try (BufferedReader reverseIndexBr = new BufferedReader(new FileReader(file))) {
            while (reverseIndexBr.ready()) {
                fileValues = reverseIndexBr.readLine().split(".txt ");
                if(resIndex >= res.length)
                    res = Arrays.copyOf(res, (int) (res.length * 1.25));
                res[resIndex++] = new Pair<>(fileValues[0] + ".txt", Integer.parseInt(fileValues[1]));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return Arrays.copyOfRange(res, 0, resIndex);
    }

    private Pair<String, Integer>[] sort(Pair<String, Integer>[] pairs) {
        // to do
        return null;
    }
}