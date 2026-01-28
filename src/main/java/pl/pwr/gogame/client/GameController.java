package pl.pwr.gogame.client;

import pl.pwr.gogame.client.view.BoardCanvas;
import pl.pwr.gogame.client.view.GameView;

/**
 * Klasa {@code GameController} odpowiada za logikę sterującą grą
 * po stronie klienta. Łączy warstwę widoku ({@link GameView})
 * z komunikacją sieciową realizowaną przez {@link NetworkClient}
 * oraz obsługuje interakcje użytkownika.
 */
public class GameController {

    /**
     * Widok gry, z którym powiązany jest kontroler.
     */
    private final GameView view;

    /**
     * Klient sieciowy odpowiedzialny za komunikację z serwerem gry.
     */
    private final NetworkClient client;
    // czy jesteśmy w trybie negocjacji (oznaczanie martwych grup)
    private boolean negotiationMode = false;

    /**
     * Tworzy nowy kontroler gry.
     * Inicjalizuje klienta sieciowego oraz rejestruje obsługę
     * przycisków sterujących (Pass, Resign).
     *
     * @param view widok gry
     */
    public GameController(GameView view) {
        this.view = view;
        this.client = new NetworkClient(
                "localhost",
                58901,
                view, this
        );

        // pass and resign buttons
        view.getPassButton().setOnAction(e -> {
            client.send("pass");
        });

        view.getResignButton().setOnAction(e -> {
            client.send("resign");
        });

        // negotiate done button (hidden by default in view)
        view.getNegotiateDoneButton().setOnAction(e -> {
            // signal server that local player finished marking
            client.send("NEGOTIATE_DONE");
            // disable button to prevent double sends
            view.getNegotiateDoneButton().setDisable(true);
        });
    }

    /**
     * Rejestruje obsługę zdarzeń myszy na planszy gry.
     * Metoda mapuje współrzędne kliknięcia myszy na odpowiednie
     * pole planszy i wysyła wybrany ruch do serwera.
     */
    public void registerBoardHandlers() {
        BoardCanvas board = view.getBoardCanvas();
        board.setOnMouseClicked(e -> {
            int size = board.getSize();
            double cellWidth = board.getWidth() / size;
            double cellHeight = board.getHeight() / size;
            double offsetX = cellWidth / 2.0;
            double offsetY = cellHeight / 2.0;

            double relX = e.getX() - offsetX;
            double relY = e.getY() - offsetY;

            int col = (int) Math.round(relX / cellWidth);
            int row = (int) Math.round(relY / cellHeight);

            col = Math.max(0, Math.min(size - 1, col));
            row = Math.max(0, Math.min(size - 1, row));

            if (negotiationMode) {
                // jeśli kliknięto na kamień, zaznacz całą grupę lokalnie i wyślij wszystkie pozycje
                if (board.getStoneAt(col, row) != null) {
                    java.util.List<int[]> group = board.getGroupPositions(col, row);
                    for (int[] p : group) {
                        client.send("NEGOTIATE_MARK " + p[0] + " " + p[1]);
                        board.addNegotiationMark(p[0], p[1], true);
                    }
                } else {
                    // puste pole - podczas negocjacji kliknięcia są ignorowane
                    try {
                        view.getLogArea().appendText("Kliknij pole z kamieniem, aby zaznaczyć grupę.\n");
                    } catch (Exception ignored) {}
                }
            } else {
                client.send(col + " " + row);
            }
        });
    }

    /**
     * Włącz/wyłącz tryb negocjacji (oznaczanie martwych grup).
     */
    public void setNegotiationMode(boolean on) {
        this.negotiationMode = on;
        // pokaż/ukryj przycisk Done
        view.getNegotiateDoneButton().setDisable(!on);
        view.getNegotiateDoneButton().setVisible(on);
        // podczas negocjacji zablokuj pass i resign aby uniknąć konfliktów
        view.getPassButton().setDisable(on);
        view.getResignButton().setDisable(on);
        // odblokuj/zaklucz planszę dla klików jeśli plansza istnieje
        if (view.getBoardCanvas() != null) {
            // Enable board for marking when entering negotiation.
            // When leaving negotiation, do not change enabled state here —
            // turn messages (YOUR_TURN / OPPONENT_TURN) manage whether the
            // board should be enabled for regular moves.
            if (on) {
                // allow mouse interaction for marking during negotiation without changing visuals
                view.getBoardCanvas().setMouseTransparent(false);
            }
        }
    }

    /**
     * Zwraca, czy obecnie jesteśmy w trybie negocjacji.
     */
    public boolean isNegotiationMode() { return negotiationMode; }
}
