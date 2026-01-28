package pl.pwr.gogame.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import pl.pwr.gogame.model.GamePhase;

import pl.pwr.gogame.service.BoardService;
import pl.pwr.gogame.service.GameEngineService;

/**
 * Klasa {@code GameEngine} pełni rolę fasady dla logiki gry Go.
 * Przechowuje aktualny stan gry (planszę, graczy, punktację)
 * oraz deleguje szczegółowe operacje do odpowiednich serwisów.
 */
/**
 * Fasada dla logiki gry. Przechowuje stan gry i deleguje operacje do serwisów.
 */
public class GameEngine {

    //atany gry

    /**
     * Plansza gry.
     */
    private final Board board;

    /**
     * Gracz grający kolorem czarnym.
     */
    private GamePlayer blackPlayer;

    /**
     * Gracz grający kolorem białym.
     */
    private GamePlayer whitePlayer;

    /**
     * Aktualny gracz wykonujący ruch.
     */
    private GamePlayer currentPlayer;

    /**
     * Liczba kamieni zbitych przez czarnego gracza.
     */
    private int blackCaptures = 0;

    /**
     * Liczba kamieni zbitych przez białego gracza.
     */
    private int whiteCaptures = 0;

    /**
     * Migawka poprzedniego stanu planszy, wykorzystywana
     * m.in. do sprawdzania reguły ko.
     */
    private String previousBoardSnapshot;

    /**
     * Informacja, czy w poprzednim ruchu zbity został dokładnie jeden kamień.
     */
    private boolean singleCaptureOnLastMove;

    /**
     * Informacja, czy ostatni ruch był pasem.
     */
    private boolean lastMoveWasPass = false;

    /**
     * Informacja, czy gra została zakończona.
     */
    private boolean end = false;

    // --- negocjacja martwych grup ---
    /** Faza gry: PLAYING, NEGOTIATION, FINISHED */
    private GamePhase phase = GamePhase.PLAYING;

    /** Dla każdego gracza: pozycje oznaczone w fazie negocjacji */
    private final Map<GamePlayer, Set<Position>> negotiationMarks = new HashMap<>();

    /** Gracze, którzy zakończyli oznaczanie w negocjacji */
    private final Set<GamePlayer> negotiationDone = new HashSet<>();
    /** Wynik ostatniej negocjacji (true jeśli obie strony się zgodziły) */
    private boolean lastNegotiationSucceeded = false;

    //serwisy

    /**
     * Serwis operujący na planszy.
     */
    private final BoardService boardService;

    /**
     * Serwis realizujący główną logikę silnika gry.
     */
    private final GameEngineService GameEngineService;

    /**
     * Tworzy nowy silnik gry dla podanej planszy.
     *
     * @param board plansza gry
     */
    public GameEngine(Board board) {
        this.board = board;
        this.boardService = new BoardService();
        this.GameEngineService = new GameEngineService(this.boardService);
        this.currentPlayer = null;
    }

    // --- negocjacja martwych grup: API ---
    /** Zwraca aktualną fazę gry */
    public synchronized GamePhase getPhase() { return phase; }

    /** Rozpoczyna fazę negocjacji (po podwójnym pasie) */
    public synchronized void startNegotiation() {
        phase = GamePhase.NEGOTIATION;
        negotiationMarks.clear();
        negotiationDone.clear();
    }

    /** Gracz oznacza pozycję/grupę jako potencjalnie martwą (w trakcie negocjacji) */
    public synchronized void markNegotiationPosition(GamePlayer player, Position pos) {
        negotiationMarks.computeIfAbsent(player, k -> new HashSet<>()).add(pos);
    }

    /** Gracz kończy oznaczanie; gdy obaj gracze skończą, stosujemy uzgodnione rezultaty */
    /**
     * Oznacza, że dany gracz zakończył fazę negocjacji.
     * Zwraca true jeśli po tym wywołaniu obie strony zakończyły negocjację
     * (wtedy wyniki negocjacji zostaną zastosowane), w przeciwnym wypadku false.
     */
    public synchronized boolean finishNegotiationFor(GamePlayer player) {
        negotiationDone.add(player);
        if (negotiationDone.contains(blackPlayer) && negotiationDone.contains(whitePlayer)) {
            applyNegotiationResults();
            return true;
        }
        return false;
    }

    /** Wykonuje usunięcie grup, które zostały oznaczone przez obie strony (consensus) */
    private void applyNegotiationResults() {
        Set<Position> blackMarks = negotiationMarks.getOrDefault(blackPlayer, Collections.emptySet());
        Set<Position> whiteMarks = negotiationMarks.getOrDefault(whitePlayer, Collections.emptySet());

        // jeśli obie strony oznaczyły dokładnie te same pozycje -> kontynuujemy usuwanie
        if (blackMarks.equals(whiteMarks)) {
            Set<Position> agreed = new HashSet<>(blackMarks);

            // Usuń dokładnie te pozycje, które zostały uzgodnione — pojedynczo ustawiając je jako EMPTY
            // i zliczaj usunięte kamienie, aby zaktualizować liczniki zbitych.
            Set<Position> removed = new HashSet<>();
            int removedByBlack = 0;
            int removedByWhite = 0;
            for (Position p : agreed) {
                if (removed.contains(p)) continue;
                if (board.isOutOfBounds(p)) continue;
                StoneColor s = board.getStone(p);
                if (s == StoneColor.EMPTY) continue;
                // ustaw pole jako puste
                board.removeStone(p);
                removed.add(p);
                // zliczaj w zależności od koloru usuniętego kamienia
                if (s == StoneColor.WHITE) removedByBlack++;
                else if (s == StoneColor.BLACK) removedByWhite++;
            }

            if (removedByBlack > 0) updateCaptureCounts(StoneColor.BLACK, removedByBlack);
            if (removedByWhite > 0) updateCaptureCounts(StoneColor.WHITE, removedByWhite);

            // Zakończ grę — negocjacja zakończona sukcesem.
            this.end = true;
            this.phase = GamePhase.FINISHED;
            this.lastNegotiationSucceeded = true;
        } else {
            // negocjacja nie przyniosła konsensusu -> gra trwa dalej
            this.lastNegotiationSucceeded = false;
            this.phase = GamePhase.PLAYING;
            this.negotiationMarks.clear();
            this.negotiationDone.clear();
            // resetujemy flagę pasów, tak by gra mogła być kontynuowana normalnie
            this.lastMoveWasPass = false;
        }
    }

