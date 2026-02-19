package pl.pwr.gogame.persistence.service;



// w entity znajduje się deklaracja tabel do których będą
//zapisywane dane, a tutaj w service znajdują się metody
//do zapisywania danych do tych tabel

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.StoneColor;
import pl.pwr.gogame.persistence.entity.GameEntity;
import pl.pwr.gogame.persistence.entity.MoveEntity;
import pl.pwr.gogame.persistence.entity.MoveType;
import pl.pwr.gogame.persistence.repository.GameRepository;
import pl.pwr.gogame.persistence.repository.MoveRepository;

@Service

public class GamePersistenceService {

     @Autowired
    private GameReplayService replayService;

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    
    public GamePersistenceService(GameRepository gameRepository, MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
    }

    public GameEntity startGame(GameEngine engine) {
        GameEntity game = new GameEntity(
            engine.getBoard().getSize(),
            engine.getBlackPlayer().getName(),
            engine.getBlackPlayer().getColor(),
            engine.getWhitePlayer().getName(),
            engine.getWhitePlayer().getColor(),
            LocalDateTime.now()
        );
        return gameRepository.save(game);
    }
    
    @Transactional
    public void saveMove(GameEntity game, Move move, int moveNumber) {
        MoveEntity entity = new MoveEntity();
        
        entity.setGame(game);
        entity.setMoveNumber(game.getMoves().size() + 1);
        entity.setPlayerColor(move.getPlayer().getColor());
        entity.setCol(move.getPosition().col());
        entity.setRow(move.getPosition().row());
        entity.setType(MoveType.MOVE);
        
        game.getMoves().add(entity);

        moveRepository.save(entity);
        moveRepository.flush();
    }

    public void saveMovePassBot(GameEntity game, Move move, int moveNumber) {
        MoveEntity entity = new MoveEntity();
        
        entity.setGame(game);
        entity.setMoveNumber(game.getMoves().size() + 1);
        entity.setPlayerColor(StoneColor.WHITE);
        entity.setCol(-1);
        entity.setRow(-1);
        entity.setType(MoveType.PASS);
        
        game.getMoves().add(entity);

        moveRepository.save(entity);
        moveRepository.flush();
    }

    public void finishGame(GameEntity game, GamePlayer winner) {
         if (winner != null) {
        game.setWinner(winner.getName());
        } else {
        game.setWinner("DRAW");
        }
    game.setFinishedAt(LocalDateTime.now());
    gameRepository.save(game);

    replayService.replayGame(game.getId());
    }
    

}