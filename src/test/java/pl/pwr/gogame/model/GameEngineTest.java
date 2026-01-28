package pl.pwr.gogame.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.pwr.gogame.service.BoardService;

public class GameEngineTest {

    private Board board;
    private GameEngine gameEngine;
    private GamePlayer blackPlayer;
    private GamePlayer whitePlayer;


    @BeforeEach
    void setUp() {
        // Inicjalizujemy świeżą planszę i silnik gry przed każdym testem
        board = new Board(9);
        gameEngine = new GameEngine(board);
        blackPlayer = new GamePlayer("Black Player", StoneColor.BLACK);
        whitePlayer = new GamePlayer("White Player", StoneColor.WHITE);
        gameEngine.setPlayers(blackPlayer, whitePlayer);
    }

    @Test
    void testShouldCaptureStoneBySurroundingItInASquare() {
        // Umieszczamy jeden biały kamień na środku
        Position whiteStonePos = new Position(1, 1);
        board.setStone(whiteStonePos, StoneColor.WHITE);

        // Otaczamy go z trzech stron czarnymi kamieniami
        board.setStone(new Position(0, 1), StoneColor.BLACK); // Góra
        board.setStone(new Position(2, 1), StoneColor.BLACK); // Dół
        board.setStone(new Position(1, 0), StoneColor.BLACK); // Lewo
        // Ostatni oddech białego kamienia jest na pozycji (1, 2)

        // Sprawdzamy stan przed ruchem
        assertEquals(StoneColor.WHITE, board.getStone(whiteStonePos));
        assertEquals(0, gameEngine.getBlackCaptures());

        // wykonujemy ruch zbijający, zamykając kwadrat
        Position capturingMovePos = new Position(1, 2); // Prawo
        Move capturingMove = new Move(capturingMovePos, blackPlayer);
        MoveResult result = gameEngine.applyMove(capturingMove);

        // Sprawdzamy, czy stan po ruchu jest prawidłowy
        assertTrue(result.isOk(), "Ruch zbijający powinien być prawidłowy");
        
        // Sprawdzamy, czy biały kamień został zbity (pole jest teraz puste)
        assertEquals(StoneColor.EMPTY, board.getStone(whiteStonePos), "Kamień na (1,1) powinien zostać zbity");
        
        // Sprawdzamy, czy czarny kamień, który dokonał zbicia, stoi na swoim miejscu
        assertEquals(StoneColor.BLACK, board.getStone(capturingMovePos));
        
        // Sprawdzamy, czy licznik zbić został zaktualizowany
        assertEquals(1, gameEngine.getBlackCaptures(), "Licznik zbić czarnego gracza powinien wynosić 1");
        
        // Sprawdzamy, czy tura zmieniła się na białego gracza
        assertEquals(whitePlayer, gameEngine.getCurrentPlayer(), "Tura powinna przejść na białego gracza");
    }
     @Test
    void testShouldCaptureFourStoneGroup() {
        // Ustawiamy scenariusz na planszy
        // Tworzymy grupę czterech czarnych kamieni w kwadracie 2x2
        Position blackStone1 = new Position(1, 1);
        Position blackStone2 = new Position(1, 2);
        Position blackStone3 = new Position(2, 1);
        Position blackStone4 = new Position(2, 2);
        board.setStone(blackStone1, StoneColor.BLACK);
        board.setStone(blackStone2, StoneColor.BLACK);
        board.setStone(blackStone3, StoneColor.BLACK);
        board.setStone(blackStone4, StoneColor.BLACK);

        // Otaczamy grupę białymi kamieniami, zostawiając jeden oddech
        board.setStone(new Position(0, 1), StoneColor.WHITE);
        board.setStone(new Position(0, 2), StoneColor.WHITE);
        board.setStone(new Position(3, 1), StoneColor.WHITE);
        board.setStone(new Position(3, 2), StoneColor.WHITE);
        board.setStone(new Position(1, 0), StoneColor.WHITE);
        board.setStone(new Position(2, 0), StoneColor.WHITE);
        board.setStone(new Position(1, 3), StoneColor.WHITE);
        // Ostatni oddech grupy czarnych kamieni jest na pozycji (2, 3)
        gameEngine.changePlayers();
        assertEquals(whitePlayer, gameEngine.getCurrentPlayer(), "Powinna być tura białego gracza");
        assertEquals(0, gameEngine.getWhiteCaptures());

        Position capturingMovePos = new Position(2, 3);
        Move capturingMove = new Move(capturingMovePos, whitePlayer);
        MoveResult result = gameEngine.applyMove(capturingMove);

        //Sprawdzamy, czy stan po ruchu jest prawidłowy
        assertTrue(result.isOk(), "Ruch zbijający grupę powinien być prawidłowy");
        
        // Sprawdzamy, czy wszystkie cztery kamienie zostały zbite
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone1), "Kamień 1 powinien zostać zbity");
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone2), "Kamień 2 powinien zostać zbity");
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone3), "Kamień 3 powinien zostać zbity");
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone4), "Kamień 4 powinien zostać zbity");
        
        // Sprawdzamy, czy licznik zbić został poprawnie zaktualizowany
        assertEquals(4, gameEngine.getWhiteCaptures(), "Licznik zbić białego gracza powinien wynosić 4");
        
        // Sprawdzamy, czy tura zmieniła się z powrotem na czarnego gracza
        assertEquals(blackPlayer, gameEngine.getCurrentPlayer(), "Tura powinna przejść na czarnego gracza");
    }
     @Test
    void testShouldCaptureGroupAgainstWall() {
        // Tworzymy grupę dwóch czarnych kamieni przy górnej krawędzi
        Position blackStone1 = new Position(0, 0);
        Position blackStone2 = new Position(1, 0);
        board.setStone(blackStone1, StoneColor.BLACK);
        board.setStone(blackStone2, StoneColor.BLACK);

        // Otaczamy grupę białymi kamieniami, zostawiając jeden oddech
        // Oddechy tej grupy to (0,1), (1,1), (2,0)
        board.setStone(new Position(0, 1), StoneColor.WHITE);
        board.setStone(new Position(1, 1), StoneColor.WHITE);
        // Ostatni oddech grupy czarnych kamieni jest na pozycji (2, 0)

        // Zmieniamy turę na białego gracza
        gameEngine.changePlayers();
        assertEquals(whitePlayer, gameEngine.getCurrentPlayer());

        // Wykonujemy ruch zbijający na ostatnim oddechu
        Position capturingMovePos = new Position(2, 0);
        Move capturingMove = new Move(capturingMovePos, whitePlayer);
        MoveResult result = gameEngine.applyMove(capturingMove);

        // Sprawdzamy, czy stan po ruchu jest prawidłowy
        assertTrue(result.isOk(), "Ruch zbijający grupę przy ścianie powinien być prawidłowy");

        // Sprawdzamy, czy oba czarne kamienie zostały zbite
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone1), "Kamień 1 przy ścianie powinien zostać zbity");
        assertEquals(StoneColor.EMPTY, board.getStone(blackStone2), "Kamień 2 przy ścianie powinien zostać zbity");

        // Sprawdzamy, czy licznik zbić został poprawnie zaktualizowany
        assertEquals(2, gameEngine.getWhiteCaptures(), "Licznik zbić białego gracza powinien wynosić 2");

        // Sprawdzamy, czy tura zmieniła się z powrotem na czarnego gracza
        assertEquals(blackPlayer, gameEngine.getCurrentPlayer());
    }

      @Test
    void testShouldAllowSuicideMoveBecauseItCaptures() {
        //   kolumny: 0  1  2
        // wiersze
        //    0       B  W  .
        //    1       W  x  W
        //    2       B  W  .
        
        // Ustawiamy białe kamienie
        Position whiteToCapture = new Position(0, 1);
        board.setStone(whiteToCapture, StoneColor.WHITE);
        board.setStone(new Position(1, 0), StoneColor.WHITE);
        board.setStone(new Position(1, 2), StoneColor.WHITE);
        board.setStone(new Position(2, 1), StoneColor.WHITE);

        // Ustawiamy czarne kamienie
        board.setStone(new Position(0, 0), StoneColor.BLACK);
        board.setStone(new Position(0, 2), StoneColor.BLACK);
        
        assertEquals(blackPlayer, gameEngine.getCurrentPlayer());

        // Wykonujemy ruch na (1,1), który jest samobójczy, ale zbija białego na (0,1)
        Position movePos = new Position(1, 1);
        Move move = new Move(movePos, blackPlayer);
        MoveResult result = gameEngine.applyMove(move);

        // Sprawdzamy, czy ruch został poprawnie ZAAKCEPTOWANY
        assertTrue(result.isOk(), "Ruch samobójczy, który zbija, powinien być dozwolony");
        
        // Sprawdzamy, czy biały kamień na (0,1) został zbity
        assertEquals(StoneColor.EMPTY, board.getStone(whiteToCapture), "Biały kamień na (0,1) powinien zostać zbity");
        
        // Sprawdzamy, czy czarny kamień, który wykonał ruch, stoi na planszy
        assertEquals(StoneColor.BLACK, board.getStone(movePos), "Kamień na (1,1) powinien stać na planszy");
        
        // Sprawdzamy, czy licznik zbić został zaktualizowany
        assertEquals(1, gameEngine.getBlackCaptures(), "Licznik zbić czarnego gracza powinien wynosić 1");
    }

     @Test
    void testShouldPreventGroupSuicideMove() {
        //   kolumny: 0  1  2
        //    0       .  W  .
        //    1       W  B  W
        //    2       W  x  W
        //    3       .  W  .
        Position blackStone = new Position(1, 1);
        board.setStone(blackStone, StoneColor.BLACK);

        // Otaczamy go i pole 'x' białymi kamieniami
        board.setStone(new Position(1,0), StoneColor.WHITE);
        board.setStone(new Position(0, 1), StoneColor.WHITE);
        board.setStone(new Position(2, 1), StoneColor.WHITE);
        board.setStone(new Position(2, 2), StoneColor.WHITE);
        board.setStone(new Position(1, 3), StoneColor.WHITE);
        board.setStone(new Position(0, 2), StoneColor.WHITE);
        board.setStone(new Position(0, 1), StoneColor.WHITE); 


        board.setStone(new Position(1, 1), StoneColor.BLACK);
        System.out.println(board);
        assertEquals(blackPlayer, gameEngine.getCurrentPlayer());

        // wykonujemy ruch samobójczy na pozycji (1,2)
        Position groupSuicideMovePos = new Position(1, 2);
        Move suicideMove = new Move(groupSuicideMovePos, blackPlayer);
        MoveResult result = gameEngine.applyMove(suicideMove);

        assertFalse(result.isOk(), "Ruch samobójczy dla grupy powinien być niedozwolony"); // jesli isOk() zwraca false, to ruch jest niedozwolony
        assertEquals("Nie można postawić kamienia - samobójstwo", result.getErrorMessage());
        
        // Sprawdzamy, czy plansza pozostała niezmieniona
        assertEquals(StoneColor.EMPTY, board.getStone(groupSuicideMovePos), "Pole ruchu samobójczego powinno pozostać puste");
        assertEquals(StoneColor.BLACK, board.getStone(blackStone), "Istniejący kamień grupy powinien pozostać na planszy");
        
        // Sprawdzamy, czy tura NIE zmieniła się
        //assertEquals(blackPlayer, gameEngine.getCurrentPlayer(), "Tura powinna pozostać u czarnego gracza");
    }

     @Test 
    void testShouldResign() {
        
        MoveResult moveResign = gameEngine.resign(blackPlayer);
        assertEquals(null, gameEngine.getCurrentPlayer());
        assertEquals(moveResign.isResigned(), true);
        assertEquals(moveResign.getLoser(), blackPlayer);
        assertEquals(moveResign.getWinner(), whitePlayer);
        //sprawdzenie czy nie da się zrobić kolejnych ruchów
        Position testMovePos = new Position(0,0);
        Move moveAfterResign = new Move(testMovePos, whitePlayer);
        MoveResult resultAfterResign = gameEngine.applyMove(moveAfterResign);

        assertFalse(resultAfterResign.isOk(), "Ruch po poddaniu jest niedozwolony");

        MoveResult whiteResign = gameEngine.resign(whitePlayer);
        assertEquals(whiteResign.isResigned(), true, "Gracz powinien móc się poddać nawet poza swoją turą");
        assertEquals(whiteResign.getWinner(), blackPlayer);
    }

    /* @Test
    void testShouldPass() {

        MoveResult movePass = gameEngine.pass(blackPlayer);

        assertEquals(whitePlayer, gameEngine.getCurrentPlayer());
        assertTrue(movePass.isPassed());
        assertEquals(gameEngine.getLastMoveWasPass(), true);
        MoveResult moveSecondPass = gameEngine.pass(whitePlayer);

        assertTrue(moveSecondPass.isPassed());

        assertTrue(moveSecondPass.isEnd(), "Drugi pass powinien zakończyć grę");
        assertFalse(movePass.isResigned(), "Pass nie powinien być rezygnacją");
        assertFalse(moveSecondPass.isResigned(), "Pass nie powinien być rezygnacją");

        Position testMovePos = new Position(0,0);
        Move moveAfterResign = new Move(testMovePos, whitePlayer);
        MoveResult resultAfterResign = gameEngine.applyMove(moveAfterResign);
        
        assertFalse(resultAfterResign.isOk(), "Ruch po zrobieniu pass przez obojga graczy jest niedozwolony");
        assertEquals("Gra została zakończona", resultAfterResign.getErrorMessage());
    
        
    }
    */
     @Test
    void testShouldPreventKo() {
        //ustawiamy planszę na pozycji do ko- czarne "kółko" i prawie skończone białe "kółko"
        //  0 1 2 3
        //1   c b
        //2 c . c b
        //3   c b
        //

        board.setStone(new Position(1,1), StoneColor.BLACK);
        board.setStone(new Position(2, 1), StoneColor.WHITE);
        board.setStone(new Position(0, 2), StoneColor.BLACK);
        board.setStone(new Position(3, 2), StoneColor.WHITE);
        board.setStone(new Position(1, 3), StoneColor.BLACK);
        board.setStone(new Position(2, 3), StoneColor.WHITE);
        board.setStone(new Position(2, 2), StoneColor.BLACK);
        
        gameEngine.changePlayers();
        assertEquals(whitePlayer, gameEngine.getCurrentPlayer());
        //przejmujemy 
        Position capturePosition = new Position(1, 2);
        Move captureMove = new Move(capturePosition, whitePlayer);
        MoveResult captureResult = gameEngine.applyMove(captureMove);
        
        assertTrue(captureResult.isOk());

        Position recapturePosition = new Position(2, 2);
        Move recaptureMove = new Move(recapturePosition, blackPlayer);
        MoveResult blackRecapture = gameEngine.applyMove(recaptureMove);
        assertFalse(blackRecapture.isOk());
        assertEquals("Zaszło ko- ruch nieprawidłowy", blackRecapture.getErrorMessage());


    }

     @Test
    void testShouldFindSingleEmptyRegion() {
        //w tym teście uruchamiamy findemptyregion na pustej planszy i sprawdzamy
        //czy otrzymaliśmy całą planszę

        BoardService service = new BoardService();
        Set<Position> visited = new HashSet<>();

        List<Position> region = service.getEmptyRegion(board, new Position(1, 1), visited);
        assertEquals(81, region.size(), "Cała plansza powinna być pustym terytorium");
    }


