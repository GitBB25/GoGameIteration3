package pl.pwr.gogame.persistence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import pl.pwr.gogame.model.GameEngine;

@Component
public class ReplayTestRunner implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private GameReplayService replayService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        replayService.findGameById(1L).ifPresentOrElse(
            game -> {
                GameEngine engine = replayService.replayGame(game.getId());
                System.out.println("Replay finished for game " + game.getId());
            },
            () -> System.out.println("No game 1 found in DB, skipping replay.")
        );
    }
}