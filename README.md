# PROEJEKT GO 2.0

Uruchamianie aplikacji

Do uruchomienia gry wymagane są trzy osobne terminale.

Uruchomienie serwera

mvn exec:java

Uruchomienie klientów (dla każdego gracza osobno)

mvn javafx:run

Serwer obsługuje jednocześnie dwóch klientów, którzy rozgrywają jedną partię gry.

Zasady rozgrywki

W trakcie gry dostępne są trzy podstawowe akcje:

1. Stawianie kamieni

Gracze wykonują ruchy poprzez klikanie myszką na planszy. Ruchy są walidowane zgodnie z zasadami gry Go.

2. Pass (pas) – tryb negocjacji

Akcja Pass uruchamia tryb negocjacji martwych grup.

Gracze zaznaczają na planszy kamienie, które uznają za jeńców (martwe grupy).
Po zakończeniu zaznaczania gracz zatwierdza wybór przyciskiem Done i oczekuje na decyzję przeciwnika.

jeśli obaj gracze wskażą te same pozycje, gra zostaje zakończona i następuje podliczenie punktów

w przypadku braku porozumienia negocjacje zostają anulowane, a gra toczy się dalej.

3. Resign (poddanie się)

Gracz może w dowolnym momencie poddać partię. Rezygnacja natychmiast kończy grę i przyznaje zwycięstwo przeciwnikowi.
