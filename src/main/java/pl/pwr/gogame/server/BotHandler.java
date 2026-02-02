package pl.pwr.gogame.server;

import java.net.Socket;
import java.util.Random;

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.Position;

public class BotHandler extends ClientHandler {
    private final Random random = new Random();

 public BotHandler(Socket socket,GameEngine engine, GamePlayer botPlayer, Board board) {
    super(socket, engine, botPlayer, board); // Bot nie potrzebuje socketu, więc przekazujemy `null`
}
    @Override
    public void run() {
        System.out.println("Bot dołączył do gry jako " + player.getName());
        while (!engine.isEnd()) {
             //System.out.println("BOT: Aktualny gracz to: " + engine.getCurrentPlayer().getName());
            if (engine.getCurrentPlayer().equals(player) ) {
                makeMove();
            }
            try {
                Thread.sleep(1000); // Bot wykonuje ruch co sekundę
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    protected void handleDisconnect() {
        // Bot nie wymaga obsługi rozłączenia
        System.out.println("BOT: Rozłączenie nie wymaga obsługi.");
    }

private Position lastMove = null; // Przechowuje ostatnią pozycję bota
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
                move = pos;
                break;
            }
        }
    }

    // Jeśli nie znaleziono pola obok, szukaj pierwszego pustego pola na planszy
    if (move == null) {
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                Position pos = new Position(col, row);
                if (board.isEmpty(pos)) {
                    move = pos;
                    break;
                }
            }
            if (move != null) break;
        }
    }

    System.out.println("BOT: Wybrany ruch: " + (move != null ? move : "PASS"));
    if (move != null) {
        Move botMove = new Move(move, player);
        MoveResult result = engine.applyMove(botMove);
        if (result.isOk()) {
            lastMove = move; // Zaktualizuj ostatnią pozycję
            sendMove(botMove, result);
            if (opponent != null) {
                opponent.sendMove(botMove, result);
            }
            ClientHandler current = engine.getCurrentPlayer() == player ? this : opponent;
            ClientHandler waiting = current == this ? opponent : this;

            current.send("YOUR_TURN");
            waiting.send("OPPONENT_TURN");
        }
    } else {
        MoveResult result = engine.pass(player);
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