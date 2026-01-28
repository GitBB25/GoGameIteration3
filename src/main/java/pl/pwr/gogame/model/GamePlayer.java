// Reprezentacja gracza w grze Go

package pl.pwr.gogame.model;

/**
 * Klasa {@code GamePlayer} reprezentuje gracza w grze Go.
 * Jest to klasa modelu , przechowująca podstawowe
 * informacje o graczu, takie jak jego nazwa oraz kolor kamieni,
 * którymi gra.
 */
public class GamePlayer {

    /**
     * Nazwa gracza.
     */
    private final String name;

    /**
     * Kolor kamieni przypisany do gracza.
     */
    private final StoneColor color;

    /**
     * Tworzy nowego gracza o podanej nazwie i kolorze.
     * Jeśli nazwa jest pusta lub {@code null}, ustawiana jest
     * domyślna wartość {@code "Player"}.
     * Jeśli kolor jest {@code null}, ustawiany jest kolor {@link StoneColor#EMPTY}.
     *
     * @param name nazwa gracza
     * @param color kolor kamieni gracza
     */
    public GamePlayer(String name, StoneColor color) {
        this.name = (name == null || name.isBlank()) ? "Player" : name;
        this.color = color == null ? StoneColor.EMPTY : color;
    }

    /**
     * Zwraca nazwę gracza.
     *
     * @return nazwa gracza
     */
    public String getName() {
        return name;
    }

    /**
     * Zwraca kolor kamieni gracza.
     *
     * @return kolor gracza
     */
    public StoneColor getColor() {
        return color;
    }

    /**
     * Zwraca tekstową reprezentację gracza
     * w formacie: {@code nazwa(kolor)}.
     *
     * @return reprezentacja tekstowa gracza
     */
    @Override
    public String toString() {
        return name + "(" + color + ")";
    }
}
