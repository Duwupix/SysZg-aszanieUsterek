package com.usterki.service;

import com.usterki.model.*;
import com.usterki.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe ZgloszenieService (Mockito, bez kontekstu Spring).
 * Weryfikują kluczową logikę: tworzenie zgłoszeń, zmianę statusów,
 * zapis historii oraz walidację danych wejściowych.
 */
@ExtendWith(MockitoExtension.class)
class ZgloszenieServiceTest {

    @Mock private ZgloszenieRepository zgloszenieRepo;
    @Mock private UzytkownikRepository uzytkownikRepo;
    @Mock private KategoriaUsterkiRepository kategoriaRepo;
    @Mock private HistoriaStatusowRepository historiaRepo;
    @Mock private PriorytetService priorytetService;

    private ZgloszenieService serwis;

    private Uzytkownik uzytkownik;
    private KategoriaUsterki kategoria;

    @BeforeEach
    void setUp() {
        serwis = new ZgloszenieService(
                zgloszenieRepo, uzytkownikRepo, kategoriaRepo, historiaRepo, priorytetService);

        uzytkownik = new Uzytkownik();
        uzytkownik.setId(1L);
        uzytkownik.setLogin("jan.kowalski");
        uzytkownik.setEmail("jan@miasto.pl");
        uzytkownik.setImie("Jan");
        uzytkownik.setNazwisko("Kowalski");
        uzytkownik.setHasloHash("$2a$10$hash");
        uzytkownik.setRola(Uzytkownik.Rola.ZGLASZAJACY);

        kategoria = new KategoriaUsterki();
        kategoria.setId(1L);
        kategoria.setNazwa("Drogi i chodniki");
        kategoria.setDomyslnyPriorytet(50);
        kategoria.setWspolczynnikWagi(BigDecimal.ONE);
        kategoria.setSzacCzasGodz(4);
    }

    // ── utworz() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Nowe zgłoszenie otrzymuje status NOWE")
    void utworz_noweZgloszenie_statusNOWE() {
        stubUtworzZgloszenie();

        Zgloszenie z = serwis.utworz(1L, 1L, "Dziura w jezdni", "Głęboka dziura",
                Zgloszenie.Pilnosc.SREDNIA, "ul. Testowa 1", null);

        assertThat(z.getStatus()).isEqualTo(Zgloszenie.Status.NOWE);
    }

    @Test
    @DisplayName("Nowe zgłoszenie przechowuje tytuł, opis, adres i pilność")
    void utworz_ustawiaWszystkiePola() {
        stubUtworzZgloszenie();

        Zgloszenie z = serwis.utworz(1L, 1L, "Uszkodzona latarnia",
                "Latarnia nie świeci od tygodnia",
                Zgloszenie.Pilnosc.WYSOKA, "ul. Słoneczna 5", null);

        assertThat(z.getTytul()).isEqualTo("Uszkodzona latarnia");
        assertThat(z.getOpis()).isEqualTo("Latarnia nie świeci od tygodnia");
        assertThat(z.getAdres()).isEqualTo("ul. Słoneczna 5");
        assertThat(z.getPilnosc()).isEqualTo(Zgloszenie.Pilnosc.WYSOKA);
    }

    @Test
    @DisplayName("Numer zgłoszenia ma format ZGL-YYYYMMDD-XXXXXX")
    void utworz_generujeNumerWPoprawnymFormacie() {
        stubUtworzZgloszenie();

        Zgloszenie z = serwis.utworz(1L, 1L, "Awaria kanalizacji", "Opis",
                Zgloszenie.Pilnosc.NATYCHMIASTOWA, "Rynek 1", null);

        assertThat(z.getNumerZgloszenia()).matches("ZGL-\\d{8}-\\d{6}");
    }

    @Test
    @DisplayName("Tworzenie zgłoszenia wywołuje zapis do historii statusów (stary=null, nowy=NOWE)")
    void utworz_zapiszWpisWHistoriiStatusow() {
        stubUtworzZgloszenie();

        serwis.utworz(1L, 1L, "Test", "Opis", Zgloszenie.Pilnosc.SREDNIA, "Adres", null);

        verify(historiaRepo, times(1)).save(any(HistoriaStatusow.class));
    }

    @Test
    @DisplayName("Tworzenie zgłoszenia wywołuje przeliczenie priorytetu")
    void utworz_wywolujeObliczeniePriorytetu() {
        stubUtworzZgloszenie();

        serwis.utworz(1L, 1L, "Test", "Opis", Zgloszenie.Pilnosc.NISKA, "Adres", null);

        verify(priorytetService, times(1)).zaktualizujPriorytet(any(Zgloszenie.class));
    }

