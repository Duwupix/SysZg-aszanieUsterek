package com.usterki.service.priorytet;

import com.usterki.model.Zgloszenie;

/**
 * Wzorzec projektowy: STRATEGIA (Strategy) — grupa czynnościowa.
 *
 * Problem: sposób obliczania automatycznego priorytetu zgłoszenia był zaszyty
 * w jednej metodzie z instrukcją {@code switch}. Dodanie alternatywnego sposobu
 * liczenia (np. uproszczonego na potrzeby raportów) wymagało ingerencji w
 * istniejący kod, co łamie zasadę otwarte/zamknięte (OCP).
 *
 * Rozwiązanie: wspólny interfejs definiuje wymienny algorytm obliczania
 * priorytetu. Konkretne strategie ({@link WazonaStrategiaPriorytetu},
 * {@link ProstaStrategiaPriorytetu}) realizują go na różne sposoby, a kontekst
 * ({@code PriorytetService}) deleguje do wstrzykniętej strategii — bez wiedzy,
 * która to konkretnie.
 */
public interface StrategiaPriorytetu {

    /** Oblicza automatyczny priorytet (1–100; mniejsza wartość = wyższy priorytet). */
    int oblicz(Zgloszenie zgloszenie);
}
