package pl.pwr.gogame.client.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import pl.pwr.gogame.model.StoneColor;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasa {@code BoardCanvas} odpowiada za graficzną reprezentację planszy gry Go.
 * Dziedziczy po klasie {@link Canvas} i umożliwia rysowanie planszy oraz kamieni
 * przy użyciu obiektu {@link GraphicsContext}.
 *
 * Przechowuje aktualny stan planszy (rozmieszczenie kamieni), aby umożliwić
 * ich ponowne rysowanie oraz usuwanie.
 */
//klasa Canvas daje nam obraz na którym można rysować za pomocą
//różnych dostępnych metod
public class BoardCanvas extends Canvas {

    /**
     * Liczba pól planszy w jednym wymiarze (np. 9x9, 13x13, 19x19).
     */
    private int size;

    /**
     * Rozmiar pojedynczej kratki planszy w pikselach.
     */
    private int gridSize = 40;

    /**
     * Mapa przechowująca aktualny stan planszy.
     * Kluczem jest pozycja w formacie "kolumna,wiersz",
     * a wartością kolor kamienia.
     */
    //żeby móc usuwać kamień z pola, należy przechowywać aktualny stan planszy,
    //ponieważ sam obiekt klasy GraphicsContext nie przechowuje historii
    //wywołanych metod i "nie wie", gdzie narysowaliśmy wcześniej kamień
    private final Map<String, StoneColor> stones = new HashMap<>();
    // marks used during negotiation: key -> "col,row"; value -> true for local player's mark, false for opponent's mark
    private final Map<String, Boolean> negotiationMarks = new HashMap<>();

    /**
     * Tworzy nową kanwę planszy o zadanym rozmiarze.
     * Ustawia szerokość i wysokość kanwy oraz inicjalnie rysuje pustą planszę.
     *
     * @param size liczba pól planszy w jednym wymiarze
     */
    //konstruktor- tworzenie obrazu o danym rozmiarze
    public BoardCanvas(int size) {
        this.size = size;
        setWidth(size * gridSize);
        setHeight(size * gridSize);

        widthProperty().addListener(evt -> redraw());
        heightProperty().addListener(evt -> redraw());

        drawEmptyBoard();
    }

    /**
     * Zwraca kolor kamienia na danej pozycji lub null jeśli pole puste.
     */
    public StoneColor getStoneAt(int col, int row) {
        return stones.get(col + "," + row);
    }

    /**
     * Zwraca listę pozycji (col,row) należących do grupy kamieni zawierającej podaną pozycję.
     * Używane po stronie GUI do zaznaczania całej grupy podczas negocjacji.
     * Zwracane pozycje są w formacie tablicy [col,row].
     */
    public java.util.List<int[]> getGroupPositions(int col, int row) {
        java.util.List<int[]> result = new java.util.ArrayList<>();
        StoneColor color = getStoneAt(col, row);
        if (color == null) return result;

        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
        stack.push(new int[]{col, row});

        while (!stack.isEmpty()) {
            int[] p = stack.pop();
            String key = p[0] + "," + p[1];
            if (visited.contains(key)) continue;
            visited.add(key);

            StoneColor s = getStoneAt(p[0], p[1]);
            if (s == null || s != color) continue;
            result.add(new int[]{p[0], p[1]});

            // neighbors: up/down/left/right
            if (p[0] - 1 >= 0) stack.push(new int[]{p[0] - 1, p[1]});
            if (p[0] + 1 < size) stack.push(new int[]{p[0] + 1, p[1]});
            if (p[1] - 1 >= 0) stack.push(new int[]{p[0], p[1] - 1});
            if (p[1] + 1 < size) stack.push(new int[]{p[0], p[1] + 1});
        }

        return result;
    }

    /**
     * Zwraca rozmiar planszy (liczbę pól w jednym wymiarze).
     * Metoda wykorzystywana np. przez kontrolery do mapowania
     * współrzędnych myszy na przecięcia linii planszy.
     *
     * @return rozmiar planszy
     */
    // expose size so controllers can map mouse coordinates to intersections
    public int getSize() {
        return size;
    }

