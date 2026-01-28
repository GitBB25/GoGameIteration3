package pl.pwr.gogame.client.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Klasa {@code GameView} odpowiada za warstwę widoku interfejsu użytkownika
 * podczas rozgrywki. Zarządza głównym układem aplikacji, planszą gry,
 * obszarem logów oraz przyciskami sterującymi przebiegiem gry.
 */
public class GameView {

    /**
     * Rozmiar planszy gry.
     */
    int boardsize;

    /**
     * Główny kontener układu widoku.
     */
    private BorderPane root = new BorderPane();

    /**
     * Kanwa reprezentująca planszę gry.
     */
    private BoardCanvas boardCanvas;

    /**
     * Pole tekstowe wyświetlające logi gry.
     */
    private TextArea logArea = new TextArea();

    /**
     * Przycisk umożliwiający wykonanie ruchu „Pass”.
     */
    private Button passButton = new Button("Pass");

    /**
     * Przycisk umożliwiający rezygnację z gry.
     */
    private Button resignButton = new Button("Resign");

    /**
     * Przycisk potwierdzający zakończenie oznaczania podczas negocjacji.
     */
    private Button negotiateDoneButton = new Button("Done");

    /**
     * Tworzy nowy widok gry.
     * Inicjalizuje elementy interfejsu użytkownika oraz
     * ustawia ich rozmieszczenie w głównym kontenerze.
     */
    public GameView() {
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        HBox bottom = new HBox(8);
        bottom.setPadding(new Insets(6));
    bottom.getChildren().addAll(passButton, resignButton, negotiateDoneButton);

    // domyślnie przycisk negocjacji ukryty
    negotiateDoneButton.setVisible(false);
    negotiateDoneButton.setDisable(true);

        root.setBottom(bottom);
        root.setTop(logArea);
    }

    /**
     * Zwraca główny węzeł widoku.
     *
     * @return główny kontener widoku
     */
    //getters
    public Parent getRoot() {
        return root;
    }

    /**
     * Zwraca obiekt {@link BoardCanvas} reprezentujący planszę gry.
     *
     * @return kanwa planszy
     */
    public BoardCanvas getBoardCanvas() {
        return boardCanvas;
    }

    /**
     * Zwraca pole tekstowe logów gry.
     *
     * @return obszar logów
     */
    public TextArea getLogArea() {
        return logArea;
    }

    /**
     * Zwraca przycisk „Pass”.
     *
     * @return przycisk pass
     */
    public Button getPassButton() {
        return passButton;
    }

    /**
     * Zwraca przycisk „Resign”.
     *
     * @return przycisk resign
     */
    public Button getResignButton() {
        return resignButton;
    }

    /**
     * Zwraca przycisk kończący fazę negocjacji.
     */
    public Button getNegotiateDoneButton() { return negotiateDoneButton; }

    /**
     * Tworzy planszę gry o podanym rozmiarze i umieszcza ją
     * w centralnej części widoku.
     *
     * @param size rozmiar planszy
     */
    public void createBoard(int size) {
        BoardCanvas board = new BoardCanvas(size);
        // keep visual appearance intact; ignore mouse events until server enables interaction
        board.setMouseTransparent(true);
        this.boardCanvas = board;
        root.setCenter(board);
    }
}