    public synchronized boolean getLastNegotiationSucceeded() { return lastNegotiationSucceeded; }

    //metody fasady 

    /**
     * Wykonuje ruch gracza i aktualizuje stan gry.
     *
     * @param move ruch do wykonania
     * @return rezultat ruchu
     */
    public synchronized MoveResult applyMove(Move move) {
        return GameEngineService.applyMove(this, move);
    }

    /**
     * Obsługuje wykonanie ruchu typu „pass” przez gracza.
     *
     * @param player gracz wykonujący pas
     * @return rezultat operacji
     */
    public synchronized MoveResult pass(GamePlayer player) {
        return GameEngineService.pass(this, player);
    }

    /**
     * Obsługuje rezygnację gracza z gry.
     *
     * @param player gracz rezygnujący
     * @return rezultat operacji
     */
    public synchronized MoveResult resign(GamePlayer player) {
        return GameEngineService.resign(this, player);
    }

    //zarzadzanie graczami i punktacja

    /**
     * Ustawia graczy gry i inicjalizuje aktualnego gracza
     * jako gracza czarnego.
     *
     * @param blackPlayer gracz czarny
     * @param whitePlayer gracz biały
     */
    public void setPlayers(GamePlayer blackPlayer, GamePlayer whitePlayer) {
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.currentPlayer = blackPlayer;
    }

    /**
     * Zmienia aktualnego gracza na przeciwnika.
     */
    public void changePlayers() {
        currentPlayer = (currentPlayer == blackPlayer) ? whitePlayer : blackPlayer;
    }

    /**
     * Aktualizuje liczbę zbitych kamieni dla danego koloru.
     *
     * @param color kolor gracza
     * @param count liczba zbitych kamieni
     */
    public void updateCaptureCounts(StoneColor color, int count) {
        if (count == 0) return;
        if (color == StoneColor.BLACK) blackCaptures += count;
        else whiteCaptures += count;
    }

    /**
     * Zwraca przeciwnika podanego gracza.
     *
     * @param player gracz
     * @return przeciwnik gracza
     * @throws IllegalArgumentException jeśli gracz nie jest znany
     */
    public GamePlayer getOpponentPlayer(GamePlayer player) {
        if (player.equals(blackPlayer)) return whitePlayer;
        if (player.equals(whitePlayer)) return blackPlayer;
        throw new IllegalArgumentException("Nieznany gracz");
    }

    /**
     * Oblicza końcowe wyniki gry na podstawie terytoriów
     * oraz liczby zbitych kamieni.
     *
     * @return wynik punktowy gry
     */
    public ScoreResult calculateScores() {
        int blackTerritory = 0;
        int whiteTerritory = 0;
        Set<Position> visited = new HashSet<>();
        int boardSize = board.getSize();
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Position currentPos = new Position(c, r);
                if (board.isEmpty(currentPos) && !visited.contains(currentPos)) {
                    List<Position> region = boardService.getEmptyRegion(board, currentPos, visited);
                    Set<StoneColor> borderingColors = boardService.getBorderingColors(board, region);
                    if (borderingColors.size() == 1) {
                        if (borderingColors.contains(StoneColor.BLACK)) {
                            blackTerritory += region.size();
                        } else if (borderingColors.contains(StoneColor.WHITE)) {
                            whiteTerritory += region.size();
                        }
                    }
                }
            }
        }
        int finalBlackScore = blackTerritory + getBlackCaptures();
        int finalWhiteScore = whiteTerritory + getWhiteCaptures();
        GamePlayer winner = (finalBlackScore > finalWhiteScore) ? blackPlayer : whitePlayer;
        if (finalBlackScore == finalWhiteScore) {
            winner = null; // Remis
        }
        return new ScoreResult(finalBlackScore, finalWhiteScore, winner);
    }

    //gettery i settery

    public Board getBoard() { return board; }

    public GamePlayer getCurrentPlayer() { return currentPlayer; }

    public void setCurrentPlayer(GamePlayer player) { this.currentPlayer = player; }

    public boolean getLastMoveWasPass() { return lastMoveWasPass; }

    public void setLastMoveWasPass(boolean value) { this.lastMoveWasPass = value; }

    public boolean isEnd() { return end; }

    public void setEnd(boolean value) { this.end = value; }

    public String getPreviousBoardSnapshot() { return previousBoardSnapshot; }

    public void setPreviousBoardSnapshot(String snapshot) { this.previousBoardSnapshot = snapshot; }

    public boolean isSingleCaptureOnLastMove() { return singleCaptureOnLastMove; }

    public void setSingleCaptureOnLastMove(boolean value) { this.singleCaptureOnLastMove = value; }

    public int getBlackCaptures() { return blackCaptures; }

    public int getWhiteCaptures() { return whiteCaptures; }

    public StoneColor getCurrentColor() {
        return currentPlayer != null ? currentPlayer.getColor() : StoneColor.EMPTY;
    }
}