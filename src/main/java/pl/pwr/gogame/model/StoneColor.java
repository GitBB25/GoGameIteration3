package pl.pwr.gogame.model;

/**
 * Enum {@code StoneColor} reprezentuje możliwe kolory kamieni
 * w grze Go.
 *
 * Wzorzec projektowy: Enum.
 */
// Reprezentacja koloru kamienia w grze Go
// Wzorzecz: Enum
public enum StoneColor {

    /**
     * Puste pole planszy.
     */
    EMPTY,

    /**
     * Kamień gracza czarnego.
     */
    BLACK,

    /**
     * Kamień gracza białego.
     */
    WHITE;

    /**
     * Zwraca przeciwny kolor kamienia.
     * Dla {@link #BLACK} zwraca {@link #WHITE},
     * dla {@link #WHITE} zwraca {@link #BLACK},
     * a dla {@link #EMPTY} zwraca {@link #EMPTY}.
     *
     * @return przeciwny kolor kamienia
     */
    public StoneColor other() {
        if (this == BLACK) return WHITE;
        if (this == WHITE) return BLACK;
        return EMPTY;
    }
}