@Test
void testShouldFindEnclosedEmptyRegion() {
    //ten test pozwala sprawdzić, czy poprawnie liczony jest pusty region,
    //gdy jest w całości otoczony kamieniami
    //   0 1 2 3 4
    // 0 . B B B .
    // 1 B . . . B
    // 2 B . . . B
    // 3 B . . . B
    // 4 . B B B .

    BoardService service = new BoardService();

    // otaczamy 
    for (int i = 1; i <= 3; i++) {
        board.setStone(new Position(i, 0), StoneColor.BLACK);
        board.setStone(new Position(i, 4), StoneColor.BLACK);
        board.setStone(new Position(0, i), StoneColor.BLACK);
        board.setStone(new Position(4, i), StoneColor.BLACK);
    }

    Set<Position> visited = new HashSet<>();
    List<Position> region = service.getEmptyRegion(
            board,
            new Position(2, 2),
            visited
    );

    assertEquals(9, region.size(), "Środkowy region powinien mieć 9 pól");

    assertTrue(region.contains(new Position(1, 1)));
    assertTrue(region.contains(new Position(3, 3)));
    }

    @Test
void testShouldDetectSeparateEmptyRegions() {
    //Liczenie pustego regionu powinno również działać dla dwóch oddzielonych
    //od siebie regionów
    // . . c . . . . . .
    // . . c . . . . . .
    // . . c . . . . . .
    // . . c . . . . . .
    // . . c . . . . . .
    // . . c . . . . . .
    // . . c . . . . . .
    // . . c . . . . . .
    // . . c . . . . . .

    
    BoardService service = new BoardService();

    for (int y = 0; y < 9; y++) {
        board.setStone(new Position(2, y), StoneColor.BLACK);
    }

    Set<Position> visited = new HashSet<>();

    List<Position> leftRegion = service.getEmptyRegion(
            board,
            new Position(0, 1),
            visited
    );

    List<Position> rightRegion = service.getEmptyRegion(
            board,
            new Position(4, 1),
            visited
    );

    assertEquals(18, leftRegion.size());
    assertEquals(54, rightRegion.size());
    }
   @Test
    void testShouldCalculateScoresCorrectlyOn9x9Board() {
        // Ustawiamy scenariusz zakończonej gry na planszy 9x9
        
        // Terytorium czarnego w lewym górnym rogu (2x2 = 4 punkty)
        // Otaczamy obszar od (0,0) do (1,1)
        board.setStone(new Position(0, 2), StoneColor.BLACK);
        board.setStone(new Position(1, 2), StoneColor.BLACK);
        board.setStone(new Position(2, 2), StoneColor.BLACK);
        board.setStone(new Position(2, 1), StoneColor.BLACK);
        board.setStone(new Position(2, 0), StoneColor.BLACK);

        // Terytorium białego w prawym dolnym rogu (1 punkt)
        // Otaczamy pole (8,8)
        board.setStone(new Position(7, 8), StoneColor.WHITE);
        board.setStone(new Position(8, 7), StoneColor.WHITE);

        // Symulujemy, że w trakcie gry gracze zdobyli jeńców
        gameEngine.updateCaptureCounts(StoneColor.BLACK, 3); // Czarny zbił 3 kamienie
        gameEngine.updateCaptureCounts(StoneColor.WHITE, 5); // Biały zbił 5 kamieni

        // Wypisanie planszy do konsoli w celu weryfikacji wizualnej
        System.out.println("--- Plansza 9x9 do liczenia punktów ---");
        System.out.println(board);

        // Wywołujemy metodę liczenia punktów
        ScoreResult scores = gameEngine.calculateScores();

        // ASSERT - Sprawdzamy, czy wyniki są zgodne z oczekiwaniami
        // Wynik czarnego = 4 (terytorium) + 3 (zbicia) = 7
        // Wynik białego = 1 (terytorium) + 5 (zbicia) = 6
        
        assertEquals(7, scores.getBlackScore(), "Wynik czarnego powinien wynosić 7");
        assertEquals(6, scores.getWhiteScore(), "Wynik białego powinien wynosić 6");
        
        // Sprawdzamy, czy zwycięzca został poprawnie określony
        assertEquals(blackPlayer, scores.getWinner(), "Zwycięzcą powinien być czarny gracz");
    }
}