/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;

import java.io.*;
import java.util.*;

public class SearchEngine {
    public static final boolean UPDATE_INDICES_FLAG = true;
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
        String[] res = new String[30];
        int resIndex = 0;
        MorfologyTool mt = new MorfologyTool();
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String[] split = null;
            String line;
            String text = "";
            while ((line = br.readLine()) != null)
                text += line + " ";
            //text = text.replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", "");
            //split = text.split("[^a-zA-Z0-9ąćęłńóśżźĄĆŁŃÓĘŚŻŹ]+");
            split = text.split("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+");
            for (String s : split)
                if (toString().trim().length() > 1) {
                    if (resIndex >= res.length)
                        res = Arrays.copyOf(res, (int) (res.length * 1.25));
                    res[resIndex++] = mt.getConcept(s.toLowerCase());
                }
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
                //res[resIndex++] = br.readLine().replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", ""); // to jest potrzebne bo w plikach profilowych jest jakieś zło które sprawia że .equals() nie działa
                res[resIndex++] = br.readLine();
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
    }

    private void updateIndex(File fileEntry, String word) {
       //to do
    }

    /**
     * Tworzy pliki indeksowe dla słów kluczowych: w każdym wierszu jest id pliku oraz liczba wystąpień słowa w pliku
     */
    public void makeReverseIndices() {
        File fileFolder = new File("files/");
        String[] fileNames = fileFolder.list();
        String[] lineValues;

        File reverseIndicesFolder = new File("indices/reverseIndices/");
        reverseIndicesFolder.mkdir();

        if(reverseIndicesFolder.listFiles() != null)
            for(File file : reverseIndicesFolder.listFiles()) {
                try {
                    file.createNewFile();
                    if(file.exists()) {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        for(int i = 0; i < fileNames.length; i++) {
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
    private Pair<Integer, Integer>[][] getReverseIndicesForKeywords(String[] keyWords){
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
        for(String keyWord : keyWords) {
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
     * @param word wyszukiwane słowo
     * @return
     */
    public String[] getDocsContainingWord(String word) {
        String[] res = new String[30];
        int resIndex = 0;

        //Czytamy odwrócony indeks dla słowa kluczowego - wszystkie pliki zawarte w nim zawierają słowo.
        for(Pair<Integer, Integer> pair : readIndex(new File("indices/reverseIndices/" + word + ".idx"))) {
            if (resIndex >= res.length)
                res = Arrays.copyOf(res, (int) (res.length * 1.25));
            res[resIndex++] = getFileName(pair.getValue0());
        }
        return Arrays.copyOfRange(res, 0, resIndex);
    }

    private String getFileName(int id) {
        return new File("files").list()[id].replace(".txt", "");
    }

    /**
     * Zwraca pliki zawierające wszystkie podane słowa
     * @param words wyszukiwane słowa
     * @return
     */
    public String[] getDocsContainingWords(String[] words){

        Pair<Integer, Integer>[][] reverseIndices = getReverseIndicesForKeywords(words);

        int[] pointers = new int[reverseIndices.length];
        int finishedPointers = 0;

        String[] res = new String[30];
        int resIndex = 0;

        for(int i = 0; i < reverseIndices.length; i++)
            if(reverseIndices[i].length == 0){
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
            for(int i = 0; i < pointers.length; i++) {
                if (reverseIndices[i][pointers[i]].getValue0() == minFileId) {
                    if (++pointers[i] >= reverseIndices[i].length) {
                        finishedPointers++;
                    }
                } else fileMatchesAllKeywords = false;
            }

            if (fileMatchesAllKeywords) {
                if (resIndex >= res.length)
                    res = Arrays.copyOf(res, (int) (res.length * 1.25));
                res[resIndex++] = getFileName(minFileId);

                for(int i = 0; i < pointers.length; i++)
                    if(++pointers[i] == reverseIndices[i].length)
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

        return Arrays.copyOfRange(res, 0, resIndex);
    }

    /**
     * Zwraca n plików zawierających najwięcj poszukiwanych słów
     * @param words
     * @return
     */
    public String[] getDocsWithMaxMatchingWords(String[] words, int n) {
        Pair<Integer, Integer>[][] reverseIndices = getReverseIndicesForKeywords(words);

        int[] pointers = new int[reverseIndices.length];
        int finishedPointers = 0;
        for(int i = 0; i < reverseIndices.length; i++) {
            if (reverseIndices[i].length == 0) {
                pointers[i] = -1;
                finishedPointers++;
            }
        }

        Pair<String, Integer>[] array = new Pair[pointers.length];
        int index = 0;

        int minFileId, matchingFileIdCount;
        while (finishedPointers != pointers.length) {
            minFileId = Integer.MAX_VALUE;
            for(int i = 0; i < pointers.length; i++)
                if(pointers[i] != -1)
                    if (reverseIndices[i][pointers[i]].getValue0() < minFileId)
                        minFileId = reverseIndices[i][pointers[i]].getValue0();

            matchingFileIdCount = 0;
            for(int i = 0; i < pointers.length; i++) {
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

            if(index >= array.length)
                array = Arrays.copyOf(array, (int)(array.length * 1.25));
            array[index++] = new Pair<String, Integer>(getFileName(minFileId), matchingFileIdCount);

        }

        array = Arrays.copyOf(array, index);
        if(n > array.length) n = array.length;

        int max = array[0].getValue1();
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

            System.arraycopy(output, 0, array, 0, array.length);
        }



        String[] res = new String[n];

        for(int m = 0; m < n; m++)
            res[m] = array[m].getValue1() + " " + array[m].getValue0();

        return res;
    }

    /**
     * Zwrócenie n dokumentów z największą zgodnościa z wybranym profilem
     * @param n
     * @return
     */
    public Pair<String,Double>[] getDocsClosestToProfile(int n, String profileName) {
        String[] keyWords = readProfile(profileName + ".txt");
        Pair<Integer, Integer>[][] reverseIndices = getReverseIndicesForKeywords(keyWords);

        Pair<String, Double>[] res = new Pair[30], tmp;
        int fileIdsOccurrencesIndex = 0;

        int[] pointers = new int[reverseIndices.length];

        int minFileId, finishedPointers = 0;
        double logSum;
        for(int i = 0; i < reverseIndices.length; i++)
            if(reverseIndices[i].length == 0){
                pointers[i] = -1;
                finishedPointers++;
            }

        while (finishedPointers != pointers.length) {

            minFileId = Integer.MAX_VALUE;
            for(int i = 0; i < pointers.length; i++)
                if(pointers[i] != -1)
                    if (reverseIndices[i][pointers[i]].getValue0() < minFileId)
                        minFileId = reverseIndices[i][pointers[i]].getValue0();

            logSum = 0;
            for(int i = 0; i < pointers.length; i++) {
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
            logSum = Math.round(logSum * 100) / 100d;
            if(fileIdsOccurrencesIndex >= res.length) {
                tmp = new Pair[(int)(res.length * 1.25)];
                System.arraycopy(res, 0, tmp, 0, fileIdsOccurrencesIndex);
                res = tmp;
            }
            res[fileIdsOccurrencesIndex++] = new Pair<>(getFileName(minFileId), logSum);
        }

        tmp = new Pair[fileIdsOccurrencesIndex];
        System.arraycopy(res, 0, tmp, 0, fileIdsOccurrencesIndex);
        res = tmp;

        Arrays.sort(res, (pair2, pair1) -> Double.compare(pair1.getValue1(), pair2.getValue1()));
        res = Arrays.copyOf(res, Math.min(fileIdsOccurrencesIndex, n));

        return res;

    }

    /**
     * Zwrócenie tablicy par (id pliku, liczba wystąpień słowa). Ilość odczytanych par jest zależna od argumentu.
     * @param file
     * @param count definiuje liczbę par zaczerpniętych z pliku
     * @return
     */
    private Pair<Integer, Integer>[] readIndex(File file, int count) {
        Pair<Integer, Integer>[] res = new Pair[30];
        String[] fileValues;
        int resIndex = 0;
        if(file.exists())
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (br.ready() && resIndex < count) {
                    fileValues = br.readLine().split(" ");
                    if (resIndex >= res.length)
                        res = Arrays.copyOf(res, (int) (res.length * 1.25));
                    res[resIndex++] = new Pair<>(Integer.parseInt(fileValues[0]), Integer.parseInt(fileValues[1]));
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        return Arrays.copyOfRange(res, 0, resIndex);
    }
    /**
     * Zwrócenie tablicy wszystkich par (id pliku, liczba wystąpień słowa) z pliku.
     * @param file
     * @param
     * @return
     */
    private Pair<Integer, Integer>[] readIndex(File file) {
        Pair<Integer, Integer>[] res = new Pair[30];
        String[] fileValues;
        int resIndex = 0;
        if(file.exists())
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (br.ready()) {
                    fileValues = br.readLine().split(" ");
                    if (resIndex >= res.length)
                        res = Arrays.copyOf(res, (int) (res.length * 1.25));
                    res[resIndex++] = new Pair<>(Integer.parseInt(fileValues[0]), Integer.parseInt(fileValues[1]));
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





    //tu są moje stare:
    public String[] getDocsContainingWordsOld(String[] words) {
        MDictionary dictionary = new MDictionary();

        for(String word : words) { // O(docs * words)?
            try (BufferedReader br = new BufferedReader(new FileReader("indices/reverseIndices/" + word + ".idx"))) {
                while (br.ready()) {
                    dictionary.Add(getFileName(Integer.parseInt(br.readLine().split(" ")[0])));
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
    public String[] getDocsWithMaxMatchingWordsOldOld(String[] words, int n) {
        MDictionary dictionary = new MDictionary();
        String[] fileValues;
        File reverseIndexFile;
        for(String word : words) { // O(docs * words)?
            reverseIndexFile = new File("indices/reverseIndices/" + word + ".idx");
            if(reverseIndexFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(reverseIndexFile))) {
                    while (br.ready()) {
                        fileValues = br.readLine().split(" ");
                        dictionary.Add(getFileName(Integer.parseInt(fileValues[0])));
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
    private void heapify(Pair<String, Integer>[] heap, int range, int i) {
        Pair<String, Integer> tmp;
        boolean leftChildBigger = false, rightChildBigger = false;

        if(2 * i + 1 < heap.length - 1 - range) {
            leftChildBigger = heap[2 * i + 1].getValue1() > heap[i].getValue1();
            if(2 * i + 2 < heap.length - 1 - range)
                rightChildBigger = heap[2 * i + 2].getValue1() > heap[i].getValue1();
            if(leftChildBigger && rightChildBigger) {
                if (heap[2 * i + 1].getValue1() > heap[2 * i + 2].getValue1()) {
                    tmp = heap[i];
                    heap[i] = heap[2 * i + 1];
                    heap[2 * i + 1] = tmp;
                } else {
                    tmp = heap[i];
                    heap[i] = heap[2 * i + 2];
                    heap[2 * i + 2] = tmp;
                }
            } else if (rightChildBigger) {
                tmp = heap[i];
                heap[i] = heap[2 * i + 2];
                heap[2 * i + 2] = tmp;
            } else if (leftChildBigger) {
                tmp = heap[i];
                heap[i] = heap[2 * i + 1];
                heap[2 * i + 1] = tmp;
            }
        }
    }
    private void heapifyRec(Pair<String, Integer>[] heap, int range, int i) {
        Pair<String, Integer> tmp;
        boolean leftChildBigger = false, rightChildBigger = false;

        if(2 * i + 1 < heap.length - 1 - range) {
            leftChildBigger = heap[2 * i + 1].getValue1() > heap[i].getValue1();
            if(2 * i + 2 < heap.length - 1 - range)
                rightChildBigger = heap[2 * i + 2].getValue1() > heap[i].getValue1();
            if(leftChildBigger && rightChildBigger) {
                if (heap[2 * i + 1].getValue1() > heap[2 * i + 2].getValue1()) {
                    tmp = heap[i];
                    heap[i] = heap[2 * i + 1];
                    heap[2 * i + 1] = tmp;
                    if(2 * i + 1 < heap.length - 1 - range)
                        heapifyRec(heap, range, 2 * i + 1);
                } else {
                    tmp = heap[i];
                    heap[i] = heap[2 * i + 2];
                    heap[2 * i + 2] = tmp;
                    if(2 * i + 2 < heap.length - 1 - range)
                        heapifyRec(heap, range, 2 * i + 2);
                }
            } else if (rightChildBigger) {
                tmp = heap[i];
                heap[i] = heap[2 * i + 2];
                heap[2 * i + 2] = tmp;
                if(2 * i + 1 < heap.length - 1 - range)
                    heapifyRec(heap, range, 2 * i + 2);
            } else if (leftChildBigger) {
                tmp = heap[i];
                heap[i] = heap[2 * i + 1];
                heap[2 * i + 1] = tmp;
                if(2 * i + 1 < heap.length - 1 - range)
                    heapifyRec(heap, range, 2 * i + 2);
            }
        }
    }
    public String[] getDocsWithMaxMatchingWordsOld(String[] words, int n) {
        Pair<Integer, Integer>[][] reverseIndices = new Pair[words.length][];

        int l = 0;


        for(String keyWord : words) {
            reverseIndices[l] = readIndex(new File("indices/reverseIndices/" + words[l] + ".idx"));

            /*System.out.print("(" + keyWord + "): [");
            for(Pair<Integer, Integer> pair : reverseIndices[l])
                System.out.print(pair + " ");
            System.out.println("]");*/
            l++;
        }

        /*
        Teraz nie musimy już szukać plików które występują we wszystkich indeksach.
        Interesuje nas liczba indeksów w których plik się pokazał.
        [
        [(235, 3), (543, 1), (2111, 7), (2326, 2)],
        [(174, 1), (235, 2), (2326, 12), (2562, 6)],
        [(102, 3), (222, 1), (235, 7)]
        ]
         */
        // [(102, 1), (174, 1), (222, 1),
        String[] fileNames = new File("files").list();

        //przechowujemy więc tablicę par (IdPliku, Dotychczasowo zaobserwowana największa liczba indeksów w których wystąpiło od)
        Pair<Integer, Integer>[] fileIdsOccurrences = new Pair[30];
        int fileIdsOccurrencesIndex = 0;
        for(int i = 0; i < fileIdsOccurrences.length; i++)
            fileIdsOccurrences[i] = new Pair<>(i, 0);

        int[] keyWordPointers = new int[words.length]; //każde słowo kluczowe ma własny niezależny wskaźnik idący po też własnym indeksie

        int tmp;

        //Warunkiem wyjścia z pętli jest dojście każdego wskaźnika do końca indeksu
        boolean exit = false;

        int minFileId, minFilePointerIndex = -1, finishedKeywordPointers = 0;

        for(int i = 0; i < reverseIndices.length; i++)
            if(reverseIndices[i].length == 0){
                keyWordPointers[i] = -1;
                finishedKeywordPointers++;
            }

        while (finishedKeywordPointers < keyWordPointers.length) {
            minFileId = Rozmiar.MAX_ELEM;
            for(int j = 0; j < keyWordPointers.length; j++) {                            //Szukanie najmniejszego id pliku spośród par wskazywanych przez indeksy
                if (keyWordPointers[j] != -1) {
                    if (reverseIndices[j][keyWordPointers[j]].getValue0() < minFileId) {
                        minFileId = reverseIndices[j][keyWordPointers[j]].getValue0();
                        minFilePointerIndex = j;
                    }
                }
            }

            if(fileIdsOccurrencesIndex == 0) {
                fileIdsOccurrences[fileIdsOccurrencesIndex++] = new Pair<>(minFileId, 1);
            } else if(fileIdsOccurrences[fileIdsOccurrencesIndex - 1].getValue0() == minFileId) {
                fileIdsOccurrences[fileIdsOccurrencesIndex - 1] = fileIdsOccurrences[fileIdsOccurrencesIndex - 1].setAt1(fileIdsOccurrences[fileIdsOccurrencesIndex - 1].getValue1() + 1);
            } else if(finishedKeywordPointers != keyWordPointers.length - 1) {
                fileIdsOccurrences[fileIdsOccurrencesIndex++] = new Pair<>(minFileId, 1);
            }

            if(++keyWordPointers[minFilePointerIndex] > reverseIndices[minFilePointerIndex].length - 1) {
                keyWordPointers[minFilePointerIndex] = -1;
                finishedKeywordPointers++;
            }

        }

        Arrays.sort(fileIdsOccurrences, (pair2, pair1) -> pair1.getValue1() - pair2.getValue1());
        fileIdsOccurrences = Arrays.copyOf(fileIdsOccurrences, Math.min(fileIdsOccurrencesIndex, n));

        String[] res = new String[fileIdsOccurrences.length];

        for (int i = 0 ; i < res.length; i++)
            res[i] = fileIdsOccurrences[i].getValue1() + " " + getFileName(fileIdsOccurrences[i].getValue0());
        return res;
    }
    public Pair<String,Double>[] getDocsClosestToProfileOld(int n, String profileName) {
        int fileCount = new File("files").listFiles().length;

        File profileFile = new File("profiles/" + profileName + ".txt");
        String[] profileKeyWords = readProfile(profileFile.getName());

        Pair<Integer, Integer>[][] reverseIndices = new Pair[profileKeyWords.length][]; //odwrócone indeksy w postaci podtablic par reprezentujących każdy z plików [pary (plik, liczebność słowa)]
        /* Wierszy jest tyle co słów kluczowych (profile.length). We wszystkich wierszach (dla wszystkich słów kluczowych) liczba par powinna być zbliżona do liczby plików tekstowych.
        [
        [(235, 3), (543, 1), (3424, 7)],
        [(174, 1), (629, 2), (2326, 12), (2562, 6)],
        [(235, 3), (543, 1), (3424, 7)]
        ]
         */
        //czej:
        int l = 0;
        for(String keyWord : profileKeyWords) {
            reverseIndices[l] = readIndex(new File("indices/reverseIndices/" + profileKeyWords[l] + ".idx"));

            /*System.out.print("(" + keyWord + "): [");
            for(Pair<Integer, Integer> pair : reverseIndices[l])
                System.out.print(pair + " ");
            System.out.println("]");*/

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
            for(String keyWord : profileKeyWords) { //2
                for(Pair<Integer, Integer>[] reverseIndex : reverseIndices) { //3
                    for(Pair<Integer, Integer> pair : reverseIndex) {
                        if (Objects.equals(pair.getValue0(), getFileName(i))) {
                            logResult = Math.log10(pair.getValue1());
                            //System.out.println(logResult);
                            if (pairs[i] == null) {
                                pairs[i] = new Pair<>(getFileName(pair.getValue0()), logResult);
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
}
