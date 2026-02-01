package pl.pwr.gogame.persistence.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import pl.pwr.gogame.model.BoardFactory;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.Position;
import pl.pwr.gogame.persistence.entity.GameEntity;
import pl.pwr.gogame.persistence.entity.MoveEntity;
import pl.pwr.gogame.persistence.repository.GameRepository;
import pl.pwr.gogame.persistence.repository.MoveRepository;


@Service
public class GameReplayService {

    private final GameRepository gameRepo;
    private final MoveRepository moveRepo;

    public GameReplayService(GameRepository gameRepo,
                             MoveRepository moveRepo) {
        this.gameRepo = gameRepo;
        this.moveRepo = moveRepo;
    }

       public Optional<GameEntity> findGameById(Long gameId) {
        return gameRepo.findById(gameId);
    }
    
    public GameEngine replayGame(Long gameId) {

        GameEntity game = gameRepo.findById(gameId)
                .orElseThrow();

        GameEngine engine = new GameEngine(
                BoardFactory.createBoard(game.getBoardSize())
        );

        List<MoveEntity> moves =
            moveRepo.findByGameOrderByMoveNumber(game);

        for (MoveEntity m : moves) {
            Move move = new Move(
                new Position(m.getCol(), m.getRow()),
                new GamePlayer("", m.getPlayerColor())
            );
            engine.applyMove(move);
        }

        return engine;
    }
}