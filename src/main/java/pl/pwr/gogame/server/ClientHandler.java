//Client Handler
// sluzy do obslugi klienta w serwerze gry Go
package pl.pwr.gogame.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;

import pl.pwr.gogame.ApplicationContextProvider;
import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.Position;
import pl.pwr.gogame.model.ScoreResult;
import pl.pwr.gogame.persistence.entity.GameEntity;
import pl.pwr.gogame.persistence.service.GamePersistenceService;

/**
 * Klasa {@code ClientHandler} odpowiada za obsługę pojedynczego klienta
 * po stronie serwera gry Go. Każda instancja działa w osobnym wątku
 * i przetwarza komunikację pomiędzy serwerem a jednym graczem.
 *
 * Odpowiada za:
 * <ul>
 *   <li>odbieranie komend od klienta</li>
 *   <li>delegowanie logiki do {@link GameEngine}</li>
 *   <li>wysyłanie komunikatów do klienta oraz przeciwnika</li>
 *   <li>synchronizację stanu gry pomiędzy graczami</li>
 * </ul>
 */
public class ClientHandler implements Runnable {

    private volatile boolean running = true;

    public final GamePersistenceService persistenceService;
    protected final GameEntity gameEntity;
    private int moveNumber = 0;

    /**
     * Gniazdo sieciowe połączone z klientem.
     */
    private final Socket socket;

    /**
     * Silnik gry zarządzający logiką rozgrywki.
     */
    protected final GameEngine engine;

    /**
     * Gracz obsługiwany przez ten handler.
     */
    protected final GamePlayer player;

    /**
     * Plansza gry.
     */
    private final Board board;

    /**
     * Handler przeciwnika.
     */
    protected ClientHandler opponent;

    /**
     * Strumień wyjściowy do wysyłania danych do klienta.
     */
    private PrintWriter out;

    //potrzebna flaga by pierwszy gracz nie mógł robić ruchów zanim drugi nie dołączy 
    boolean gameStarted = false;

    // flaga zapobiegająca wielokrotnemu wysyłaniu komunikatu o zakończeniu gry
    private boolean gameEndNotified = false;

    /**
     * Tworzy nowy {@code ClientHandler}.
     *
     * @param socket gniazdo klienta
     * @param engine silnik gry
     * @param player gracz
     * @param board plansza gry
     */
    public ClientHandler(Socket socket, GameEngine engine, GamePlayer player, Board board, GamePersistenceService persistenceService, GameEntity gameEntity) {
        this.socket = socket;
        this.engine = engine;
        this.player = player;
        this.board = board;
        this.persistenceService = persistenceService;
        this.gameEntity = gameEntity;
    }

    /**
     * Główna pętla wątku obsługującego klienta.
     * Odbiera komendy z gniazda i przekazuje je do obsługi.
     */
    @Override
    public void run() {
        try (Scanner in = new Scanner(socket.getInputStream());
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            this.out = writer;

            // Używamy nowej klasy do sformatowania powitania
            send(ResponseFormatter.formatWelcome(board));
            sendConfigBoardsize();

            // Jeśli przeciwnik jeszcze nie dołączył — poinformuj gracza, że czekamy
            if (opponent == null) {
                send("OCZEKIWANIE: Oczekiwanie na dołączenie przeciwnika...");
            }

            while (running && in.hasNextLine()) {
                String command = in.nextLine();
                handleCommand(command);
            }

        } catch (IOException e) {
            System.err.println("Gracz rozłączony: " + player.getName());
        } finally {
            handleDisconnect();
        }
    }

