package pl.pwr.gogame.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.Position;
import pl.pwr.gogame.model.StoneColor;

/**
 * Serwis {@code BoardService} zawiera logikę operującą bezpośrednio
 * na obiekcie {@link Board}. Udostępnia metody do analizy sąsiedztwa,
 * grup kamieni, pustych regionów, oddechów (liberties) oraz
 * kolorów otaczających dane terytorium.
 */
public class BoardService {

    /**
     * Zwraca listę sąsiadów (góra, dół, lewo, prawo) dla danej pozycji
     * na planszy, pomijając pozycje wychodzące poza planszę.
     *
     * @param board plansza gry
     * @param position pozycja, dla której szukani są sąsiedzi
     * @return lista sąsiednich pozycji
     */
    public List<Position> getNeighbors(Board board, Position position) {
        List<Position> neighbors = new ArrayList<>();
        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            // POPRAWIONA KOLEJNOŚĆ: (kolumna, wiersz)
            Position neighbor = new Position(position.col() + dCol[i], position.row() + dRow[i]);
            if (!board.isOutOfBounds(neighbor)) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    /**
     * Oblicza liczbę oddechów (liberties) dla pojedynczego kamienia
     * znajdującego się na danej pozycji.
     *
     * @param board plansza gry
     * @param position pozycja kamienia
     * @return liczba oddechów kamienia
     */
    public int getLibertiesCount(Board board, Position position) {
        int count = 0;
        for (Position neighbor : getNeighbors(board, position)) {
            if (board.getStone(neighbor) == StoneColor.EMPTY) {
                count++;
            }
        }
        return count;
    }

    //Poprzednią funkcję findGroup rozbiliśmy na floodfill i funkcje znajdujące
    //grupy kamieni danego koloru i funkcję szukania pustych regionów,
    //bo algorytm szukania jest ten sam

    /**
     * Wykonuje algorytm flood fill w celu znalezienia wszystkich
     * połączonych pól o zadanym kolorze.
     *
     * @param board plansza gry
     * @param startPosition pozycja startowa
     * @param color kolor, który ma być wyszukiwany
     * @param visited zbiór już odwiedzonych pozycji
     * @return lista pozycji należących do znalezionej grupy
     */
    public List<Position> floodFill(Board board,
                                    Position startPosition,
                                    StoneColor color,
                                    Set<Position> visited) {

        List<Position> group = new ArrayList<>();

        //jeśli nasza pozycja startowa nie jest kolorem który sprawdzamy, lub jeśli już ją sprawdziliśmy, zwracamy pustą listę
        if (board.getStone(startPosition) != color || visited.contains(startPosition)) {
            return group;
        }

        Queue<Position> queue = new LinkedList<>();
        queue.add(startPosition);
        visited.add(startPosition);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            group.add(current);

            for (Position neighbor : getNeighbors(board, current)) {
                if (!visited.contains(neighbor) && board.getStone(neighbor) == color) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return group;
    }

    /**
     * Zwraca listę pozycji należących do grupy połączonych kamieni
     * tego samego koloru.
     *
     * @param board plansza gry
     * @param startPosition pozycja startowa
     * @param visited zbiór odwiedzonych pozycji
     * @return lista pozycji w grupie kamieni
     */
    public List<Position> getGroup(Board board, Position startPosition, Set<Position> visited) {
        StoneColor color = board.getStone(startPosition);
        //ta funkcja działa tylko dla kolorów, nie dla pustych pól. Jak kolor jest pusty to
        //zwracamy pustą listę
        if (color == StoneColor.EMPTY) {
            return List.of();
        }
        return floodFill(board, startPosition, color, visited);
    }

    /**
     * Znajduje wszystkie pola należące do jednego pustego regionu
     * (obszar pustych pól połączonych ze sobą).
     *
     * @param board plansza gry
     * @param start pozycja startowa pustego pola
     * @param visited zbiór odwiedzonych pozycji
     * @return lista pozycji należących do pustego regionu
     */
    //Znajduje pola należące do pustego regionu.
    public List<Position> getEmptyRegion(Board board, Position start, Set<Position> visited) {
        return floodFill(board, start, StoneColor.EMPTY, visited);
    }

    /**
     * Oblicza liczbę oddechów (liberties) dla całej grupy kamieni.
     *
     * @param board plansza gry
     * @param group lista pozycji tworzących grupę kamieni
     * @return liczba unikalnych oddechów grupy
     */
    /**
     * Oblicza liczbę oddechów dla całej grupy kamieni. 
     */
    public int getGroupLiberties(Board board, List<Position> group) {
        Set<Position> liberties = new HashSet<>();
        for (Position stone : group) {
            for (Position neighbor : getNeighbors(board, stone)) {
                if (board.isEmpty(neighbor)) {
                    liberties.add(neighbor);
                }
            }
        }
        return liberties.size();
    }

    /**
     * Zwraca zbiór kolorów kamieni otaczających dany pusty region.
     * Jeśli zbiór zawiera tylko jeden kolor, terytorium należy
     * do gracza tego koloru.
     *
     * @param board plansza gry
     * @param emptyRegion lista pozycji pustego regionu
     * @return zbiór kolorów otaczających region
     */
    //Tworzy listę kolorów kamieni sąsiadujących z pustym terytorium.
    //W zasadzie odwrotność funkcji getGroupLiberties- dla pustych pól
    //sprawdzamy czy sąsiad jest kamieniem i dodajemy jego kolor do zbioru
    public Set<StoneColor> getBorderingColors(Board board, List<Position> emptyRegion) {
        Set<StoneColor> colors = new HashSet<>();

        for (Position p : emptyRegion) {
            for (Position neighbor : getNeighbors(board, p)) {
                StoneColor c = board.getStone(neighbor);
                if (c != StoneColor.EMPTY) {
                    colors.add(c);
                }
            }
        }

        return colors;
    }
}
