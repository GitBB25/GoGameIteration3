package pl.pwr.gogame.persistence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import pl.pwr.gogame.model.GameEngine;

@Component
public class ReplayTestRunner implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private GameReplayService replayService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        //Odpalamy replay w osobnym wątku by nie blokował wyświetlania logów z bieżącej gry
        new Thread(() -> {
            replayService.findLastGame().ifPresentOrElse(
                game -> {
                    System.out.println("\n=== ROZPOCZĘCIE POWTÓRKI DLA GRY " + game.getId() + " ===");

                    GameEngine engine = replayService.replayGameWithLogging(game.getId());

                    System.out.println(
                        (engine.getWinner() != null
                            ? engine.getWinner().getName()
                            : "DRAW"));

                    System.out.println("=== POWTÓRKA UKOŃCZONA ===\n");
                },
                () -> System.out.println("Brak gier w bazie, powtórka pominięta.")
            );
        }).start(); 
    }
}
