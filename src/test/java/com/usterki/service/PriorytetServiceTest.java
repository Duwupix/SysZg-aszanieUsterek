package com.usterki.service;

import com.usterki.model.KategoriaUsterki;
import com.usterki.model.Zgloszenie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe algorytmu obliczania priorytetu (PriorytetService.oblicz).
 * Nie wymagają kontekstu Spring — testują czystą logikę biznesową.
 */
class PriorytetServiceTest {

    private PriorytetService serwis;

    @BeforeEach
    void setUp() {
        serwis = new PriorytetService();
    }

    // ── Pomocniki budujące obiekty testowe ─────────────────────────────────

    private KategoriaUsterki kategoria(int domyslnyPriorytet, double wspolczynnikWagi) {
        KategoriaUsterki k = new KategoriaUsterki();
        k.setDomyslnyPriorytet(domyslnyPriorytet);
        k.setWspolczynnikWagi(BigDecimal.valueOf(wspolczynnikWagi));
        k.setSzacCzasGodz(4);
        return k;
    }

    private Zgloszenie zgloszenie(KategoriaUsterki kat, Zgloszenie.Pilnosc pilnosc) {
        Zgloszenie z = new Zgloszenie();
        z.setKategoria(kat);
        z.setPilnosc(pilnosc);
        z.setTytul("Testowe zgłoszenie");
        z.setOpis("Opis testowy");
        return z;
    }

    // ── Priorytet ręczny ───────────────────────────────────────────────────

    @Test
    @DisplayName("Priorytet ręczny jest zwracany bez przeliczania")
    void oblicz_priorytetRecznyUstawiony_zwracanaBezModyfikacji() {
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.NISKA);
        z.setPriorytetReczny(30);

        assertThat(serwis.oblicz(z)).isEqualTo(30);
    }

    @Test
    @DisplayName("Priorytet ręczny > 100 jest przycinany do 100")
    void oblicz_priorytetRecznyPowyżej100_clampDoStu() {
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.SREDNIA);
        z.setPriorytetReczny(150);

        assertThat(serwis.oblicz(z)).isEqualTo(100);
    }

    @Test
    @DisplayName("Priorytet ręczny < 1 jest przycinany do 1")
    void oblicz_priorytetRecznyPonizejJednego_clampDoJednego() {
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.SREDNIA);
        z.setPriorytetReczny(0);

        assertThat(serwis.oblicz(z)).isEqualTo(1);
    }

    // ── Mnożniki pilności ──────────────────────────────────────────────────

    @Test
    @DisplayName("Pilność NATYCHMIASTOWA mnoży przez 0.50 — zmniejsza priorytet o połowę")
    void oblicz_natychmiastowa_mnozy050() {
        // 50 * 1.0 * 0.50 * 1.0 = 25
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.NATYCHMIASTOWA);

        assertThat(serwis.oblicz(z)).isEqualTo(25);
    }

    @Test
    @DisplayName("Pilność WYSOKA mnoży przez 0.70")
    void oblicz_wysoka_mnozy070() {
        // 50 * 1.0 * 0.70 * 1.0 = 35
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.WYSOKA);

        assertThat(serwis.oblicz(z)).isEqualTo(35);
    }

    @Test
    @DisplayName("Pilność SREDNIA mnoży przez 1.00 — brak zmiany")
    void oblicz_srednia_brakZmiany() {
        // 50 * 1.0 * 1.00 * 1.0 = 50
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.SREDNIA);

        assertThat(serwis.oblicz(z)).isEqualTo(50);
    }

    @Test
    @DisplayName("Pilność NISKA mnoży przez 1.30 — zwiększa priorytet")
    void oblicz_niska_mnozy130() {
        // 50 * 1.0 * 1.30 * 1.0 = 65
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.NISKA);

        assertThat(serwis.oblicz(z)).isEqualTo(65);
    }

    // ── Przycinanie wyniku ─────────────────────────────────────────────────

    @Test
    @DisplayName("Wynik powyżej 100 (wysoki domyślny + duży współczynnik) jest przycinany do 100")
    void oblicz_wynikPowyżej100_clampDoStu() {
        // 80 * 1.5 * 1.0 * 1.0 = 120 → clamp(120) = 100
        Zgloszenie z = zgloszenie(kategoria(80, 1.5), Zgloszenie.Pilnosc.SREDNIA);

        assertThat(serwis.oblicz(z)).isEqualTo(100);
    }

    // ── Mnożnik zwłoki ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Brak terminu realizacji → mnożnik zwłoki = 1.0, brak redukcji")
    void oblicz_bezTerminu_brakRedukcji() {
        // 50 * 1.0 * 1.0 * 1.0 = 50
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.SREDNIA);
        z.setDataZauwazeniaUsterki(null);

        assertThat(serwis.oblicz(z)).isEqualTo(50);
    }

    @Test
    @DisplayName("Przyszły termin realizacji → mnożnik zwłoki = 1.0, brak redukcji")
    void oblicz_przyszlyTermin_brakRedukcji() {
        // termin w przyszłości → mnoznikZwloki = 1.0
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.SREDNIA);
        z.setDataZauwazeniaUsterki(LocalDateTime.now().plusDays(7));

        assertThat(serwis.oblicz(z)).isEqualTo(50);
    }

    @Test
    @DisplayName("Miniony termin (24h temu) nieznacznie redukuje priorytet")
    void oblicz_przeszlyTermin24h_redukujePriorytet() {
        // godziny = 24
        // mnoznikZwloki = max(0.5, 1.0 - (24/24.0)*0.05) = max(0.5, 0.95) = 0.95
        // 50 * 1.0 * 1.0 * 0.95 = 47.5 → round = 48
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.SREDNIA);
        z.setDataZauwazeniaUsterki(LocalDateTime.now().minusHours(24));

        assertThat(serwis.oblicz(z)).isEqualTo(48);
    }

    @Test
    @DisplayName("Bardzo stary termin → mnożnik zwłoki nie schodzi poniżej 0.50")
    void oblicz_bardzoStaryTermin_mnoznikMinimum050() {
        // godziny = 480 (20 dni)
        // 1.0 - (480/24.0)*0.05 = 1.0 - 1.0 = 0.0 → max(0.5, 0.0) = 0.5
        // 50 * 1.0 * 1.0 * 0.5 = 25
        Zgloszenie z = zgloszenie(kategoria(50, 1.0), Zgloszenie.Pilnosc.SREDNIA);
        z.setDataZauwazeniaUsterki(LocalDateTime.now().minusDays(20));

        assertThat(serwis.oblicz(z)).isEqualTo(25);
    }

    // ── zaktualizujPriorytet ───────────────────────────────────────────────

    @Test
    @DisplayName("zaktualizujPriorytet zapisuje obliczony priorytet do pola priorytetObliczony")
    void zaktualizujPriorytet_ustawiaPolePriorytetObliczony() {
        // 40 * 2.0 * 0.50 = 40 → clamp(40) = 40
        Zgloszenie z = zgloszenie(kategoria(40, 2.0), Zgloszenie.Pilnosc.NATYCHMIASTOWA);

        serwis.zaktualizujPriorytet(z);

        assertThat(z.getPriorytetObliczony()).isEqualTo(40);
    }
}
