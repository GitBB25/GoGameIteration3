// glowny plik uruchomieniowy serwera gry Go
// Composite, poniewaz sklada sie z wielu komponentow
package pl.pwr.gogame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        SpringApplication.run(Main.class, args);
    }

}
