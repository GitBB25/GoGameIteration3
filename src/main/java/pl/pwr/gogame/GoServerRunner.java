package pl.pwr.gogame;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.BoardFactory;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.StoneColor;
import pl.pwr.gogame.persistence.entity.GameEntity;
import pl.pwr.gogame.server.BotHandler;
import pl.pwr.gogame.server.ClientHandler;

import pl.pwr.gogame.persistence.service.GamePersistenceService;

@Component
public class GoServerRunner {

    private final GamePersistenceService persistenceService;

    public GoServerRunner(GamePersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }


    @PostConstruct
    public void startServer() {
        new Thread(() -> {
            try {
                runServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void runServer() throws IOException {
         try (ServerSocket serverSocket = new ServerSocket(58901)) {


            System.out.println("Serwer Go działa...");
            System.out.println("Witaj w Go! (serwer będzie oczekiwał na wybór rozmiaru od pierwszego klienta)");

            // Akceptuj pierwszego klienta
            System.out.println("OCZEKIWANIE: Oczekiwanie na pierwszego klienta (wybór rozmiaru)...");
            Socket socket1 = serverSocket.accept();
            System.out.println("Pierwszy klient połączył się");

            // Wyślij żądanie wyboru trybu gry do pierwszego klienta
            PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
            out1.println("REQUEST_GAME_MODE");

            // Odczytaj komunikat SET_GAME_MODE od pierwszego klienta
            Scanner in1 = new Scanner(socket1.getInputStream());
            String gameMode = null;

            while (in1.hasNextLine()) {
                String line = in1.nextLine().trim();
                if (line.startsWith("SET_GAME_MODE")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        gameMode = parts[1];
                        System.out.println("Wybrano tryb gry: " + gameMode);
                        break;
                    }
                } else {
                    // Ignoruj inne linie do momentu otrzymania prawidłowej komendy
                }
            }
            
            // Wyślij żądanie wyboru rozmiaru do pierwszego klienta
            out1.println("REQUEST_BOARD_SIZE");

            // Odczytaj komunikat SET_BOARD_SIZE N od pierwszego klienta
            int boardSize = 0;
            Board board = null;

            while (in1.hasNextLine()) {
                String line = in1.nextLine().trim();
                if (line.startsWith("SET_BOARD_SIZE")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        try {
                            boardSize = Integer.parseInt(parts[1]);
                            board = BoardFactory.createBoard(boardSize);
                            System.out.println("Wybrano rozmiar planszy: " + boardSize);
                            break;
                        } catch (IllegalArgumentException e) {
                            System.out.println(
                                "Nieprawidłowy rozmiar otrzymany od klienta: " + parts[1]
                            );
                            // ignoruj i czekaj dalej
                        }
                    }
                } else {
                    // Ignoruj inne linie do momentu otrzymania prawidłowej komendy
                }
            }

            // nie zamykamy 'in1' ani InputStream tutaj — ClientHandler będzie obsługiwał połączenie dalej
            if (board == null) {
                System.out.println("Nie otrzymano prawidłowego rozmiaru planszy. Kończę.");
                socket1.close();
                return;
            }

            if (gameMode.equals("BOT")) {
                    // Inicjalizacja silnika gry
                    GameEngine gameEngine = new GameEngine(board);
                    System.out.println("Uruchamianie gry przeciwko botowi...");

                    // Stworzenie gracza i bota
                    GamePlayer blackPlayer = new GamePlayer("BlackPlayer", StoneColor.BLACK);
                    GamePlayer botPlayer = new GamePlayer("WhitePlayer", StoneColor.WHITE);
                    gameEngine.setPlayers(blackPlayer, botPlayer);

                    // Obsługa bazy danych
                    GameEntity gameEntity = persistenceService.startGame(gameEngine);

                    // Utworzenie handlera klienta i bota
                    ClientHandler black = new ClientHandler(socket1, gameEngine, blackPlayer, board, persistenceService, gameEntity);

                    // Tworzenie drugiego socketu dla bota
                    Socket botSocket = new Socket("localhost", socket1.getLocalPort());
                    BotHandler bot = new BotHandler(botSocket, gameEngine, botPlayer, board, persistenceService, gameEntity);

                    // Uruchomienie wątków obsługi klienta i bota
                    new Thread(black).start();
                    new Thread(bot).start();

                    // Po uruchomieniu obu handlerów — ustaw przeciwników i rozpocznij grę
                    black.setOpponent(bot);
                } else {
                // Akceptuj drugiego klienta
                System.out.println("OCZEKIWANIE: (możesz uruchomić drugiego klienta teraz)");
                Socket socket2 = serverSocket.accept();
               
                // Inicjalizacja silnika gry
                GameEngine gameEngine = new GameEngine(board);

                // Stworzenie dwóch lokalnych graczy
                GamePlayer blackPlayer = new GamePlayer("BlackPlayer", StoneColor.BLACK);
                GamePlayer whitePlayer = new GamePlayer("WhitePlayer", StoneColor.WHITE);
                gameEngine.setPlayers(blackPlayer, whitePlayer);
                
                // Obsługa bazy danych
                GameEntity gameEntity = persistenceService.startGame(gameEngine);

                System.out.println("OCZEKIWANIE: Oczekiwanie na drugiego klienta... (oczekuję na 1 połączenie)");

                // Utworzenie handlerów klientów
                ClientHandler black = new ClientHandler(socket1, gameEngine, blackPlayer, board, persistenceService, gameEntity);
                ClientHandler white = new ClientHandler(socket2, gameEngine, whitePlayer, board, persistenceService, gameEntity);

                // Uruchomienie wątków obsługi klientów
                new Thread(black).start();
                new Thread(white).start();

                // Po uruchomieniu obu handlerów — ustaw przeciwników i rozpocznij grę
                black.setOpponent(white);
                System.out.println("Drugi klient połączył się");
            }
        }
    }
}
