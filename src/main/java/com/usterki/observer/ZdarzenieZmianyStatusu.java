package com.usterki.observer;

import com.usterki.model.Uzytkownik;
import com.usterki.model.Zgloszenie;
import com.usterki.model.Zgloszenie.Status;

/**
 * Zdarzenie zmiany statusu zgłoszenia — niezmienny obiekt danych przekazywany
 * obserwatorom we wzorcu Obserwator. Przy utworzeniu zgłoszenia {@code staryStatus}
 * jest {@code null}.
 */
public record ZdarzenieZmianyStatusu(
        Zgloszenie zgloszenie,
        Status staryStatus,
        Status nowyStatus,
        Uzytkownik uzytkownik,
        String komentarz) {
}
