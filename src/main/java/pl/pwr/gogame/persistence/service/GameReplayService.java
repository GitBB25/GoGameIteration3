package pl.pwr.gogame.persistence.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import pl.pwr.gogame.model.BoardFactory;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.Position;
import pl.pwr.gogame.model.ScoreResult;
import pl.pwr.gogame.model.StoneColor;
import pl.pwr.gogame.persistence.entity.GameEntity;
import pl.pwr.gogame.persistence.entity.MoveEntity;
import pl.pwr.gogame.persistence.entity.MoveType;
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

    public Optional<GameEntity> findLastGame() {
    return gameRepo.findAll()
        .stream()
        .reduce((first, second) -> second);   
    }   


    public GameEngine replayGameWithLogging(Long gameId) {

    GameEntity game = gameRepo.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

    GameEngine engine = new GameEngine(BoardFactory.createBoard(game.getBoardSize()));


    GamePlayer black = new GamePlayer(game.getBlackPlayerName(), game.getBlackPlayerColor());
    GamePlayer white = new GamePlayer(game.getWhitePlayerName(), game.getWhitePlayerColor());
    engine.setPlayers(black, white);

    System.out.println("\nInitial board:");
    System.out.println(engine.getBoard());

    
    List<MoveEntity> moves = moveRepo.findByGameOrderByMoveNumber(game);

    for (MoveEntity m : moves) {
        GamePlayer player = (m.getPlayerColor() == StoneColor.BLACK) ? black : white;
        MoveResult result;

        switch (m.getType()) {
            case MOVE -> {
                Move move = new Move(new Position(m.getCol(), m.getRow()), player);
                result = engine.applyMove(move);
                if (!result.isOk()) {
                    System.out.println("WARNING: Move could not be applied: " + move);
                }
            }
            case PASS -> {
                result = engine.pass(player);
                if (!result.isOk()) {
                    System.out.println("WARNING: Pass could not be applied for player: " + player.getName());
                }
            }
            case RESIGN -> {
                result = engine.resign(player);
                if (!result.isOk()) {
                    System.out.println("WARNING: Resign could not be applied for player: " + player.getName());
                }
            }
            default -> {
                System.out.println("Unknown move type: " + m.getType());
                continue;
            }
        }

        System.out.println("\nBoard after " + m.getType() + " by " + player.getName() + ":");
        System.out.println(engine.getBoard());
    }

    ScoreResult score = engine.calculateScores();
    System.out.println("\nFinal score:");
    System.out.println("BLACK: " + score.getBlackScore());
    System.out.println("WHITE: " + score.getWhiteScore());
    System.out.println("Winner: " + (score.getWinner() != null ? score.getWinner().getName() : "DRAW"));

    return engine;
    }
    
}