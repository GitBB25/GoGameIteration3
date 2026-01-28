// glowny plik uruchomieniowy serwera gry Go
// Composite, poniewaz sklada sie z wielu komponentow
package pl.pwr.gogame.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.util.Scanner;

import pl.pwr.gogame.model.Board; 
import pl.pwr.gogame.model.BoardFactory;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.StoneColor;
import pl.pwr.gogame.server.ClientHandler;

/**
 * Klasa {@code Main} stanowi główny punkt uruchomieniowy
 * serwera gry Go.
 *
 * <p>
 * Odpowiada za:
 * <ul>
 *   <li>uruchomienie gniazda serwera</li>
 *   <li>akceptację połączeń klientów</li>
 *   <li>obsługę wyboru rozmiaru planszy przez pierwszego gracza</li>
 *   <li>inicjalizację silnika gry oraz graczy</li>
 *   <li>utworzenie i uruchomienie {@link ClientHandler} dla klientów</li>
 * </ul>
 * </p>
 *
 * <p>
 * Wzorzec projektowy: <b>Composite</b> – klasa składa się z wielu
 * współpracujących komponentów (silnik gry, plansza, handlery klientów).
 * </p>
 */

//keyword do poinformowania o wykorzystaniu Springa
@SpringBootApplication
public class Main {

    /**
     * Metoda główna uruchamiająca serwer gry Go.
     *
     * @param args argumenty linii poleceń (nieużywane)
     * @throws IOException w przypadku błędu wejścia/wyjścia
     */
    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(58901)) {
            System.out.println("Serwer Go działa...");
            System.out.println("Witaj w Go! (serwer będzie oczekiwał na wybór rozmiaru od pierwszego klienta)");

            // Akceptuj pierwszego i drugiego klienta (oba mogą się połączyć zanim wybierzemy rozmiar)
            System.out.println("OCZEKIWANIE: Oczekiwanie na pierwszego klienta (wybór rozmiaru)...");
            Socket socket1 = serverSocket.accept();
            System.out.println("Pierwszy klient połączył się");

            System.out.println("OCZEKIWANIE: (możesz uruchomić drugiego klienta teraz)");
            Socket socket2 = serverSocket.accept();
            System.out.println("Drugi klient połączył się (połączenia przyjęte). Teraz poproszę pierwszego o wybór rozmiaru planszy");

            // Wyślij żądanie wyboru rozmiaru do pierwszego klienta
            PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
            out1.println("REQUEST_BOARD_SIZE");

            // Odczytaj komunikat SET_BOARD_SIZE N od pierwszego klienta
            Scanner in1 = new Scanner(socket1.getInputStream());
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

            // Inicjalizacja silnika gry
            GameEngine gameEngine = new GameEngine(board);

            // Stworzenie dwóch lokalnych graczy
            GamePlayer blackPlayer = new GamePlayer("BlackPlayer", StoneColor.BLACK);
            GamePlayer whitePlayer = new GamePlayer("WhitePlayer", StoneColor.WHITE);
            gameEngine.setPlayers(blackPlayer, whitePlayer);

            System.out.println("OCZEKIWANIE: Oczekiwanie na drugiego klienta... (oczekuję na 1 połączenie)");

            // Utworzenie handlerów klientów
            ClientHandler black = new ClientHandler(socket1, gameEngine, blackPlayer, board);
            ClientHandler white = new ClientHandler(socket2, gameEngine, whitePlayer, board);

            // Uruchomienie wątków obsługi klientów
            new Thread(black).start();
            new Thread(white).start();

            // Po uruchomieniu obu handlerów — ustaw przeciwników i rozpocznij grę
            black.setOpponent(white);
        }
    }
}
