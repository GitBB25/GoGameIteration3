package pl.pwr.gogame.model;

/**
 * Klasa {@code ScoreResult} reprezentuje końcowy wynik gry Go.
 * Przechowuje punktację obu graczy oraz informację o zwycięzcy.
 */
public class ScoreResult {

    /**
     * Wynik punktowy gracza czarnego.
     */
    private final int blackScore;

    /**
     * Wynik punktowy gracza białego.
     */
    private final int whiteScore;

    /**
     * Zwycięzca gry. Może być {@code null} w przypadku remisu.
     */
    private final GamePlayer winner;

    /**
     * Tworzy obiekt wyniku gry.
     *
     * @param blackScore liczba punktów gracza czarnego
     * @param whiteScore liczba punktów gracza białego
     * @param winner zwycięzca gry lub {@code null} w przypadku remisu
     */
    public ScoreResult(int blackScore, int whiteScore, GamePlayer winner) {
        this.blackScore = blackScore;
        this.whiteScore = whiteScore;
        this.winner = winner;
    }

    /**
     * Zwraca wynik gracza czarnego.
     *
     * @return liczba punktów czarnego gracza
     */
    public int getBlackScore() {
        return blackScore;
    }

    /**
     * Zwraca wynik gracza białego.
     *
     * @return liczba punktów białego gracza
     */
    public int getWhiteScore() {
        return whiteScore;
    }

    /**
     * Zwraca zwycięzcę gry.
     *
     * @return zwycięzca gry lub {@code null} w przypadku remisu
     */
    public GamePlayer getWinner() {
        return winner;
    }
}
