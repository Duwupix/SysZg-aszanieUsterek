package com.usterki.model;

import java.time.LocalDateTime;

/**
 * Wzorzec projektowy: BUDOWNICZY (Builder) — grupa konstrukcyjna.
 *
 * Problem: encja {@link Zgloszenie} ma kilkanaście pól. Tworzenie jej w serwisie
 * polegało na długim, podatnym na pomyłki ciągu setterów — łatwo pominąć pole
 * albo pomylić kolejność, a sam proces składania mieszał się z logiką biznesową.
 *
 * Rozwiązanie: Budowniczy oddziela proces krok-po-kroku składania obiektu od
 * jego docelowej reprezentacji i udostępnia płynne (fluent) API. Metoda
 * {@link #build()} dodatkowo waliduje pola wymagane przed zwróceniem gotowego
 * obiektu, dzięki czemu nie powstanie zgłoszenie w niespójnym stanie.
 */
public class ZgloszenieBuilder {

    private final Zgloszenie produkt = new Zgloszenie();

    public ZgloszenieBuilder numer(String numer) {
        produkt.setNumerZgloszenia(numer);
        return this;
    }

    public ZgloszenieBuilder zglaszajacy(Uzytkownik zglaszajacy) {
        produkt.setZglaszajacy(zglaszajacy);
        return this;
    }

    public ZgloszenieBuilder kategoria(KategoriaUsterki kategoria) {
        produkt.setKategoria(kategoria);
        return this;
    }

    public ZgloszenieBuilder tytul(String tytul) {
        produkt.setTytul(tytul);
        return this;
    }

    public ZgloszenieBuilder opis(String opis) {
        produkt.setOpis(opis);
        return this;
    }

    public ZgloszenieBuilder pilnosc(Zgloszenie.Pilnosc pilnosc) {
        produkt.setPilnosc(pilnosc);
        return this;
    }

    public ZgloszenieBuilder adres(String adres) {
        produkt.setAdres(adres);
        return this;
    }

    public ZgloszenieBuilder dataZauwazeniaUsterki(LocalDateTime data) {
        produkt.setDataZauwazeniaUsterki(data);
        return this;
    }

    public ZgloszenieBuilder status(Zgloszenie.Status status) {
        produkt.setStatus(status);
        return this;
    }

    /** Zwraca gotowy, złożony obiekt po walidacji pól wymaganych. */
    public Zgloszenie build() {
        if (produkt.getTytul() == null || produkt.getTytul().isBlank())
            throw new IllegalStateException("Zgłoszenie musi mieć tytuł");
        if (produkt.getKategoria() == null)
            throw new IllegalStateException("Zgłoszenie musi mieć kategorię");
        if (produkt.getZglaszajacy() == null)
            throw new IllegalStateException("Zgłoszenie musi mieć zgłaszającego");
        return produkt;
    }
}
