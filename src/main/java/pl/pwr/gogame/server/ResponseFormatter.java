package pl.pwr.gogame.server;

// Formatuje odpowiedzi serwera gry Go dla klienta CMD
// Wzorzec: Adapter

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.ScoreResult;
import pl.pwr.gogame.model.StoneColor;

/**
 * Klasa {@code ResponseFormatter} odpowiada za formatowanie komunikatów
 * wysyłanych z serwera gry Go do klienta.
 *
 * <p>
 * Realizuje wzorzec projektowy <b>Adapter</b>, tłumacząc obiekty logiki
 * domenowej (np. {@link MoveResult}, {@link ScoreResult}) na czytelne
 * komunikaty tekstowe.
 * </p>
 */
public class ResponseFormatter {

    /**
     * Formatuje komunikat powitalny wysyłany do klienta po połączeniu.
     * Zawiera aktualny stan planszy oraz podstawowe informacje
     * o sposobie sterowania grą.
     *
     * @param board aktualna plansza gry
     * @return sformatowany komunikat powitalny
     */
    public static String formatWelcome(Board board) {
        StringBuilder sb = new StringBuilder();
        sb.append("Aktualna plansza:").append(System.lineSeparator());
        sb.append(board.toString()).append(System.lineSeparator());
        sb.append("Uzycie: i j - np. '2 3' ustawi kamien na kolumnie 2, wierszu 3.");
        sb.append("Dostępne komendy: 'pass' (pas), 'resign' (poddanie się).");
        return sb.toString();
    }

    /**
     * Formatuje rezultat wykonanego ruchu.
     *
     * @param result wynik ruchu
     * @return komunikat tekstowy opisujący rezultat ruchu
     */
    public static String formatMoveResult(MoveResult result) {
        if (result.isOk()) {
            if (result.getCapturedPositions().isEmpty()) {
                return "Ruch poprawny.";
            }
            return "Ruch poprawny. Zbite kamienie: " + result.getCapturedPositions().size();
        } else {
            return "BŁĄD: " + result.getErrorMessage();
        }
    }

    /**
     * Formatuje komunikat informujący o aktualnej turze gry.
     *
     * @param nextPlayer gracz, który wykonuje następny ruch
     * @param nextColor kolor kamieni aktualnego gracza
     * @return komunikat statusowy
     */
    public static String formatStatus(GamePlayer nextPlayer, StoneColor nextColor) {
        String name = (nextPlayer != null) ? nextPlayer.getName() : "(brak)";
        return "Tura: " + name + " (" + nextColor + ")";
    }

    /**
     * Formatuje końcowe wyniki gry.
     *
     * @param scores wynik punktowy gry
     * @return komunikat podsumowujący grę
     */
    public static String formatScores(ScoreResult scores) {
        StringBuilder sb = new StringBuilder();
        sb.append("KONIEC GRY - PODSUMOWANIE:").append(System.lineSeparator());
        sb.append("TEXT Wynik czarnego: ")
          .append(scores.getBlackScore())
          .append(" punktów.")
          .append(System.lineSeparator());
        sb.append("TEXT Wynik białego: ")
          .append(scores.getWhiteScore())
          .append(" punktów.")
          .append(System.lineSeparator());

        GamePlayer winner = scores.getWinner();
        if (winner != null) {
            sb.append("TEXT Wygrywa: ")
              .append(winner.getName())
              .append(" (")
              .append(winner.getColor())
              .append(")");
        } else {
            sb.append("TEXT Remis!");
        }
        return sb.toString();
    }
}
