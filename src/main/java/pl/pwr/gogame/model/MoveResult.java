package pl.pwr.gogame.model;
// reprezentacja wyniku ruchu w grze Go

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Klasa {@code MoveResult} reprezentuje rezultat wykonania ruchu w grze Go.
 * Zawiera informacje o powodzeniu operacji, ewentualnych zbiciach,
 * komunikacie błędu, a także o zdarzeniach specjalnych takich jak
 * pass, zakończenie gry czy rezygnacja gracza.
 */
public class MoveResult {

    /**
     * Informacja, czy ruch zakończył się powodzeniem.
     */
    private final boolean ok;

    /**
     * Lista pozycji zbitych kamieni w wyniku ruchu.
     */
    private final List<Position> capturedPositions;

    /**
     * Komunikat błędu, jeśli ruch był niepoprawny.
     */
    private final String errorMessage;

    /**
     * Informacja, czy ruch był pasem.
     */
    private final boolean passed;

    /**
     * Informacja, czy gra została zakończona.
     */
    private final boolean end;

    /**
     * Informacja, czy uruchomiono fazę negocjacji (po podwójnym pasie).
     */
    private final boolean negotiation;

    /**
     * Informacja, czy któryś z graczy zrezygnował z gry.
     */
    private final boolean resigned;

    /**
     * Zwycięzca gry (jeśli gra została zakończona).
     */
    private final GamePlayer winner;

    /**
     * Przegrany gracz (jeśli gra została zakończona).
     */
    private final GamePlayer loser;

    /**
     * Tworzy obiekt wyniku ruchu z pełnym zestawem informacji.
     *
     * @param ok informacja, czy ruch był poprawny
     * @param errorMessage komunikat błędu
     * @param capturedPositions lista zbitych kamieni
     * @param passed informacja o pasie
     * @param end informacja o zakończeniu gry
     * @param resigned informacja o rezygnacji
     * @param winner zwycięzca gry
     * @param loser przegrany gracz
     */
    public MoveResult(boolean ok, String errorMessage, List<Position> capturedPositions,
                      boolean passed, boolean end, boolean resigned,
                      boolean negotiation,
                      GamePlayer winner, GamePlayer loser) {
        this.ok = ok;
        this.capturedPositions = capturedPositions != null ? capturedPositions : new ArrayList<>();
        this.errorMessage = errorMessage;
        this.passed = passed;
        this.end = end;
        this.resigned = resigned;
        this.winner = winner;
        this.loser = loser;
        this.negotiation = negotiation;
    }

    /**
     * Tworzy wynik poprawnego ruchu z listą zbitych kamieni.
     *
     * @param capturedPositions lista zbitych pozycji
     * @return poprawny wynik ruchu
     */
    public static MoveResult ok(List<Position> capturedPositions) {
    return new MoveResult(true, null, capturedPositions,
        false, false, false, false, null, null);
    }

    //pierwszy gracz zrobił pass

    /**
     * Tworzy wynik ruchu typu pass, gdy tylko jeden gracz spasował.
     *
     * @return wynik ruchu typu pass
     */
    public static MoveResult passNext() {
    return new MoveResult(true, null, null,
        true, false, false, false, null, null);
    }

    //obojga graczy zrobiło pass

    /**
     * Tworzy wynik ruchu typu pass kończącego grę
     * (obaj gracze spasowali).
     *
     * @return wynik kończący grę
     */
    public static MoveResult passEnd() {
    return new MoveResult(true, null, null,
        true, true, false, false, null, null);
    }

    /**
     * Tworzy wynik rezygnacji jednego z graczy.
     *
     * @param loser gracz rezygnujący
     * @param winner zwycięzca gry
     * @return wynik rezygnacji
     */
    public static MoveResult resign(GamePlayer loser, GamePlayer winner) {
    return new MoveResult(false, null, null,
        false, true, true, false, winner, loser);
    }

    /**
     * Tworzy wynik błędnego ruchu.
     *
     * @param message komunikat błędu
     * @return wynik błędu
     */
    public static MoveResult error(String message) {
    return new MoveResult(false, message, Collections.emptyList(),
        false, false, false, false, null, null);
    }

    /**
     * Tworzy wynik informujący o rozpoczęciu fazy negocjacji po podwójnym pasie.
     */
    public static MoveResult negotiationStart() {
    return new MoveResult(true, null, null,
        true, false, false, true, null, null);
    }

    /**
     * Informuje, czy ruch był poprawny.
     *
     * @return {@code true} jeśli ruch się powiódł
     */
    public boolean isOk() { return ok; }

    /**
     * Zwraca listę zbitych kamieni.
     *
     * @return lista pozycji zbitych kamieni
     */
    public List<Position> getCapturedPositions() { return capturedPositions; }

    /**
     * Zwraca komunikat błędu ruchu.
     *
     * @return komunikat błędu
     */
    public String getErrorMessage() { return this.errorMessage; }

    /**
     * Informuje, czy ruch był pasem.
     *
     * @return {@code true} jeśli ruch był pasem
     */
    public boolean isPassed() { return passed; }

    /**
     * Informuje, czy gra została zakończona.
     *
     * @return {@code true} jeśli gra się zakończyła
     */
    public boolean isEnd() { return end; }

    /** Czy rozpoczęto fazę negocjacji (po podwójnym pasie)? */
    public boolean isNegotiation() { return negotiation; }

    /**
     * Informuje, czy ruch zakończył się rezygnacją.
     *
     * @return {@code true} jeśli gracz zrezygnował
     */
    public boolean isResigned() { return resigned; }

    /**
     * Zwraca zwycięzcę gry.
     *
     * @return zwycięzca gry
     */
    public GamePlayer getWinner() { return winner; }

    /**
     * Zwraca przegranego gracza.
     *
     * @return przegrany gracz
     */
    public GamePlayer getLoser() { return loser; }
}
