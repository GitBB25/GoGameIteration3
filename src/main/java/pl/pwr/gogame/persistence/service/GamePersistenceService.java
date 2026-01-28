

// w entity znajduje się deklaracja tabel do których będą
//zapisywane dane, a tutaj w service znajdują się metody
//do zapisywania danych do tych tabel

import pl.pwr.gogame.persistence.repository.MoveRepository;

import java.time.LocalDateTime;

import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.persistence.entity.GameEntity;
import pl.pwr.gogame.persistence.entity.MoveEntity;
import pl.pwr.gogame.persistence.entity.MoveType;
import pl.pwr.gogame.persistence.repository.GameRepository;

public class GamePersistenceService {

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;

    public GamePersistenceService(GameRepository gameRepository, MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
    }

    public GameEntity startGame(GameEngine engine) {
        GameEntity game = new GameEntity();
        game.setBoardSize(engine.getBoard().getSize());
        game.setBlackPlayer(engine.get);
        game.setBlackPlayer(engine.get);
        game.setStartedAt(LocalDateTime.now());
        return gameRepository.save(game);
    }

    public void saveMove(GameEntity game, Move move, int moveNumber) {
        MoveEntity entity = new MoveEntity();
        entity.setGame(game);
        entity.setMoveNumber(moveNumber);
        entity.setPlayerColor(move.getPlayer().getColor());
        entity.setCol(move.getPosition().col());
        entity.setRow(move.getPosition().row());
        entity.setType(MoveType.MOVE);
        moveRepository.save(entity);
    }


    public void finishGame(GameEntity game, GamePlayer winner) {
        game.setWinner(winner.getName());
        game.setFinishedAt(LocalDateTime.now());
        gameRepository.save(game);

    }
    

}