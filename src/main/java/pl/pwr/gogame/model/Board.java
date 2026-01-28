package pl.pwr.gogame.model;

/**
 * Klasa {@code Board} reprezentuje logiczną planszę do gry Go.
 * Przechowuje stan pól planszy oraz udostępnia metody do
 * odczytu i modyfikacji kamieni.
 *
 * Wzorzec projektowy: Information Expert.
 */
// Reprezentacja planszy do gry Go
// Wzorzec: Information Expert
public class Board {

    /**
     * Rozmiar planszy (liczba pól w jednym wymiarze).
     */
    private final int size;

    /**
     * Dwuwymiarowa tablica przechowująca stan planszy.
     * Każde pole zawiera informację o kolorze kamienia.
     */
    private final StoneColor[][] grid;

    //przechowywanie stanu planszy z poprzedniej tury
    //w celu sprawdzania warunku ko

    /**
     * Tworzy nową planszę o zadanym rozmiarze
     * i inicjalizuje wszystkie pola jako puste.
     *
     * @param size rozmiar planszy
     */
    public Board(int size) {
        this.size = size;
        this.grid = new StoneColor[size][size];
        initialize();
    }

    /**
     * Inicjalizuje planszę, ustawiając wszystkie pola
     * na wartość {@link StoneColor#EMPTY}.
     */
    private void initialize() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = StoneColor.EMPTY;
            }
        }
    }

    /**
     * Zwraca kamień znajdujący się na danej pozycji planszy.
     *
     * @param position pozycja na planszy
     * @return kolor kamienia na danej pozycji
     * @throws IllegalArgumentException jeśli pozycja znajduje się poza planszą
     */
    public StoneColor getStone(Position position) {
        if (isOutOfBounds(position)) throw new IllegalArgumentException("Poza planszą");
        return grid[position.row()][position.col()];
    }

    /**
     * Ustawia kamień o podanym kolorze na wskazanej pozycji planszy.
     *
     * @param position pozycja na planszy
     * @param stone kolor kamienia
     */
    public void setStone(Position position, StoneColor stone) {
        if (!isOutOfBounds(position)) grid[position.row()][position.col()] = stone;
    }

    /**
     * Usuwa kamień z podanej pozycji planszy,
     * ustawiając pole jako puste.
     *
     * @param position pozycja kamienia do usunięcia
     */
    public void removeStone(Position position) {
        setStone(position, StoneColor.EMPTY);
    }

    /**
     * Zwraca rozmiar planszy.
     *
     * @return rozmiar planszy
     */
    public int getSize() {
        return size;
    }

    /**
     * Sprawdza, czy dana pozycja znajduje się poza granicami planszy.
     *
     * @param position pozycja do sprawdzenia
     * @return {@code true} jeśli pozycja jest poza planszą, w przeciwnym razie {@code false}
     */
    public boolean isOutOfBounds(Position position) {
        return position.row() < 0 || position.row() >= size
                || position.col() < 0 || position.col() >= size;
    }

    /**
     * Sprawdza, czy dane pole planszy jest puste.
     *
     * @param position pozycja do sprawdzenia
     * @return {@code true} jeśli pole jest puste, {@code false} w przeciwnym razie
     */
    public boolean isEmpty(Position position) {
        if (isOutOfBounds(position)) return false;
        return grid[position.row()][position.col()] == StoneColor.EMPTY;
    }

    /**
     * Zwraca tekstową reprezentację planszy,
     * przydatną do debugowania i testów.
     *
     * @return reprezentacja planszy w postaci tekstu
     */
    @Override
    public String toString() {
        //lineSeparator printuje nam /n poprawnie w kazdym rodzaju terminala
        String ls = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int c = 0; c < size; c++) sb.append(String.format(" %2d", c));
        sb.append(ls);
        for (int r = 0; r < size; r++) {
            sb.append(String.format("%2d ", r));
            for (int c = 0; c < size; c++) {
                StoneColor p = grid[r][c];
                char ch = (p == StoneColor.BLACK) ? 'B'
                        : (p == StoneColor.WHITE) ? 'W' : '.';
                sb.append(String.format("  %c", ch));
            }
            sb.append(ls);
        }
        return sb.toString();
    }
}
