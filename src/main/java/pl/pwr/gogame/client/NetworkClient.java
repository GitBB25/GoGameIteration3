package pl.pwr.gogame.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.pwr.gogame.client.view.BoardCanvas;
import pl.pwr.gogame.client.view.GameView;
import pl.pwr.gogame.model.StoneColor;

/**
 * Klasa {@code NetworkClient} odpowiada za komunikację sieciową
 * pomiędzy klientem gry Go a serwerem. Obsługuje wysyłanie i
 * odbieranie wiadomości, aktualizację widoku gry oraz reakcje
 * na zdarzenia przychodzące z serwera.
 */
public class NetworkClient {

    /**
     * Strumień wyjściowy służący do wysyłania wiadomości do serwera.
     */
    private PrintWriter out;

    //javafx zapewnia nam klasę TextArea służącą
    //do przechowywania wielu linijek tekstu.
    //Tutaj służy do przechowywania logów

    /**
     * Widok gry, który jest aktualizowany na podstawie komunikatów z serwera.
     */
    private final GameView view;

    /**
     * Kontroler gry, wykorzystywany m.in. do rejestracji obsługi planszy.
     */
    private final GameController controller;

    /**
     * Tworzy nowego klienta sieciowego i nawiązuje połączenie z serwerem.
     * Po połączeniu uruchamiany jest osobny wątek nasłuchujący komunikaty
     * przychodzące z serwera.
     *
     * @param host adres serwera
     * @param port port serwera
     * @param view widok gry
     * @param controller kontroler gry
     */
    public NetworkClient(String host, int port, GameView view,
                         GameController controller) {
        this.view = view;
        this.controller = controller;

        try {
            Socket socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(() -> listen(socket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wyświetla okno dialogowe umożliwiające wybór
     * rozmiaru planszy gry. Opcja ta dostępna jest tylko
     * dla pierwszego gracza.
     */
    private void showBoardSizeDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Wybierz rozmiar planszy");

        Label label = new Label("Wybierz rozmiar planszy dla gry (tylko pierwszy gracz):");

        Button b9 = new Button("9 x 9");
        Button b13 = new Button("13 x 13");
        Button b19 = new Button("19 x 19");

        b9.setOnAction(e -> {
            send("SET_BOARD_SIZE 9");
            b9.setDisable(true);
            b13.setDisable(true);
            b19.setDisable(true);
            dialog.close();
        });
        b13.setOnAction(e -> {
            send("SET_BOARD_SIZE 13");
            b9.setDisable(true);
            b13.setDisable(true);
            b19.setDisable(true);
            dialog.close();
        });
        b19.setOnAction(e -> {
            send("SET_BOARD_SIZE 19");
            b9.setDisable(true);
            b13.setDisable(true);
            b19.setDisable(true);
            dialog.close();
        });

        HBox buttons = new HBox(12);
        buttons.setSpacing(12);
        buttons.setStyle("-fx-alignment: center;");
        buttons.getChildren().addAll(b9, b13, b19);

        VBox root = new VBox(16, label, buttons);
        root.setPadding(new javafx.geometry.Insets(14));
        root.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(root, 520, 220);

        // Stylizacja przycisków i dostosowanie ich rozmiarów do sceny
        b9.setStyle("-fx-font-size: 18px;");
        b13.setStyle("-fx-font-size: 18px;");
        b19.setStyle("-fx-font-size: 18px;");

        b9.prefWidthProperty().bind(scene.widthProperty().multiply(0.28));
        b13.prefWidthProperty().bind(scene.widthProperty().multiply(0.28));
        b19.prefWidthProperty().bind(scene.widthProperty().multiply(0.28));

        b9.prefHeightProperty().bind(scene.heightProperty().multiply(0.5));
        b13.prefHeightProperty().bind(scene.heightProperty().multiply(0.5));
        b19.prefHeightProperty().bind(scene.heightProperty().multiply(0.5));

        b9.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        b13.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        b19.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        dialog.setScene(scene);
        dialog.setMinWidth(420);
        dialog.setMinHeight(180);
        dialog.showAndWait();
    }

    /**
     * Nasłuchuje wiadomości przychodzących z serwera
     * i reaguje na nie odpowiednimi akcjami w interfejsie użytkownika.
     *
     * @param socket aktywne połączenie z serwerem
     */
    private void listen(Socket socket) {
        try (Scanner in = new Scanner(socket.getInputStream())) {

            while (in.hasNextLine()) {
                String msg = in.nextLine();
                System.out.println("SERVER: " + msg);

                // Jeśli w dowolnym komunikacie pojawia się informacja o zakończeniu gry,
                // zablokuj przycisk 'Resign' (użytkownik nie może się poddać po zakończeniu gry).
                if (msg.toLowerCase().contains("koniec gry") || msg.toLowerCase().contains("koniec")) {
                    Platform.runLater(() -> {
                        try {
                            view.getResignButton().setDisable(true);
                        } catch (Exception ignored) {}
                    });
                }

                if (msg.equals("REQUEST_BOARD_SIZE")) {
                    // show modal dialog to choose board size (only first client receives this)
                    Platform.runLater(() -> showBoardSizeDialog());
                    continue;
                }

                // W zależności od typu zdarzenia wywoływane są
                // dane metody
                // Np. dla MOVE dzielimy wiadomość np "1 2" na
                // koordynaty i rysujemy kamień na tym miejscu

                //dopóki drugi gracz się nie połączy, klikanie pierwszego w GUI nic nie robi
                if (msg.equals("GAME_START")) {
                    log("Gra się rozpoczęła.");
                }

                // negocjacje: serwer uruchamia fazę negocjacji
                if (msg.equals("NEGOTIATE_START")) {
                    log("Negocjacja martwych grup: zaznacz na planszy kamienie i kliknij Done");
                    Platform.runLater(() -> {
                        view.getNegotiateDoneButton().setVisible(true);
                        view.getNegotiateDoneButton().setDisable(false);
                        controller.setNegotiationMode(true);
                    });
                    continue;
                }

                if (msg.startsWith("NEGOTATE_MARKED")) {
                    // ack for local mark
                    continue;
                }

                if (msg.startsWith("OPPONENT_NEGOTATE_MARKED")) {
                    // Do not draw opponent's negotiation marks locally — just log it.
                    String[] parts = msg.split(" ");
                    if (parts.length >= 3) {
                        int col = Integer.parseInt(parts[1]);
                        int row = Integer.parseInt(parts[2]);
                        log("Przeciwnik oznaczył pozycję: " + col + "," + row);
                    }
                    continue;
                }

                if (msg.equals("NEGOTIATE_DONE_ACK") || msg.equals("OPPONENT_NEGOTIATE_DONE")) {
                    Platform.runLater(() -> {
                        controller.setNegotiationMode(false);
                        view.getNegotiateDoneButton().setVisible(false);
                        view.getNegotiateDoneButton().setDisable(true);
                        log("Negocjacje zakończone.");
                    });
                    continue;
                }

                if (msg.equals("YOUR_TURN")) {
                    // jeśli jesteśmy w trybie negocjacji, nie nadpisuj stanu planszy
                    if (controller != null && controller.isNegotiationMode()) {
                        log("Twój ruch (ignorowany podczas negocjacji)");
                    } else {
                        Platform.runLater(() -> {
                            if (view.getBoardCanvas() != null) view.getBoardCanvas().setMouseTransparent(false);
                            log("Twój ruch.");
                        });
                    }
                }

                if (msg.equals("OPPONENT_TURN")) {
                    // jeśli jesteśmy w trybie negocjacji, nie nadpisuj stanu planszy
                    if (controller != null && controller.isNegotiationMode()) {
                        log("Ruch przeciwnika (ignorowany podczas negocjacji)");
                    } else {
                        Platform.runLater(() -> {
                            if (view.getBoardCanvas() != null) view.getBoardCanvas().setMouseTransparent(true);
                            log("Ruch przeciwnika.");
                        });
                    }
                }

                if (msg.startsWith("CONFIG BOARD_SIZE")) {
                    int size = Integer.parseInt(msg.split(" ")[2]);

                    Platform.runLater(() -> {
                        view.createBoard(size);
                        controller.registerBoardHandlers();
                    });
                    continue;
                }

                if (msg.startsWith("MOVE")) {
                    String[] parts = msg.split(" ");
                    int col = Integer.parseInt(parts[1]);
                    int row = Integer.parseInt(parts[2]);
                    StoneColor color = StoneColor.valueOf(parts[3]);

                    Platform.runLater(() -> {
                        BoardCanvas board = view.getBoardCanvas();
                        if (board != null) {
                            board.drawStone(col, row, color);
                        }
                    });

                } else if (msg.startsWith("CAPTURE")) {
                    // usuwanie kamienia na danym miejscu

                    String[] parts = msg.split(" ");
                    int col = Integer.parseInt(parts[1]);
                    int row = Integer.parseInt(parts[2]);

                    Platform.runLater(() -> {
                        BoardCanvas board = view.getBoardCanvas();
                        if (board != null) {
                            board.removeStone(col, row);
                        }
                    });

                } else if (msg.startsWith("PASS")) {
                    //wpisywanie pasu do logów
                    log("Przeciwnik zpasował.");
                } else if (msg.startsWith("RESIGN")) {
                    String[] parts = msg.split(" ");
                    log("Koniec gry. Wygrał: " + parts[2]);
                    Platform.runLater(() -> {
                        try {
                            view.getResignButton().setDisable(true);
                        } catch (Exception ignored) {}
                    });
                } else if (msg.startsWith("TEXT")) {
                    //Wiadomości są schematu: "TEXT: info",
                    //usuń prefiks protokołu "TEXT " w miejscach, gdzie pokażemy podsumowanie
                    String text = msg.substring(5);
                    String cleaned = text.replaceAll("TEXT\\s*", "");
                    // Jeśli negocjacje nie przyniosły porozumienia, wyczyść oznaczenia i wróć do gry
                    if (text.startsWith("NEGOTIATION_FAILED") || text.contains("Brak zgody")) {
                        Platform.runLater(() -> {
                            controller.setNegotiationMode(false);
                            view.getNegotiateDoneButton().setVisible(false);
                            view.getNegotiateDoneButton().setDisable(true);
                            BoardCanvas board = view.getBoardCanvas();
                            if (board != null) board.clearNegotiationMarks();
                        });
                    }
                    // Jeśli otrzymujemy końcowe podsumowanie wyników, pokaż je i zablokuj planszę
                    if (text.toLowerCase().contains("koniec gry") || text.toLowerCase().contains("koniec") || text.contains("KONIEC GRY - PODSUMOWANIE")) {
                        // zapisz pełne, posprzątane podsumowanie do logów i pokaż dialog
                        log(cleaned);
                        Platform.runLater(() -> {
                            try {
                                view.getResignButton().setDisable(true);
                                view.getPassButton().setDisable(true);
                                BoardCanvas board = view.getBoardCanvas();
                                if (board != null) board.setMouseTransparent(true);
                            } catch (Exception ignored) {}
                        });
                    } else {
                        // zwykły tekst — loguj oryginalną treść
                        log(text);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wysyła wiadomość tekstową do serwera.
     *
     * @param msg treść wiadomości
     */
    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    /**
     * Dodaje wpis do logów gry w wątku JavaFX.
     *
     * @param message treść komunikatu
     */
    private void log(String message) {
        Platform.runLater(() ->
                view.getLogArea().appendText(message + "\n")
        );
    }
}
