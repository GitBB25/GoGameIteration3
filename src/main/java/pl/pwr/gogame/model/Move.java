// reprezentacja ruchu w grze Go
// Wzorzec:
package pl.pwr.gogame.model;

/**
 * Klasa {@code Move} reprezentuje pojedynczy ruch w grze Go.
 * Przechowuje informację o pozycji, na której wykonywany jest ruch,
 * oraz o graczu, który ten ruch wykonuje.
 */
public class Move {

    /**
     * Pozycja na planszy, na której wykonywany jest ruch.
     */
    private final Position position;

    /**
     * Gracz wykonujący ruch.
     */
    private GamePlayer player;

    /**
     * Tworzy nowy ruch dla podanej pozycji i gracza.
     *
     * @param position pozycja ruchu na planszy (nie może być {@code null})
     * @param player gracz wykonujący ruch
     * @throws IllegalArgumentException jeśli pozycja jest {@code null}
     */
    public Move(Position position, GamePlayer player) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        this.position = position;
        this.player = player;
    }

    /**
     * Zwraca pozycję ruchu.
     *
     * @return pozycja na planszy
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Zwraca gracza wykonującego ruch.
     *
     * @return gracz
     */
    public GamePlayer getPlayer() {
        return player;
    }
}