    /**
     * Obsługuje pojedynczą komendę otrzymaną od klienta.
     *
     * @param command treść komendy
     */
    private void handleCommand(String command) {
        try {

            // Jeśli silnik zgłasza koniec gry, blokujemy wszystkie dalsze komendy
            if (engine.isEnd()) {
               
                // Wyślij komunikat o zakończeniu gry tylko raz na klienta
                if (!gameEndNotified) {
                    sendText("Gra zakończona.");
                    if (opponent != null) {
                        opponent.sendText("Gra zakończona.");
                    }

                    engine.setEnd(true); 
                    persistenceService.finishGame(gameEntity, engine.getWinner());

                    gameEndNotified = true;

                    shutdownHandler();

                    if (opponent != null) {
                    opponent.shutdownHandler();
                    }

                    SpringApplication.exit(
                        ApplicationContextProvider.getContext(),
                        () -> 0
                    );
                }
                return;
            }

            if (!gameStarted) {
                sendText("Gra jeszcze się nie rozpoczęła. Oczekiwanie na przeciwnika.");
                return;
            }

            command = command.trim();

            // negocjacja: oznaczanie pól/grup jako martwe
            if (command.toUpperCase().startsWith("NEGOTIATE_MARK")) {
                // format: NEGOTIATE_MARK col row
                String[] parts = command.split("\\s+");
                if (parts.length >= 3) {
                    try {
                        int col = Integer.parseInt(parts[1]);
                        int row = Integer.parseInt(parts[2]);
                        Position pos = new Position(col, row);
                        engine.markNegotiationPosition(player, pos);
                        // send raw protocol notifications so client can react to them directly
                        send("NEGOTATE_MARKED " + col + " " + row);
                        if (opponent != null) opponent.send("OPPONENT_NEGOTATE_MARKED " + col + " " + row);
                    } catch (NumberFormatException ex) {
                        sendText("BŁĄD: niepoprawne współrzędne negocjacji");
                    }
                } else {
                    sendText("BŁĄD: NEGOTIATE_MARK wymaga dwóch argumentów: kolumna wiersz");
                }
                return;
            }

            if (command.equalsIgnoreCase("NEGOTIATE_DONE")) {
                // gracz zakończył oznaczanie; sprawdź czy obie strony już skończyły
                boolean finished = engine.finishNegotiationFor(player);
                if (!finished) {
                    // czekamy na drugiego gracza - używamy raw protocol notifications
                    send("NEGOTIATE_WAITING Oczekiwanie na propozycję przeciwnika...");
                    if (opponent != null) opponent.send("OPPONENT_FINISHED_MARKING");
                    return;
                }

                // obie strony zakończyły oznaczanie - teraz silnik zastosował wyniki albo zdecydował o kontynuacji
                send("NEGOTIATE_DONE_ACK");
                if (opponent != null) opponent.send("OPPONENT_NEGOTIATE_DONE");

                if (engine.getPhase() == pl.pwr.gogame.model.GamePhase.FINISHED && engine.isEnd()) {
                    // negocjacja zakończona sukcesem - policz wynik
                    ScoreResult scores = engine.calculateScores();
                    String scoreMessage = ResponseFormatter.formatScores(scores);
                    sendText(scoreMessage);
                    if (opponent != null) opponent.sendText(scoreMessage);
                } else if (!engine.getLastNegotiationSucceeded()) {
                    // negocjacje nie przyniosły konsensusu - kontynuujemy grę
                    sendText("NEGOTIATION_FAILED: Brak zgody, gra trwa dalej.");
                    if (opponent != null) opponent.sendText("NEGOTIATION_FAILED: Brak zgody, gra trwa dalej.");

                    // Powiadom o turze - przywróć normalny przepływ tur
                    ClientHandler current = engine.getCurrentPlayer() == player ? this : opponent;
                    ClientHandler waiting = current == this ? opponent : this;
                    if (current != null) current.send("YOUR_TURN");
                    if (waiting != null) waiting.send("OPPONENT_TURN");
                }
                return;
            }

            // Obsługa rezygnacji
            if (command.equalsIgnoreCase("resign")) {
                MoveResult resignResult = engine.resign(player);
                if (opponent != null) {
                    opponent.sendResign(resignResult);
                }
                return;
            }

            if (command.equalsIgnoreCase("pass") || command.equals("pas")) {
                MoveResult result = engine.pass(player);

                if (result.isOk()) {
                    sendPass(player);
                    if (opponent != null) opponent.sendPass(player);
                }
                // Jeśli pass uruchomił negocjację
                if (result.isNegotiation()) {
                    send("NEGOTIATE_START");
                    if (opponent != null) opponent.send("NEGOTIATE_START");
                } else if (result.isEnd()) {
                    // Oboje gracze spasowali - KONIEC GRY I LICZENIE PUNKTÓW
                    ScoreResult scores = engine.calculateScores();
                    String scoreMessage = ResponseFormatter.formatScores(scores);

                    sendText(scoreMessage);
                    if (opponent != null) {
                        opponent.sendText(scoreMessage);
                    }
                } else {
                    notifyPlayers(null, null);
                    ClientHandler current = engine.getCurrentPlayer() == player ? this : opponent;
                    ClientHandler waiting = current == this ? opponent : this;

                    current.send("YOUR_TURN");
                    waiting.send("OPPONENT_TURN");
                }
                return;
            }

            // Parsowanie i wykonanie ruchu
            Move move = CommandParser.parseMove(command, this.player);
            MoveResult result = engine.applyMove(move);

            if (result.isOk()) {
                //Ruch wysyłamy do GUI funkcją sendMove 
                sendMove(move, result);
                if (opponent != null) {
                    opponent.sendMove(move, result);
                }

                persistenceService.saveMove(gameEntity, move, moveNumber++);
            }

            //Funkcją sendText wysyłamy ruch do terminala
            sendText(ResponseFormatter.formatMoveResult(result));

            if (result.isOk()) {
                notifyPlayers(null, null);
                ClientHandler current = engine.getCurrentPlayer() == player ? this : opponent;
                ClientHandler waiting = current == this ? opponent : this;

                current.send("YOUR_TURN");
                waiting.send("OPPONENT_TURN");
            }
        } catch (IllegalArgumentException e) {
            sendText("BŁĄD WEJŚCIA: " + e.getMessage());
        }
    }

