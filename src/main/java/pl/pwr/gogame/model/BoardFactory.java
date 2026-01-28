package pl.pwr.gogame.model;

/**
 * Klasa {@code BoardFactory} jest fabryką odpowiedzialną za tworzenie
 * obiektów {@link Board} o dozwolonych rozmiarach planszy gry Go.
 */
// Fabryka tworząca plansze do gry Go
public class BoardFactory {

    /**
     * Tworzy nową planszę gry Go o zadanym rozmiarze.
     * Dozwolone są wyłącznie standardowe rozmiary planszy:
     * 9x9, 13x13 oraz 19x19.
     *
     * @param size rozmiar planszy
     * @return nowa instancja {@link Board}
     * @throws IllegalArgumentException jeśli rozmiar planszy jest niedozwolony
     */
    //Tworzymy tutaj board dla reszty aplikacji
    public static Board createBoard(int size) {
        //plansza może mieć tylko określone rozmiary
        if (size != 9 && size != 13 && size != 19) {
            throw new IllegalArgumentException("Dozwolone rozmiary planszy to: 9, 13, 19");
        }
        return new Board(size);
    }
}
