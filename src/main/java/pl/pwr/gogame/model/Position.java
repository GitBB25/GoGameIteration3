package pl.pwr.gogame.model;

/**
 * Rekord {@code Position} reprezentuje niezmienną pozycję
 * na planszy gry Go, określoną przez kolumnę i wiersz.
 *
 * Jest to przykład wzorca projektowego Value Object.
 */
//wzorzec: Value Object
//rekord pozwala nam na łatwe tworzenie danych które nie będą się zmieniać. Do elementu rekordu, np. x, dostajemy się poprzez position.x()
public record Position(int col, int row) {
}