    /**
     * Rysuje pustą planszę gry Go, składającą się z linii poziomych
     * i pionowych. Przed rysowaniem czyści całą kanwę.
     */
    public void drawEmptyBoard() {
        //każda kanwa z Canvas ma przydzielony obiekt GraphicsContext,
        //na którym wywołujemy nasze metody do rysowania. Obiekt
        //gc następnie dodaje do buffera parametry potrzebne do narysowania
        //danej rzeczy na kanwie
        GraphicsContext gc = getGraphicsContext2D();
        //czyścimy kanwę
        gc.clearRect(0, 0, getWidth(), getHeight());

    //rysujemy planszę- linie horyzontalne i wertykalne
    // upewnij się, że kolor i grubość linii są ustawione niezależnie od poprzednich rysowań
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(1.0);
        double w = getWidth();
        double h = getHeight();

        double cellWidth = w / size;
        double cellHeight = h / size;

        for (int i = 0; i < size; i++) {

            gc.strokeLine(cellWidth / 2, cellHeight / 2 + i * cellHeight,
                    w - cellWidth / 2, cellHeight / 2 + i * cellHeight);

            gc.strokeLine(cellWidth / 2 + i * cellWidth, cellHeight / 2,
                    cellWidth / 2 + i * cellWidth, h - cellHeight / 2);
        }
    }

    /**
     * Dodaje kamień o zadanym kolorze na wskazanej pozycji planszy
     * i odświeża jej widok.
     *
     * @param col   kolumna planszy
     * @param row   wiersz planszy
     * @param color kolor kamienia
     */
    //rysujemy umieszczony kamień
    public void drawStone(int col, int row, StoneColor color) {
        stones.put(col + "," + row, color);
        redraw();
    }

    /**
     * Usuwa kamień z podanej pozycji planszy
     * i odświeża jej widok.
     *
     * @param col kolumna planszy
     * @param row wiersz planszy
     */
    public void removeStone(int col, int row) {
        stones.remove(col + "," + row);
        redraw();
    }

    /**
     * Add a negotiation mark (X) on the given intersection.
     * @param col column
     * @param row row
     * @param local true if marked by local player, false if by opponent
     */
    public void addNegotiationMark(int col, int row, boolean local) {
        negotiationMarks.put(col + "," + row, local);
        redraw();
    }

    /** Remove a negotiation mark (if present). */
    public void removeNegotiationMark(int col, int row) {
        negotiationMarks.remove(col + "," + row);
        redraw();
    }

    /** Remove all negotiation marks. */
    public void clearNegotiationMarks() {
        negotiationMarks.clear();
        redraw();
    }

    /**
     * Przerysowuje całą planszę wraz z aktualnie umieszczonymi kamieniami.
     * Metoda ta jest wywoływana po każdej zmianie stanu planszy
     * (dodanie lub usunięcie kamienia).
     */
    //za każdym razem gdy chcemy narysować lub usunąć kamień na planszy GUI,
    //rysujemy od nowa całą planszę. 
    private void redraw() {
        drawEmptyBoard();

        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        double cellWidth = w / size;
        double cellHeight = h / size;
        double stoneDiameter = Math.min(cellWidth, cellHeight) * 0.7; // 70% of cell

        for (Map.Entry<String, StoneColor> entry : stones.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int col = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);

            gc.setFill(entry.getValue() == StoneColor.BLACK ? Color.BLACK : Color.WHITE);

            double x = col * cellWidth + cellWidth / 2 - stoneDiameter / 2;
            double y = row * cellHeight + cellHeight / 2 - stoneDiameter / 2;

            gc.fillOval(x, y, stoneDiameter, stoneDiameter);
        }

    for (Map.Entry<String, Boolean> m : negotiationMarks.entrySet()) {
            String[] parts = m.getKey().split(",");
            int col = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);

            double x = col * cellWidth + cellWidth / 2;
            double y = row * cellHeight + cellHeight / 2;

            Color markColor = m.getValue() ? Color.rgb(200, 0, 0, 1.0) : Color.rgb(255, 140, 0, 1.0);
            gc.setStroke(markColor);
            double lineWidth = Math.max(2.0, Math.min(cellWidth, cellHeight) * 0.06);
            gc.setLineWidth(lineWidth);

            gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

            double markSize = Math.min(cellWidth, cellHeight) * 0.6;
            double x1 = x - markSize / 2;
            double y1 = y - markSize / 2;
            double x2 = x + markSize / 2;
            double y2 = y + markSize / 2;
            gc.strokeLine(x1, y1, x2, y2);
            gc.strokeLine(x1, y2, x2, y1);
        }
    }
}
