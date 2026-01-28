package pl.pwr.gogame.server;

// Parsowanie komend od klienta w grze Go
// Wzorzec: Adapter

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.StoneColor;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.Position;

/**
 * Klasa {@code CommandParser} odpowiada za parsowanie komend tekstowych
 * otrzymywanych od klienta gry Go i przekształcanie ich
 * w obiekty logiki domenowej.
 *
 * <p>
 * Realizuje wzorzec projektowy <b>Adapter</b>, adaptując dane wejściowe
 * w postaci tekstu na obiekty takie jak {@link Move}.
 * </p>
 */
public class CommandParser {

    /**
     * Parsuje komendę tekstową reprezentującą ruch gracza CMD.
     *
     * <p>
     * Poprawna komenda ruchu powinna zawierać dwie liczby całkowite
     * oznaczające współrzędne pola planszy (kolumna i wiersz).
     * </p>
     *
     * <p>
     * Jeśli komenda oznacza rezygnację ({@code resign}),
     * zwracana jest wartość {@code null}.
     * </p>
     *
     * @param command komenda tekstowa otrzymana od klienta
     * @param player gracz wykonujący ruch
     * @return obiekt {@link Move} reprezentujący ruch lub {@code null} w przypadku rezygnacji
     * @throws IllegalArgumentException jeśli komenda jest niepoprawna
     */
    public static Move parseMove(String command, GamePlayer player) {

        //sprawdzamy czy komenda to resign. W tym przypadku
        //zwracamy specjalny obiekt Move bedacy nullem
        command = command.trim().toLowerCase();
        if (command.equals("resign")) return null;

        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Pusta komenda");
        }

        String[] parts = command.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Wymagane dwie współrzędne (x y)");
        }

        try {
            int col = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);
            return new Move(new Position(col, row), player);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Współrzędne muszą być liczbami");
        }
    }
}
