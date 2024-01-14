/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;

import java.io.*;
import java.util.*;

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
                //text = text.replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", "");
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
     *
     * @param file
     * @return
     */
    public String[] readFile(File file) {
        //System.out.print(file.getName());
        String[] res = new String[30], tmp;
        int resIndex = 0;
        MorfologyTool mt = new MorfologyTool();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String[] split = null;
            String line;
            String text = "";
            while ((line = br.readLine()) != null)
                text += line + " ";
            //text = text.replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", "");
            split = text.split("[^a-zA-Z0-9ąćęłńóśżźĄĆŁŃÓĘŚŻŹ]+");
            for (String s : split)
                if (toString().trim().length() > 1) {
                    if (resIndex >= res.length) {
                        tmp = new String[(int) (res.length * 1.25)];
                        System.arraycopy(res, 0, tmp, 0, res.length);
                        res = tmp;
                    }
                    res[resIndex++] = mt.getConcept(s.toLowerCase());
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tmp = new String[resIndex];
        System.arraycopy(res, 0, tmp, 0, tmp.length);
        res = tmp;
        return res;
    }

    /**
     * Czytanie profili i scalanie ich (merge). Metoda zwraca słownik główny
     */
    public String[] readProfiles() {
        File profilesFolder = new File("profiles");
        String[] res = null;
        int i = 0;
        for (File file : profilesFolder.listFiles()) {
            if (i++ == 0)
                res = readProfile(file.getName());
            else res = merge(res, readProfile(file.getName()));
        }
        return res;
    }

    private String[] merge(String[] t1, String[] t2) {
        String[] res = new String[t1.length + t2.length];
        int resIndex = 0;
        for (String word : t1)
            res[resIndex++] = word;
        for (String word : t2)
            res[resIndex++] = word;
        return res;
    }

    public String[] readProfile(String profileName) {
        File file = new File("profiles/" + profileName);
        String[] res = new String[100], tmp;
        int resIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.ready()) {
                if (resIndex >= res.length) {
                    tmp = new String[(int) (res.length * 1.25)];
                    System.arraycopy(res, 0, tmp, 0, res.length);
                    res = tmp;
                }
                res[resIndex++] = br.readLine().replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", ""); // UTF-8 z BOM
                //res[resIndex++] = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tmp = new String[resIndex];
        System.arraycopy(res, 0, tmp, 0, tmp.length);
        res = tmp;
        return res;
    }

    /**
     * Tworzy plik indeksowy dla danego pliku tekstowego: w każdym wierszu jest pojęcie oraz jego liczba wystąpień w pliku
     *
     * @param fileEntry
     * @param wordsL
     */
    public void makeIndex(File fileEntry, Pair<String, Integer>[] wordsL) {
        File file = new File("indices/" + String.copyValueOf(fileEntry.getName().toCharArray(), 0, fileEntry.getName().length() - 4) + ".idx");

        try {
            if (!file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(file)) {
            for (Pair<String, Integer> pair : wordsL) {
                pw.println(pair.getValue0() + " " + pair.getValue1());
                //updateReverseIndex(pair.getValue0(), pair.getValue1());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateReverseIndex(String keyWord, int count) {
        File reverseIndexFile = new File("indices/reverseIndices/" + keyWord + ".idx");
        File reverseIndicesFolder = new File("indices/reverseIndices");
        reverseIndicesFolder.mkdir();

        try {
            reverseIndexFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(new FileOutputStream(reverseIndexFile, true))) {
            ;//pw.println(currentFileId + " " + count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateIndex(File fileEntry, String word) {
        //todo
    }


    /**
     * Tworzy pliki indeksowe dla słów kluczowych: w każdym wierszu jest id pliku oraz liczba wystąpień słowa w pliku
     */
    public void makeReverseIndices() {
        File fileFolder = new File("files/");
        String[] fileNames = fileFolder.list();
        String[] lineValues;

        File reverseIndicesFolder = new File("indices/reverseIndices/");

        if(reverseIndicesFolder.delete())
            reverseIndicesFolder.mkdir();

        if (reverseIndicesFolder.listFiles() != null)
            for (File file : reverseIndicesFolder.listFiles()) {
                try {
                    if (file.exists()) {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        for (int i = 0; i < fileNames.length; i++) {
            try (BufferedReader br = new BufferedReader(new FileReader("indices/" + fileNames[i].split(".txt")[0] + ".idx"))) {
                while (br.ready()) {
                    lineValues = br.readLine().split(" ");
                    File indexFile = new File("indices/reverseIndices/" + lineValues[0] + ".idx");
                    indexFile.createNewFile();
                    PrintWriter pw = new PrintWriter(new FileOutputStream(indexFile, true));
                    pw.println(i + " " + lineValues[1]);
                    pw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Pair<Integer, Integer>[][] getReverseIndicesForKeywords(String[] keyWords) {
        File reverseIndicesDirectory = new File("indices/reverseIndices");

        if(!reverseIndicesDirectory.exists() || reverseIndicesDirectory.list() == null)
            makeReverseIndices();

        Pair<Integer, Integer>[][] reverseIndices = new Pair[keyWords.length][]; //odwrócone indeksy w postaci podtablic par reprezentujących każdy z plików [pary (plik, liczebność słowa)]
        /* Wierszy jest tyle co słów kluczowych (profile.length). We wszystkich wierszach (dla wszystkich słów kluczowych) liczba par powinna (chyba) być zbliżona do liczby plików tekstowych.
        Pary są posortowane rosnąco id plików. (pierwsza wartość w parze)
        [
        [(235, 3), (543, 1), (3424, 7)],
        [(174, 1), (235, 2), (2326, 12), (2562, 6)],
        [(235, 3), (543, 1), (3424, 7)]
        ]
         */
        int l = 0;
        for (String keyWord : keyWords) {
            reverseIndices[l] = readIndex(new File("indices/reverseIndices/" + keyWords[l] + ".idx"));

            /*System.out.print("(" + keyWord + "): [");
            for(Pair<Integer, Integer> pair : reverseIndices[l])
                System.out.print(pair + " ");
            System.out.println("]");*/

            l++;
        }

        return reverseIndices;

    }

    /**
     * Zwraca pliki zawierające podane słowo
     *
     * @param word wyszukiwane słowo
     * @return
     */
    public String[] getDocsContainingWord(String word) {
        String[] fileNames = new File("files").list();

        String[] res = new String[30], tmp;
        int resIndex = 0;

        //Czytamy odwrócony indeks dla słowa kluczowego - wszystkie pliki zawarte w nim zawierają słowo.
        for (Pair<Integer, Integer> pair : readIndex(new File("indices/reverseIndices/" + word + ".idx"))) {
            if (resIndex >= res.length) {
                tmp = new String[(int) (res.length * 1.25)];
                System.arraycopy(res, 0, tmp, 0, res.length);
                res = tmp;
            }
            res[resIndex++] = fileNames[pair.getValue0()];
        }
        tmp = new String[resIndex];
        System.arraycopy(res, 0, tmp, 0, tmp.length);
        res = tmp;
        return res;
    }

    /**
     * Zwraca pliki zawierające wszystkie podane słowa
     *
     * @param words wyszukiwane słowa
     * @return
     */
    public String[] getDocsContainingWords(String[] words) {
        String[] fileNames = new File("files").list();

        Pair<Integer, Integer>[][] reverseIndices = getReverseIndicesForKeywords(words);

        int[] pointers = new int[reverseIndices.length];
        int finishedPointers = 0;

        String[] res = new String[30], tmp;
        int resIndex = 0;

        for (int i = 0; i < reverseIndices.length; i++)
            if (reverseIndices[i].length == 0) {
                finishedPointers++;
            }


        int minFileId;
        boolean fileMatchesAllKeywords;
        while (finishedPointers == 0) {
            minFileId = Integer.MAX_VALUE;
            for (int i = 0; i < pointers.length; i++)                            //Szukanie najmniejszego id pliku spośród par wskazywanych przez indeksy
                if (reverseIndices[i][pointers[i]].getValue0() < minFileId) {
                    minFileId = reverseIndices[i][pointers[i]].getValue0();
                }

            fileMatchesAllKeywords = true;
            for (int i = 0; i < pointers.length; i++) {
                if (reverseIndices[i][pointers[i]].getValue0() == minFileId) {
                    if (++pointers[i] >= reverseIndices[i].length) {
                        finishedPointers++;
                    }
                } else fileMatchesAllKeywords = false;
            }

            if (fileMatchesAllKeywords) {
                if (resIndex >= res.length) {
                    tmp = new String[(int) (res.length * 1.25)];
                    System.arraycopy(res, 0, tmp, 0, res.length);
                    res = tmp;
                }
                res[resIndex++] = fileNames[minFileId];

                for (int i = 0; i < pointers.length; i++)
                    if (++pointers[i] == reverseIndices[i].length)
                        finishedPointers++;
            }
        }
        /*
        [
        [(235, 3), (543, 1), (3424, 7)],
        [(174, 1), (235, 2), (2326, 12), (2562, 6)],
        [(102, 3), (222, 1), (235, 7)]
        ]
         */

        tmp = new String[resIndex];
        System.arraycopy(res, 0, tmp, 0, tmp.length);
        res = tmp;
        return res;
    }

    /**
     * Zwraca n plików zawierających najwięcj poszukiwanych słów
     *
     * @param words
     * @return
     */
    public String[] getDocsWithMaxMatchingWords(String[] words, int n) {
        String[] fileNames = new File("files").list();

        Pair<Integer, Integer>[][] reverseIndices = getReverseIndicesForKeywords(words);

        int[] pointers = new int[reverseIndices.length];
        int finishedPointers = 0;
        for (int i = 0; i < reverseIndices.length; i++) {
            if (reverseIndices[i].length == 0) {
                pointers[i] = -1;
                finishedPointers++;
            }
        }

        Pair<String, Integer>[] array = new Pair[pointers.length], tmp;
        int index = 0;

        int minFileId, matchingFileIdCount;
        while (finishedPointers != pointers.length) {
            minFileId = Integer.MAX_VALUE;
            for (int i = 0; i < pointers.length; i++)
                if (pointers[i] != -1)
                    if (reverseIndices[i][pointers[i]].getValue0() < minFileId)
                        minFileId = reverseIndices[i][pointers[i]].getValue0();

            matchingFileIdCount = 0;
            for (int i = 0; i < pointers.length; i++) {
                if (pointers[i] != -1) {
                    if (reverseIndices[i][pointers[i]].getValue0() == minFileId) {
                        matchingFileIdCount++;
                        if (++pointers[i] >= reverseIndices[i].length) {
                            pointers[i] = -1;
                            finishedPointers++;
                        }
                    }
                }
            }


            if (index >= array.length) {
                tmp = new Pair[(int) (array.length * 1.25)];
                System.arraycopy(array, 0, tmp, 0, array.length);
                array = tmp;
            }
            array[index++] = new Pair<String, Integer>(fileNames[minFileId], matchingFileIdCount);

        }

        array = Arrays.copyOf(array, index); //tu powinien być counting sort

        tmp = new Pair[index];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        array = tmp;

        if (n > array.length) n = array.length;

        int max = -1;
        for (int i = 1; i < array.length; i++)
            if (array[i].getValue1() > max)
                max = array[i].getValue1();

        for (int exp = 1; max / exp > 0; exp *= 10) {
            Pair<String, Integer> output[] = new Pair[array.length];
            int count[] = new int[10];
            Arrays.fill(count, 0);

            for (int i = 0; i < array.length; i++)
                count[9 - (array[i].getValue1() / exp) % 10]++;

            for (int i = 1; i < 10; i++)
                count[i] += count[i - 1];

            for (int i = array.length - 1; i >= 0; i--) {
                output[count[9 - (array[i].getValue1() / exp) % 10] - 1] = array[i];
                count[9 - (array[i].getValue1() / exp) % 10]--;
            }

            System.arraycopy(output, 0, array, 0, output.length);
        }


        //Arrays.sort(array, (pair2, pair1) -> pair1.getValue1() - pair2.getValue1());
        String[] res = new String[n];

        for (int m = 0; m < n; m++)
            res[m] = array[m].getValue1() + " " + array[m].getValue0();

        return res;
    }

    /**
     * Zwrócenie n dokumentów z największą zgodnościa z wybranym profilem
     *
     * @param n
     * @return
     */
    public Pair<String, Double>[] getDocsClosestToProfile(int n, String profileName) {
        String[] fileNames = new File("files").list();

        String[] keyWords = readProfile(profileName + ".txt");
        Pair<Integer, Integer>[][] reverseIndices = getReverseIndicesForKeywords(keyWords);

        Pair<String, Double>[] res = new Pair[30], tmp;
        int fileIdsOccurrencesIndex = 0;

        int[] pointers = new int[reverseIndices.length];

        int minFileId, finishedPointers = 0;
        double logSum;
        for (int i = 0; i < reverseIndices.length; i++)
            if (reverseIndices[i].length == 0) {
                pointers[i] = -1;
                finishedPointers++;
            }

        while (finishedPointers != pointers.length) {

            minFileId = Integer.MAX_VALUE;
            for (int i = 0; i < pointers.length; i++)
                if (pointers[i] != -1)
                    if (reverseIndices[i][pointers[i]].getValue0() < minFileId)
                        minFileId = reverseIndices[i][pointers[i]].getValue0();

            logSum = 0;
            for (int i = 0; i < pointers.length; i++) {
                if (pointers[i] != -1) {
                    if (reverseIndices[i][pointers[i]].getValue0() == minFileId) {
                        logSum += Math.log10(reverseIndices[i][pointers[i]].getValue1());

                        if (++pointers[i] >= reverseIndices[i].length) {
                            pointers[i] = -1;
                            finishedPointers++;
                        }
                    }
                }
            }
            logSum /= keyWords.length;
            logSum = Math.round(logSum * 10000d) / 100d;
            if (fileIdsOccurrencesIndex >= res.length) {
                tmp = new Pair[(int) (res.length * 1.25)];
                System.arraycopy(res, 0, tmp, 0, fileIdsOccurrencesIndex);
                res = tmp;
            }
            res[fileIdsOccurrencesIndex++] = new Pair<>(fileNames[minFileId], logSum);
        }

        tmp = new Pair[fileIdsOccurrencesIndex];
        System.arraycopy(res, 0, tmp, 0, fileIdsOccurrencesIndex);
        res = tmp;

        Arrays.sort(res, (pair2, pair1) -> Double.compare(pair1.getValue1(), pair2.getValue1()));
        tmp = new Pair[Math.min(fileIdsOccurrencesIndex, n)];
        System.arraycopy(res, 0, tmp, 0, tmp.length);
        res = tmp;

        return res;
    }
    public Pair<String, Double>[] getDocsClosestToProfileWorse(int n, String profileName) {
        File indicesDirectory = new File("indices");
        File[] indices = indicesDirectory.listFiles();
        Pair<String, Double>[] res = new Pair[indices.length];
        double logSum;
        int resIndex = 0;
        String[] fileValues;

        String[] keyWords = readProfile(profileName + ".txt");
        MDictionary mDict = new MDictionary();
        for(String word : keyWords) {
            mDict.Add(word);
        }

        for(File indexFile : indices){
            logSum = 0;
            try(BufferedReader br = new BufferedReader(new FileReader(indexFile.getAbsolutePath()))){
                while(br.ready()) {
                    fileValues = br.readLine().split(" ");
                    if(mDict.Find(fileValues[0]) != 0)
                        logSum += Math.log10(Integer.parseInt(fileValues[1]));
                }
            } catch (IOException e){
                ;//e.printStackTrace();
            }
            res[resIndex++] = new Pair<>(indexFile.getName(), Math.round(logSum/keyWords.length * 10000d) / 100d);
        }
        Arrays.sort(res, (pair2, pair1) -> Double.compare(pair1.getValue1(), pair2.getValue1()));
        return res;
    }

    /**
     * Zwrócenie tablicy par (id pliku, liczba wystąpień słowa). Ilość odczytanych par jest zależna od argumentu.
     *
     * @param file
     * @param count definiuje liczbę par zaczerpniętych z pliku
     * @return
     */
    private Pair<Integer, Integer>[] readIndex(File file, int count) {
        Pair<Integer, Integer>[] res = new Pair[30], tmp;
        String[] fileValues;
        int resIndex = 0;
        if (file.exists())
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (br.ready() && resIndex < count) {
                    fileValues = br.readLine().split(" ");
                    if (resIndex >= res.length) {
                        tmp = new Pair[(int) (res.length * 1.25)];
                        System.arraycopy(res, 0, tmp, 0, res.length);
                        res = tmp;
                    }
                    res[resIndex++] = new Pair<>(Integer.parseInt(fileValues[0]), Integer.parseInt(fileValues[1]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        tmp = new Pair[resIndex];
        System.arraycopy(res, 0, tmp, 0, tmp.length);
        res = tmp;

        return res;
    }

    /**
     * Zwrócenie tablicy wszystkich par (id pliku, liczba wystąpień słowa) z pliku.
     *
     * @param file
     * @param
     * @return
     */
    private Pair<Integer, Integer>[] readIndex(File file) {
        Pair<Integer, Integer>[] res = new Pair[30], tmp;
        String[] fileValues;
        int resIndex = 0;
        if (file.exists())
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (br.ready()) {
                    fileValues = br.readLine().split(" ");
                    if (resIndex >= res.length) {
                        tmp = new Pair[(int) (res.length * 1.25)];
                        System.arraycopy(res, 0, tmp, 0, res.length);
                        res = tmp;
                    }
                    res[resIndex++] = new Pair<>(Integer.parseInt(fileValues[0]), Integer.parseInt(fileValues[1]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        tmp = new Pair[resIndex];
        System.arraycopy(res, 0, tmp, 0, tmp.length);
        res = tmp;
        return res;
    }

    private Pair<String, Integer>[] sort(Pair<String, Integer>[] pairs) {
        // to do
        return null;
    }
}