    @Test
    @DisplayName("Tworzenie zgłoszenia: nieistniejący użytkownik → IllegalArgumentException")
    void utworz_brakUzytkownika_rzucaWyjatek() {
        when(uzytkownikRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                serwis.utworz(99L, 1L, "T", "O", Zgloszenie.Pilnosc.SREDNIA, "A", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("użytkownika");
    }

    @Test
    @DisplayName("Tworzenie zgłoszenia: nieistniejąca kategoria → IllegalArgumentException")
    void utworz_brakKategorii_rzucaWyjatek() {
        when(uzytkownikRepo.findById(1L)).thenReturn(Optional.of(uzytkownik));
        when(kategoriaRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                serwis.utworz(1L, 99L, "T", "O", Zgloszenie.Pilnosc.SREDNIA, "A", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kategorii");
    }

    // ── zmienStatus() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Zmiana statusu na ZAMKNIETE ustawia pole zamknieto")
    void zmienStatus_naZAMKNIETE_ustawiaZamknieto() {
        Zgloszenie z = noweZgloszenie();
        z.setStatus(Zgloszenie.Status.ROZWIAZANE);

        stubZmienStatus(z);

        Zgloszenie wynik = serwis.zmienStatus(10L, Zgloszenie.Status.ZAMKNIETE, 1L, "ok");

        assertThat(wynik.getZamknieto()).isNotNull();
        assertThat(wynik.getStatus()).isEqualTo(Zgloszenie.Status.ZAMKNIETE);
    }

    @Test
    @DisplayName("Zmiana statusu na ROZWIAZANE ustawia pole zamknieto")
    void zmienStatus_naROZWIAZANE_ustawiaZamknieto() {
        Zgloszenie z = noweZgloszenie();
        z.setStatus(Zgloszenie.Status.W_TOKU);

        stubZmienStatus(z);

        Zgloszenie wynik = serwis.zmienStatus(10L, Zgloszenie.Status.ROZWIAZANE, 1L, "naprawione");

        assertThat(wynik.getZamknieto()).isNotNull();
    }

    @Test
    @DisplayName("Zmiana statusu na W_TOKU nie ustawia pola zamknieto")
    void zmienStatus_naWTOKU_nieUstawiaZamknieto() {
        Zgloszenie z = noweZgloszenie();
        z.setStatus(Zgloszenie.Status.NOWE);

        stubZmienStatus(z);

        Zgloszenie wynik = serwis.zmienStatus(10L, Zgloszenie.Status.W_TOKU, 1L, "start");

        assertThat(wynik.getZamknieto()).isNull();
    }

    @Test
    @DisplayName("Zmiana statusu zapisuje wpis w historii")
    void zmienStatus_zapiszWpisWHistorii() {
        Zgloszenie z = noweZgloszenie();
        stubZmienStatus(z);

        serwis.zmienStatus(10L, Zgloszenie.Status.W_TOKU, 1L, "start");

        verify(historiaRepo, times(1)).save(any(HistoriaStatusow.class));
    }

    @Test
    @DisplayName("Zmiana statusu: nieistniejące zgłoszenie → IllegalArgumentException")
    void zmienStatus_brakZgloszenia_rzucaWyjatek() {
        when(zgloszenieRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                serwis.zmienStatus(999L, Zgloszenie.Status.ZAMKNIETE, 1L, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Zmiana statusu: nieistniejący użytkownik → IllegalArgumentException")
    void zmienStatus_brakUzytkownika_rzucaWyjatek() {
        Zgloszenie z = noweZgloszenie();
        when(zgloszenieRepo.findById(10L)).thenReturn(Optional.of(z));
        when(uzytkownikRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                serwis.zmienStatus(10L, Zgloszenie.Status.W_TOKU, 99L, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── pobierzZgloszeniePoId() ───────────────────────────────────────────

    @Test
    @DisplayName("pobierzZgloszeniePoId: istniejące ID zwraca zgłoszenie")
    void pobierzZgloszeniePoId_istniejaceId_zwracaZgloszenie() {
        Zgloszenie z = noweZgloszenie();
        when(zgloszenieRepo.findById(10L)).thenReturn(Optional.of(z));

        Zgloszenie wynik = serwis.pobierzZgloszeniePoId(10L);

        assertThat(wynik).isSameAs(z);
    }

    @Test
    @DisplayName("pobierzZgloszeniePoId: nieistniejące ID → IllegalArgumentException zawierający ID")
    void pobierzZgloszeniePoId_brakZgloszenia_rzucaWyjatek() {
        when(zgloszenieRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serwis.pobierzZgloszeniePoId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("999");
    }

    // ── Pomocniki ─────────────────────────────────────────────────────────

    /** Konfiguruje mocki dla metody `utworz`. */
    private void stubUtworzZgloszenie() {
        when(uzytkownikRepo.findById(1L)).thenReturn(Optional.of(uzytkownik));
        when(kategoriaRepo.findById(1L)).thenReturn(Optional.of(kategoria));
        when(zgloszenieRepo.count()).thenReturn(0L);
        when(zgloszenieRepo.save(any(Zgloszenie.class))).thenAnswer(inv -> inv.getArgument(0));
        when(historiaRepo.save(any(HistoriaStatusow.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /** Buduje zgłoszenie testowe. */
    private Zgloszenie noweZgloszenie() {
        Zgloszenie z = new Zgloszenie();
        z.setId(10L);
        z.setStatus(Zgloszenie.Status.NOWE);
        z.setKategoria(kategoria);
        z.setTytul("Testowe zgłoszenie");
        z.setOpis("Opis testowy");
        return z;
    }

    /** Konfiguruje mocki dla metody `zmienStatus`. */
    private void stubZmienStatus(Zgloszenie z) {
        when(zgloszenieRepo.findById(10L)).thenReturn(Optional.of(z));
        when(uzytkownikRepo.findById(1L)).thenReturn(Optional.of(uzytkownik));
        when(zgloszenieRepo.save(any(Zgloszenie.class))).thenAnswer(inv -> inv.getArgument(0));
        when(historiaRepo.save(any(HistoriaStatusow.class))).thenAnswer(inv -> inv.getArgument(0));
    }
}
