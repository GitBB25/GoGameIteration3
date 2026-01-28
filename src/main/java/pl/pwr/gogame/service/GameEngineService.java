package pl.pwr.gogame.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.GameEngine;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.Move;
import pl.pwr.gogame.model.MoveResult;
import pl.pwr.gogame.model.Position;
import pl.pwr.gogame.model.StoneColor;

/**
 * Klasa {@code GameEngineService} zawiera główną logikę gry Go.
 * Odpowiada za walidację i wykonywanie ruchów, obsługę pasów,
 * rezygnacji oraz reguł gry (zbicia, samobójstwo, ko).
 *
 * Logika ta jest wydzielona z klasy {@link GameEngine},
 * która pełni rolę fasady i przechowuje stan gry.
 */
/**
 * Serwis zawierający główną logikę gry.
 */
public class GameEngineService {

    /**
     * Serwis pomocniczy operujący na planszy gry.
     */
    private final BoardService boardService;

    /**
     * Tworzy nowy serwis silnika gry.
     *
     * @param boardService serwis operujący na planszy
     */
    public GameEngineService(BoardService boardService) {
        this.boardService = boardService;
    }

    /**
     * Stosuje ruch na planszy, waliduje go oraz aktualizuje stan gry.
     * Sprawdza poprawność tury, regułę samobójstwa, regułę ko
     * oraz obsługuje zbicia kamieni przeciwnika.
     *
     * @param engine silnik gry
     * @param move ruch do wykonania
     * @return rezultat ruchu
     */
    public MoveResult applyMove(GameEngine engine, Move move) {
        if (engine.isEnd()) {
            return MoveResult.error("Gra została zakończona");
        }
        if (engine.getCurrentPlayer() == null) {
            return MoveResult.error("Gracze nie zostali zainicjalizowani!");
        }

        engine.setLastMoveWasPass(false);

        if (!move.getPlayer().equals(engine.getCurrentPlayer())) {
            return MoveResult.error("Tura przeciwnika");
        }

        String error = validatePreConditions(
                engine.getBoard(),
                move.getPosition(),
                move.getPlayer().getColor()
        );
        if (error != null) {
            return MoveResult.error(error);
        }

        String previousBoardState = engine.getBoard().toString();
        engine.getBoard().setStone(move.getPosition(), move.getPlayer().getColor());

        List<Position> capturedStones =
                tryCaptureOpponents(engine, move.getPosition(), move.getPlayer().getColor());

        boolean singleCapture = capturedStones.size() == 1;

        if (capturedStones.isEmpty()) {
            List<Position> myGroup =
                    boardService.getGroup(engine.getBoard(), move.getPosition(), new HashSet<>());
            if (boardService.getGroupLiberties(engine.getBoard(), myGroup) == 0) {
                engine.getBoard().removeStone(move.getPosition()); // Cofnij ruch
                return MoveResult.error("Nie można postawić kamienia - samobójstwo");
            }
        }

        if (singleCapture && engine.isSingleCaptureOnLastMove()) {
            if (engine.getBoard().toString().equals(engine.getPreviousBoardSnapshot())) {
                rollbackMove(engine, move.getPosition(), capturedStones, move.getPlayer().getColor());
                return MoveResult.error("Zaszło ko- ruch nieprawidłowy");
            }
        }

        engine.setPreviousBoardSnapshot(previousBoardState);
        engine.setSingleCaptureOnLastMove(singleCapture);
        engine.updateCaptureCounts(move.getPlayer().getColor(), capturedStones.size());
        engine.changePlayers();
        return MoveResult.ok(capturedStones);
    }

    /**
     * Obsługuje spasowanie przez gracza.
     * Jeśli obaj gracze spasują kolejno, gra zostaje zakończona.
     *
     * @param engine silnik gry
     * @param player gracz wykonujący pas
     * @return rezultat ruchu typu pass
     */
    public MoveResult pass(GameEngine engine, GamePlayer player) {
        if (!player.equals(engine.getCurrentPlayer())) {
            return MoveResult.error("Tura przeciwnika");
        }
        engine.setSingleCaptureOnLastMove(false);
        engine.setPreviousBoardSnapshot(null);

        if (engine.getLastMoveWasPass()) {
            // zamiast od razu kończyć grę, przejdź do fazy negocjacji
            engine.startNegotiation();
            return MoveResult.negotiationStart();
        } else {
            engine.setLastMoveWasPass(true);
            engine.changePlayers();
            return MoveResult.passNext();
        }
    }

    /**
     * Obsługuje rezygnację gracza i kończy grę.
     *
     * @param engine silnik gry
     * @param player gracz rezygnujący
     * @return rezultat rezygnacji
     */
    public MoveResult resign(GameEngine engine, GamePlayer player) {
        GamePlayer winner = engine.getOpponentPlayer(player);
        engine.setCurrentPlayer(null); // Blokuje dalsze ruchy
        engine.setEnd(true);
        return MoveResult.resign(player, winner);
    }

    /**
     * Sprawdza warunki wstępne poprawności ruchu.
     *
     * @param board plansza gry
     * @param position pozycja ruchu
     * @param color kolor kamienia
     * @return komunikat błędu lub {@code null}, jeśli ruch jest poprawny
     */
    private String validatePreConditions(Board board, Position position, StoneColor color) {
        if (color == StoneColor.EMPTY) return "Kolor nie może być pusty";
        if (position == null) return "Pozycja nie może być pusta";
        if (board.isOutOfBounds(position)) return "Ruch poza planszą";
        if (!board.isEmpty(position)) return "Pole jest już zajęte";
        return null;
    }

    /**
     * Próbuje zbić grupy kamieni przeciwnika po wykonaniu ruchu.
     *
     * @param engine silnik gry
     * @param currentMovePos pozycja wykonanego ruchu
     * @param myColor kolor aktualnego gracza
     * @return lista pozycji zbitych kamieni
     */
    private List<Position> tryCaptureOpponents(GameEngine engine,
                                               Position currentMovePos,
                                               StoneColor myColor) {
        List<Position> allCapturedStones = new ArrayList<>();
        StoneColor opponentColor = myColor.other();
        Set<Position> visitedStones = new HashSet<>();

        for (Position neighbor : boardService.getNeighbors(engine.getBoard(), currentMovePos)) {
            if (engine.getBoard().getStone(neighbor) == opponentColor
                    && !visitedStones.contains(neighbor)) {

                List<Position> opponentGroup =
                        boardService.getGroup(engine.getBoard(), neighbor, visitedStones);

                if (boardService.getGroupLiberties(engine.getBoard(), opponentGroup) == 0) {
                    allCapturedStones.addAll(opponentGroup);
                }
            }
        }

        for (Position captured : allCapturedStones) {
            engine.getBoard().removeStone(captured);
        }
        return allCapturedStones;
    }

    /**
     * Wycofuje ruch (rollback), przywracając planszę do poprzedniego stanu.
     * Wykorzystywane głównie przy wykrywaniu reguły ko.
     *
     * @param engine silnik gry
     * @param placed pozycja postawionego kamienia
     * @param captured lista zbitych kamieni
     * @param color kolor aktualnego gracza
     */
    private void rollbackMove(GameEngine engine,
                              Position placed,
                              List<Position> captured,
                              StoneColor color) {
        engine.getBoard().removeStone(placed);
        for (Position p : captured) {
            engine.getBoard().setStone(p, color.other());
        }
    }
}
