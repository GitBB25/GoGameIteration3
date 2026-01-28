package pl.pwr.gogame.model;

import java.util.Set;

/**
 * Klasa {@code BoardRegion} reprezentuje region planszy gry Go,
 * wykorzystywany podczas analizy terytoriów.
 * <p>
 * Region składa się z zestawu połączonych pozycji planszy oraz
 * zbioru kolorów kamieni, które bezpośrednio go otaczają.
 * Na podstawie tych informacji można określić, do którego gracza
 * należy dane terytorium.
 * </p>
 */
//ta klasa jest nam potrzebna do sprawdzania typów terytorium. 
public class BoardRegion {

    /**
     * Zbiór pozycji należących do danego regionu planszy.
     */
    Set<Position> points;

    /**
     * Zbiór kolorów kamieni otaczających region.
     * Jeśli cały region jest otoczony jednym kolorem,
     * terytorium należy do gracza tego koloru.
     */
    //tutaj zbieramy kolory kamieni otaczających terytorium. Jeśli całe terytorium jest otoczone
    //jednym kolorem, należy ono do gracza tego koloru
    Set<StoneColor> borderingColors;
}
