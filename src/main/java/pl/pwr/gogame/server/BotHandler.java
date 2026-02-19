package pl.pwr.gogame.server;

import java.net.Socket;
import java.util.Random;

import pl.pwr.gogame.model.Board; // Import the missing ScoreResult class
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.Position;
import pl.pwr.gogame.model.ScoreResult;
import pl.pwr.gogame.persistence.entity.GameEntity;
import pl.pwr.gogame.persistence.service.GamePersistenceService;

public class BotHandler extends ClientHandler {
    private final Random random = new Random();

    private volatile boolean running = true;

    @Override
    protected void shutdownHandler() {
    running = false;
    }

 public BotHandler(Socket socket,GameEngine engine, GamePlayer botPlayer, Board board, GamePersistenceService persistenceService, GameEntity gameEntity) {
    super(socket, engine, botPlayer, board, persistenceService, gameEntity); // Bot nie potrzebuje socketu, więc przekazujemy `null`
}
    @Override
    public void run() {
        System.out.println("Bot dołączył do gry jako " + player.getName());
        while (running && !engine.isEnd()) {
             //System.out.println("BOT: Aktualny gracz to: " + engine.getCurrentPlayer().getName());
             if (engine.getCurrentPlayer() == null) break;
            if (engine.getCurrentPlayer().equals(player) ) {
                makeMove();
            }
            try {
                Thread.sleep(1000); // Bot wykonuje ruch co sekundę
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

          ScoreResult scores = engine.calculateScores();
                    String scoreMessage = ResponseFormatter.formatScores(scores);

                    sendText(scoreMessage);
                    if (opponent != null) {
                        opponent.sendText(scoreMessage);
                    }
    }
    protected void handleDisconnect() {
        // Bot nie wymaga obsługi rozłączenia
        System.out.println("BOT: Rozłączenie nie wymaga obsługi.");
    }

private Position lastMove = null; // Przechowuje ostatnią pozycję bota
int attempts = 0; // Licznik prób znalezienia poprawnego ruchu
private void makeMove() {
    Board board = engine.getBoard();
    int size = board.getSize();
    Position move = null;
      if (lastMove == null) {
        while (true) {
            int col = random.nextInt(size);
            int row = random.nextInt(size);
            Position pos = new Position(col, row);
            if (board.isEmpty(pos)) {
                move = pos;
                break;
            }
        }
    }

    // Jeśli bot wykonał już ruch, szukaj pola obok ostatniego ruchu
    if (lastMove != null) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Kierunki: prawo, dół, lewo, góra

        // Losowe permutowanie kierunków
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }

        // Sprawdź losowe kierunki
        for (int[] dir : directions) {
            int newCol = lastMove.col() + dir[0];
            int newRow = lastMove.row() + dir[1];
            Position pos = new Position(newCol, newRow);
            if (newCol >= 0 && newCol < size && newRow >= 0 && newRow < size && board.isEmpty(pos)) {
                if(pos != lastMove){
                move = pos;
                System.out.println("BOT: Szukam pola obok ostatniego ruchu: " + lastMove + ", znalazłem: " + move);
                }
                break;
            }
        }
    }

    // Jeśli nie znaleziono pola obok, szukaj pierwszego pustego pola na planszy
    if (move == null) {
        while (true) {
            int col = random.nextInt(size);
            int row = random.nextInt(size);
            Position pos = new Position(col, row);
            if (board.isEmpty(pos)) { // Sprawdź, czy ruch jest poprawny
                move = pos;
                break;
            }
        }
    }

    System.out.println("BOT: Wybrany ruch: " + (move != null ? move : "PASS"));
    if (move != null) {
        Move botMove = new Move(move, player);
        MoveResult result = engine.applyMove(botMove);
        lastMove = move; // Zaktualizuj ostatnią pozycję
        if (result.isOk()) {
            attempts = 0; // Resetuj licznik prób po udanym ruchu
            persistenceService.saveMove(gameEntity, botMove, size);
            
            sendMove(botMove, result);
            if (opponent != null) {
                opponent.sendMove(botMove, result);
            }
            ClientHandler current = engine.getCurrentPlayer() == player ? this : opponent;
            ClientHandler waiting = current == this ? opponent : this;

            current.send("YOUR_TURN");
            waiting.send("OPPONENT_TURN");
        }
        else if (attempts < 10) {
            System.out.println("BOT: Ruch nie został zaakceptowany: " + result.getErrorMessage());
            System.out.println(attempts);
            attempts++;
        }
        else {
            ClientHandler current = engine.getCurrentPlayer() == player ? this : opponent;
            ClientHandler waiting = current == this ? opponent : this;
            current.send("YOUR_TURN");
            waiting.send("OPPONENT_TURN");
            engine.setEnd(running);
        }
    } else {
        System.out.println("BOT: Spasowałem.");
        engine.pass(player);
        sendPass(player);
    }
    }
@Override
protected void waitForOut() {
    try {
        System.out.println("BOT: Czekam 2 sekundy przed rozpoczęciem...");
        Thread.sleep(2000); // Bot czeka 2 sekundy
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.err.println("BOT: Wątek został przerwany podczas oczekiwania.");
    }
}
}