    /**
     * Wysyła aktualny status gry do obu graczy.
     */
    private void notifyPlayers(Move move, MoveResult result) {
        String statusMsg = ResponseFormatter.formatStatus(
                engine.getCurrentPlayer(),
                engine.getCurrentColor()
        );
        sendText(statusMsg);

        if (opponent != null) {
            opponent.sendText(statusMsg);
        }
    }

    //W NetworkClient zczytujemy pierwszy wyraz z funkcji send,
    //co umożliwi poprawną aktualizację na planszy w GUI
    //w zależności od typu zdarzenia
    protected  void sendMove(Move move, MoveResult result) {
        //jak nie było stawiania kamienia, np. pas lub resign, kończymy funkcję
        if (result == null) return;

        System.out.println("WYSYŁANIE RUCHU DO KLIENTA: " + move.getPosition().col() + " " +
                           move.getPosition().row() + " " +
                           move.getPlayer().getColor());

        send("MOVE " + move.getPosition().col() + " " +
                     move.getPosition().row() + " " +
                     move.getPlayer().getColor());

        //wysyłanie listy przejętych kamieni
        List<Position> captured = result.getCapturedPositions();
        for (Position pos : captured) {
            send("CAPTURE " + pos.col() + " " + pos.row());
        }
    }

    //wysyłanie PASS do GUI lub terminala
    protected  void sendPass(GamePlayer player) {
        send("PASS " + player.getColor());
    }

    //wysyłanie RESIGN do GUI lub terminala
    private void sendResign(MoveResult result) {
        send("RESIGN " + result.getLoser().getColor() + " " + result.getWinner().getColor());
    }

    /** Wysyłanie tekstu do GUI lub terminala */
    private void sendText(String message) {
        send("TEXT " + message);
    }

    /**
     * Wysyła konfigurację rozmiaru planszy do klienta.
     */
    private void sendConfigBoardsize() {
        send("CONFIG BOARD_SIZE " + board.getSize());
    }

    /**
     * Obsługuje rozłączenie klienta.
     */
    private void handleDisconnect() {
        try {
            socket.close();
        } catch (IOException ignored) {}

        if (opponent != null) {
            opponent.send("Przeciwnik rozłączył się. Gra zakończona.");
            opponent.shutdownHandler();
        }
    }

    /**
     * Ustawia przeciwnika dla tego handlera
     * i inicjuje rozpoczęcie gry.
     *
     * @param opponent handler przeciwnika
     */
    public void setOpponent(Object opponent) {
        if (opponent instanceof ClientHandler) {
            this.opponent = (ClientHandler) opponent;
            this.gameStarted = true;
            if (this.opponent != null) {
                this.opponent.opponent = this;
                this.opponent.gameStarted = true;

                waitForOut();
                this.opponent.waitForOut();

                send("GAME_START");
                this.opponent.send("GAME_START");
                send("YOUR_TURN");
                this.opponent.send("OPPONENT_TURN");
            }
        } else if (opponent instanceof BotHandler) {
            this.gameStarted = true;
            send("GAME_START");
            send("YOUR_TURN"); // The player always starts first when playing against a bot
        }
    }

    /**
     * Oczekuje na inicjalizację strumienia wyjściowego {@link #out}.
     */
    protected void waitForOut() {
        int waited = 0;
        while (this.out == null && waited < 5000) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
            waited += 50;
        }
    }

    /**
     * Wysyła surową wiadomość tekstową do klienta.
     *
     * @param message treść wiadomości
     */
    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    protected void shutdownHandler() {
    running = false;
    try {
        socket.close();   // this will break Scanner.hasNextLine()
    } catch (IOException ignored) {}
    }

}