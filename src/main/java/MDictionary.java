/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;

public class MDictionary {
    // to do
    private int size, amountWithCountMoreThanZero;
    private Pair<String, Integer>[] array;

    public MDictionary() {
        size = 0;
        amountWithCountMoreThanZero = 0;
        array = new Pair[Rozmiar.MAX_ELEM];
    }

    /**
     Opróżnia słownik i zwalnia pamięć po kolekcjach słownikowych
     // <remarks>Metoda przydatna na zakończenie Dictu lub przed ponownym załadowaniem</remarks>
     **/
    public void Empty() {
       size = 0;
       array = new Pair[Rozmiar.MAX_ELEM];
    }

    /**
     Metoda zeruje liczbę wystąpień pojęć w słowniku
    **/
    public void Reset()
    {
        amountWithCountMoreThanZero = 0;
        for(int i = 0; i < array.length; i++)
            if(array[i] != null)
                array[i] = array[i].setAt1(0);
    }

    /**
    Dodanie pojęcia do słownika na podstawie słowa i numeru klucza haszowego
    **/
    private int Add(String W, int h)
    {
        if(array[h] == null) {
            array[h] = new Pair<>(W, 1);
            size++;
            amountWithCountMoreThanZero++;
        } else if(array[h].getValue0().equals(W))
            array[h] = array[h].setAt1(array[h].getValue1() + 1);
        else {
            do {
                h = (h + 1) % Rozmiar.MAX_ELEM;
            } while (array[h] != null);
            array[h] = new Pair<>(W, 1);
            size++;
            amountWithCountMoreThanZero++;
        } return h;
    }

    /**
    /// Dodanie pojęcia do słownika na podstawie słowa
    **/
    public int Add(String W) {
        return Add(W, Haszuj(W));
    }

    /**
     Podaje klucz dla danego słowa
    **/
    private int Haszuj(String W) {
        int hash = Rozmiar.PIERWSZA;
        for (int i = 0; i < W.length(); i++) {
            hash = hash*31 + W.charAt(i);
        }
        return Math.abs(hash % array.length);
    }

    /**
    Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień
    **/
    private int Find(String W, int h) {
        if(array[h] == null)
            return 0;
        else if(array[h].getValue0().equals(W)) {
            if(array[h].getValue1() == 0)
                amountWithCountMoreThanZero++;
            array[h] = array[h].setAt1(array[h].getValue1() + 1);
            return h;
        }
        int index = h;
        do {
            index++;
            if(array[index] == null)
                break;
        } while (!array[index].getValue0().equals(W));
        if(array[index] == null)
            return 0;
        if(array[h].getValue1() == 0)
            amountWithCountMoreThanZero++;
        array[index] = array[index].setAt1(array[h].getValue1() + 1);
        return index;
    }

    /**
     Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień o n
     **/
    public int FindAndAdd(String W, int n)
    {
        int h = Haszuj(W);
        if(array[h] == null) {
            array[h] = new Pair<>(W, n);
            amountWithCountMoreThanZero++;
            size++;
            return h;
        } else if(array[h].getValue0().equals(W)) {
            array[h] = array[h].setAt1(array[h].getValue1() + n);
            return h;
        }

        int index = h;
        do {
            index++;
            if(array[index] == null)
                break;
        } while (!array[index].getValue0().equals(W));
        if(array[index] == null) {
            array[index] = new Pair<>(W, n);
            amountWithCountMoreThanZero++;
            size++;
            return index;
        }
        array[index] = array[index].setAt1(array[index].getValue1() + n);
        return index;
    }

    /** <summary>
    Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień
    **/
    public int Find(String W) {
        return Find(W, Haszuj(W));
    }

    /**
     * Zwraca słowa w słowniku
     */
    public String[] getWords() {
        String[] res = new String[size];
        int resIndex = 0;
        for(int i = 0; i < array.length; i++)
            if(array[i] != null)
                res[resIndex++] = array[i].getValue0();
        return res;
    }

    /**
     * Zwraca słowa, które wystąpiły w dokumencie
     * @return
     */
    public String[] getAppearedWords() {
        int resIndex = 0;
        String[] res = new String[amountWithCountMoreThanZero];
        for(int i = 0; i < array.length; i++)
            if(array[i] != null)
                if(array[i].getValue1() > 0)
                    res[resIndex++] = array[i].getValue0();
        return res;
    }

    /**
     * Zwraca pojęcia, które wystąpiły oraz liczba wystąpień
     * @return
     */
    public Pair<String, Integer>[] getAppearedWordsWithCount() {
        int resIndex = 0;
        Pair<String,Integer>[] res = new Pair[amountWithCountMoreThanZero];
        for(int i = 0; i < array.length; i++)
            if(array[i] != null)
                if(array[i].getValue1() > 0)
                    res[resIndex++] = new Pair<>(array[i].getValue0(), array[i].getValue1());
        return res;
    }

    @Override
    public String toString() {
        String res = "";
        for(int i = 0; i < array.length; i++)
            if(array[i] != null)
                res += i + " = '" + array[i].getValue0() + "' - wystapienia: " + array[i].getValue1() + "\n";
        return res;
    }
}
