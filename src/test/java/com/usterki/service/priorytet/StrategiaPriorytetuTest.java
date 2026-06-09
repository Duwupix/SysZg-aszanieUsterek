package com.usterki.service.priorytet;

import com.usterki.model.KategoriaUsterki;
import com.usterki.model.Zgloszenie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Testy wzorca Strategia (Strategy) — porównanie dwóch wymiennych algorytmów priorytetu. */
class StrategiaPriorytetuTest {

    private KategoriaUsterki kategoria(int domyslnyPriorytet, double waga) {
        KategoriaUsterki k = new KategoriaUsterki();
        k.setDomyslnyPriorytet(domyslnyPriorytet);
        k.setWspolczynnikWagi(BigDecimal.valueOf(waga));
        return k;
    }

    private Zgloszenie zgloszenie(KategoriaUsterki kat, Zgloszenie.Pilnosc pilnosc) {
        Zgloszenie z = new Zgloszenie();
        z.setKategoria(kat);
        z.setPilnosc(pilnosc);
        return z;
    }

    @Test
    @DisplayName("Strategia ważona uwzględnia wagę i pilność (50 × 1.0 × 0.5 = 25)")
    void wazona_uwzgledniaPilnosc() {
        StrategiaPriorytetu strategia = new WazonaStrategiaPriorytetu();
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.NATYCHMIASTOWA);

        assertThat(strategia.oblicz(z)).isEqualTo(25);
    }

    @Test
    @DisplayName("Strategia prosta ignoruje pilność i zwraca domyślny priorytet kategorii")
    void prosta_zwracaDomyslnyPriorytet() {
        StrategiaPriorytetu strategia = new ProstaStrategiaPriorytetu();
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.NATYCHMIASTOWA);

        assertThat(strategia.oblicz(z)).isEqualTo(50);
    }

    @Test
    @DisplayName("Obie strategie przycinają wynik do zakresu 1–100")
    void obie_przycinajaDoZakresu() {
        StrategiaPriorytetu wazona = new WazonaStrategiaPriorytetu();
        StrategiaPriorytetu prosta = new ProstaStrategiaPriorytetu();
        // 90 × 2.0 = 180 → clamp do 100
        Zgloszenie z = zgloszenie(kategoria(90, 2.0), Zgloszenie.Pilnosc.SREDNIA);

        assertThat(wazona.oblicz(z)).isEqualTo(100);
        assertThat(prosta.oblicz(z)).isEqualTo(90);
    }

    @Test
    @DisplayName("Dla tych samych danych strategie dają różne wyniki — potwierdza wymienność algorytmu")
    void strategie_dajaRozneWyniki() {
        Zgloszenie z = zgloszenie(kategoria(60, 1.0), Zgloszenie.Pilnosc.NISKA);

        int wynikWazony = new WazonaStrategiaPriorytetu().oblicz(z); // 60 × 1.3 = 78
        int wynikProsty = new ProstaStrategiaPriorytetu().oblicz(z); // 60

        assertThat(wynikWazony).isNotEqualTo(wynikProsty);
        assertThat(wynikWazony).isEqualTo(78);
        assertThat(wynikProsty).isEqualTo(60);
    }
}
