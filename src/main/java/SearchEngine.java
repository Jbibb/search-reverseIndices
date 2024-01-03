/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;

import java.io.*;
import java.util.*;

public class SearchEngine {
    public static final boolean UPDATE_INDICES_FLAG = false;
    private MDictionary fileNamesDictionary;
    private Pair<Integer, String>[] fileIdsAndNamesArray;

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
            text = text.replaceAll("[^\\x00-\\x7FąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+", "");
            split = text.split("[^a-zA-Z0-9ąćęłńóśżźĄĆŁŃÓĘŚŻŹ]+");
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
    }

    private void updateIndex(File fileEntry, String word) {
       //to do
    }



    /**
     * Tworzy pliki indeksowe dla słów kluczowych: w każdym wierszu jest id pliku oraz liczba wystąpień słowa w pliku
     */
    public void makeReverseIndices() {
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

        for(Pair<Integer, String> fileIdAndName : getFileIdAndNames()) {
            try (BufferedReader br = new BufferedReader(new FileReader("indices/" + fileIdAndName.getValue1() + ".idx"))) {
                while (br.ready()) {
                    lineValues = br.readLine().split(" ");
                    File indexFile = new File("indices/reverseIndices/" + lineValues[0] + ".idx");
                    indexFile.createNewFile();
                    PrintWriter pw = new PrintWriter(new FileOutputStream(indexFile, true));
                    pw.println(fileIdAndName.getValue0() + " " + lineValues[1]);
                    pw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Pair<Integer, String>[] getFileIdAndNames(){
        //ID plików w plikach indeksowych powinny być posortowane rosnąco ID
        //przypiszmy każdemu plikowi numer ID
        File[] files = new File("files").listFiles();
        MDictionary dictionary = new MDictionary();
        Pair<Integer, String>[] fileIdsAndNames = new Pair[files.length];
        int i = 0;

        String fileName;
        for(File file : files) {
            fileName = file.getName().split(".txt")[0];
            fileIdsAndNames[i++] = new Pair<>(dictionary.Add(fileName), fileName); //sama nazwa pliku, bez rozszerzenia
        }
        Arrays.sort(fileIdsAndNames, (pair1, pair2) -> pair1.getValue0() - pair2.getValue0());

        fileNamesDictionary = dictionary;
        fileIdsAndNamesArray = fileIdsAndNames;
        return fileIdsAndNames;
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
        int f = 0, l = fileIdsAndNamesArray.length - 1;

        while (f <= l) {
            int m = f + (l - f) / 2;

            if (fileIdsAndNamesArray[m].getValue0() == id) {
                return fileIdsAndNamesArray[m].getValue1();
            } else if (fileIdsAndNamesArray[m].getValue0() < id) {
                f = m + 1;
            } else {
                l = m - 1;
            }
        }

        return null;
    }

    private int getOrderNumberOfFile(int fileId){
        int f = 0, l = fileIdsAndNamesArray.length - 1;

        while (f <= l) {
            int m = f + (l - f) / 2;

            if (fileIdsAndNamesArray[m].getValue0() == fileId) {
                return m;
            } else if (fileIdsAndNamesArray[m].getValue0() < fileId) {
                f = m + 1;
            } else {
                l = m - 1;
            }
        }
        return -1;
    }


    /**
     * Zwraca pliki zawierające wszystkie podane słowa
     * @param words wyszukiwane słowa
     * @return
     */
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

    public String[] getDocsContainingWords(String[] words){
        Pair<Integer, Integer>[][] reverseIndices = new Pair[words.length][]; //odwrócone indeksy w postaci podtablic par reprezentujących każdy z plików [pary (plik, liczebność słowa)]
        /* Wierszy jest tyle co słów kluczowych (profile.length). We wszystkich wierszach (dla wszystkich słów kluczowych) liczba par powinna (chyba) być zbliżona do liczby plików tekstowych.
        Pary są posortowane rosnąco id plików. (pierwsza wartość w parze)
        [
        [(235, 3), (543, 1), (3424, 7)],
        [(174, 1), (235, 2), (2326, 12), (2562, 6)],
        [(235, 3), (543, 1), (3424, 7)]
        ]
         */

        int l = 0;
        for(String keyWord : words) {
            reverseIndices[l] = readIndex(new File("indices/reverseIndices/" + words[l] + ".idx"));

            /*System.out.print("(" + keyWord + "): [");
            for(Pair<Integer, Integer> pair : reverseIndices[l])
                System.out.print(pair + " ");
            System.out.println("]");*/

            l++;
        }

        int[] keyWordPointers = new int[words.length]; //każde słowo kluczowe ma własny niezależny wskaźnik idący po też własnym indeksie

        String[] res = new String[30];
        int resIndex = 0;

        int minFileId, minFilePointerIndex = -1;
        boolean fileMatchesAllKeywords, exit = false;

        //warunkiem wyjścia z pętli ma być dotarcie wskaźnika po najmniejszym odwróconym indeksie do końca <- to nie jest prawda i nie wiem czy wgl zostaje
        //koniec pętli jest wtedy gdy jakikolwiek wskaźnik dotrze do końca swojej tablicy (indeksu)

        int minReverseIndexLength = Rozmiar.MAX_WORD;
        int minReverseIndexPointerIndex = -1;

        for(int i = 0; i < reverseIndices.length; i++)
            if(reverseIndices[i].length < minReverseIndexLength) {        //szukanie najkrótszego indeksu. nwm czy to potrzebne
                minReverseIndexLength = reverseIndices[i].length;
                minReverseIndexPointerIndex = i;
            }

        if(minReverseIndexLength == 0)
            exit = true;

        while (!exit) {
            minFileId = Rozmiar.MAX_ELEM;
            for(int j = 0; j < keyWordPointers.length; j++)                            //Szukanie najmniejszego id pliku spośród par wskazywanych przez indeksy
                if(reverseIndices[j][keyWordPointers[j]].getValue0() < minFileId) {
                    minFileId = reverseIndices[j][keyWordPointers[j]].getValue0();
                    minFilePointerIndex = j;
                }

            fileMatchesAllKeywords = true;
            for(int j  = 0; j < reverseIndices.length && fileMatchesAllKeywords; j++) { //Sprawdzenie czy wszystkie wskaźniki wskazują na takie samo id pliku
                if(reverseIndices[j][keyWordPointers[j]].getValue0() != minFileId) {
                    fileMatchesAllKeywords = false;
                }
            }

            if (!fileMatchesAllKeywords) {                       //Inkrementacja najmniejszego wskaźnika}
                if (++keyWordPointers[minFilePointerIndex] >= reverseIndices[minFilePointerIndex].length - 1) //!!!TU POTENCJALNIE POWINIEN BYĆ > ZAMIAST >=?
                    exit = true;
            } else {                                             //...albo dodanie nazwy pliku do rezultatu
                if (resIndex >= res.length)
                    res = Arrays.copyOf(res, (int) (res.length * 1.25));
                res[resIndex++] = getFileName(minFileId);

                for(int j = 0; j < keyWordPointers.length; j++) { // oraz inkrementacja wszystkich wskaźników
                    if(keyWordPointers[j]++ >= reverseIndices[j].length - 1) // sprawdzenie czy odwrócony indeks nie został wyczerpany //!!!TU POTENCJALNIE POWINIEN BYĆ > ZAMIAST >=?
                        exit = true;
                }
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
    public String[] getDocsWithMaxMatchingWordsOld(String[] words, int n) {
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

    public String[] getDocsWithMaxMatchingWords(String[] words, int n) {
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

        //przechowujemy więc tablicę par (IdPliku, Dotychczasowo zaobserwowana największa liczba indeksów w których wystąpiło od)
        Pair<Integer, Integer>[] fileIdsOccurrences = new Pair[fileIdsAndNamesArray.length];
        int fileIdsOccurrencesIndex = 0;
        for(int i = 0; i < fileIdsOccurrences.length; i++)
            fileIdsOccurrences[i] = new Pair<>(fileIdsAndNamesArray[i].getValue0(), 0);

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
// 1432 1,
    /**
     * Zwrócenie n dokumentów z największą zgodnościa z wybranym profilem
     * @param n
     * @return
     */
    public Pair<String,Double>[] getDocsClosestToProfile(int n, String profileName) {
                /*
        Powtórka, ze zliczania, tylko teraz liczy się druga wartość w parach, liczebność wyrazu w pliku.

        Może być identyczne podejście, tylko zamiast inkrementacji o 1, dodawany byłby log(n) do jednej wspólnej sumy.
        [
        [(235, 3), (543, 1), (2111, 7), (2326, 2)],
        [(174, 1), (235, 2), (2326, 12), (2562, 6)],
        [(102, 3), (222, 1), (235, 7)]
        ]
         */
        String[] keyWords = readProfile(profileName + ".txt");
        Pair<Integer, Integer>[][] reverseIndices = new Pair[keyWords.length][];

        int l = 0;


        for(String keyWord : keyWords) {
            reverseIndices[l] = readIndex(new File("indices/reverseIndices/" + keyWords[l] + ".idx"));

            /*System.out.print("(" + keyWord + "): [");
            for(Pair<Integer, Integer> pair : reverseIndices[l])
                System.out.print(pair + " ");
            System.out.println("]");*/
            l++;
        }

        //przechowujemy więc tablicę par (IdPliku, suma logarytmów)
        Pair<Integer, Double>[] fileIdsOccurrences = new Pair[fileIdsAndNamesArray.length];
        int fileIdsOccurrencesIndex = 0;
        for(int i = 0; i < fileIdsOccurrences.length; i++)
            fileIdsOccurrences[i] = new Pair<>(fileIdsAndNamesArray[i].getValue0(), 0d);

        int[] keyWordPointers = new int[keyWords.length]; //każde słowo kluczowe ma własny niezależny wskaźnik idący po też własnym indeksie

        int tmp;
        double roundedLogResult;

        //Warunkiem wyjścia z pętli jest dojście każdego wskaźnika do końca indeksu

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
                roundedLogResult = Math.log10(reverseIndices[minFilePointerIndex][keyWordPointers[minFilePointerIndex]].getValue1());
                //System.out.println("log z " + reverseIndices[minFilePointerIndex][keyWordPointers[minFilePointerIndex]].getValue1() + " = " + roundedLogResult);
                fileIdsOccurrences[fileIdsOccurrencesIndex++] = new Pair<>(minFileId, roundedLogResult);
            } else if(fileIdsOccurrences[fileIdsOccurrencesIndex - 1].getValue0() == minFileId) {
                roundedLogResult = Math.log10(reverseIndices[minFilePointerIndex][keyWordPointers[minFilePointerIndex]].getValue1());
                //System.out.println("log z " + reverseIndices[minFilePointerIndex][keyWordPointers[minFilePointerIndex]].getValue1() + " = " + roundedLogResult);
                fileIdsOccurrences[fileIdsOccurrencesIndex - 1] = fileIdsOccurrences[fileIdsOccurrencesIndex - 1].setAt1(fileIdsOccurrences[fileIdsOccurrencesIndex - 1].getValue1() + roundedLogResult);
            } else if(finishedKeywordPointers != keyWordPointers.length - 1) {
                roundedLogResult = Math.log10(reverseIndices[minFilePointerIndex][keyWordPointers[minFilePointerIndex]].getValue1());
                //System.out.println("log z " + reverseIndices[minFilePointerIndex][keyWordPointers[minFilePointerIndex]].getValue1() + " = " + roundedLogResult);
                fileIdsOccurrences[fileIdsOccurrencesIndex++] = new Pair<>(minFileId, roundedLogResult);
            }

            if(++keyWordPointers[minFilePointerIndex] > reverseIndices[minFilePointerIndex].length - 1) {
                keyWordPointers[minFilePointerIndex] = -1;
                finishedKeywordPointers++;
            }

        }

        Arrays.sort(fileIdsOccurrences, (pair2, pair1) -> Double.compare(pair1.getValue1(), pair2.getValue1()));
        fileIdsOccurrences = Arrays.copyOf(fileIdsOccurrences, Math.min(fileIdsOccurrencesIndex, n));

        Pair<String, Double>[] res = new Pair[fileIdsOccurrences.length];
        double roundedDivisionResult;
        for (int i = 0 ; i < res.length; i++) {
            roundedDivisionResult = Math.round((fileIdsOccurrences[i].getValue1() / keyWords.length) * 1000) / 1000d;
            //System.out.println("dzielenie " + fileIdsOccurrences[i].getValue1() + " na " + keyWords.length + " = " + roundedDivisionResult);
            res[i] = new Pair<>(getFileName(fileIdsOccurrences[i].getValue0()), roundedDivisionResult * 100);
        }

        return res;

    }
    public Pair<String,Double>[] getDocsClosestToProfileOld(int n, String profileName) {
        int fileCount = fileIdsAndNamesArray.length;

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
                        if (Objects.equals(pair.getValue0(), fileIdsAndNamesArray[i].getValue0())) {
                            logResult = Math.log10(pair.getValue1());
                            System.out.println(logResult);
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
}
