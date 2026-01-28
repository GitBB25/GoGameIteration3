package pl.pwr.gogame.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.pwr.gogame.client.view.GameView;

/**
 * Klasa {@code GoClientApp} jest głównym punktem wejścia aplikacji klienckiej
 * gry Go. Dziedziczy po klasie {@link Application} biblioteki JavaFX
 * i odpowiada za inicjalizację oraz uruchomienie interfejsu użytkownika.
 */
public class GoClientApp extends Application {

    /**
     * Metoda wywoływana automatycznie przez środowisko JavaFX
     * po uruchomieniu aplikacji. Tworzy widok gry, kontroler
     * oraz ustawia główną scenę aplikacji.
     *
     * @param stage główne okno aplikacji
     */
    @Override
    public void start(Stage stage) {
        GameView view = new GameView();
        new GameController(view);

        Scene scene = new Scene(view.getRoot(), 800, 800);
        stage.setTitle("Go Game");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Główna metoda aplikacji.
     * Uruchamia aplikację JavaFX.
     *
     * @param args argumenty linii poleceń
     */
    public static void main(String[] args) {
        launch(args);
    }
}
