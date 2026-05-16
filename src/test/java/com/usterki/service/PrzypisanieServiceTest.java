package com.usterki.service;

import com.usterki.model.*;
import com.usterki.model.PrzypisanieTechnika.StatusPrzypisania;
import com.usterki.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrzypisanieServiceTest {

    @Mock private ZgloszenieRepository zgloszenieRepo;
    @Mock private UzytkownikRepository uzytkownikRepo;
    @Mock private PrzypisanieTechnikaRepository przypisanieRepo;
    @Mock private PrzypisanieService selfMock;   // proxy do obsługi transakcji / retry

    private PrzypisanieService serwis;

    private Uzytkownik technik;
    private Uzytkownik admin;
    private Zgloszenie zgloszenie;
    private KategoriaUsterki kategoria;

    @BeforeEach
    void setUp() {
        serwis = new PrzypisanieService(zgloszenieRepo, uzytkownikRepo, przypisanieRepo, selfMock);

        kategoria = new KategoriaUsterki();
        kategoria.setId(1L);
        kategoria.setNazwa("Kanalizacja");
        kategoria.setDomyslnyPriorytet(50);
        kategoria.setWspolczynnikWagi(BigDecimal.ONE);
        kategoria.setSzacCzasGodz(6);

        zgloszenie = new Zgloszenie();
        zgloszenie.setId(10L);
        zgloszenie.setStatus(Zgloszenie.Status.NOWE);
        zgloszenie.setKategoria(kategoria);
        zgloszenie.setTytul("Awaria kanalizacji");
        zgloszenie.setOpis("Opis usterki");

        technik = new Uzytkownik();
        technik.setId(2L);
        technik.setLogin("technik1");
        technik.setEmail("tech@miasto.pl");
        technik.setImie("Adam");
        technik.setNazwisko("Nowak");
        technik.setHasloHash("$2a$10$hash");
        technik.setRola(Uzytkownik.Rola.TECHNIK);

        admin = new Uzytkownik();
        admin.setId(1L);
        admin.setLogin("admin");
        admin.setEmail("admin@miasto.pl");
        admin.setImie("Karol");
        admin.setNazwisko("Wiśniewski");
        admin.setHasloHash("$2a$10$hash");
        admin.setRola(Uzytkownik.Rola.ADMINISTRATOR);
    }

    // przypiszWTransakcji() — walidacja

    @Test
    @DisplayName("Przypisanie: brak zgłoszenia → IllegalArgumentException z ID w komunikacie")
    void przypiszWTransakcji_brakZgloszenia_rzucaWyjatek() {
        when(zgloszenieRepo.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serwis.przypiszWTransakcji(99L, 2L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Przypisanie: zgłoszenie ma już AKTYWNE przypisanie → IllegalStateException")
    void przypiszWTransakcji_duplikatAktywnego_rzucaWyjatek() {
        when(zgloszenieRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(zgloszenie));
        when(przypisanieRepo.existsByZgloszenieIdAndStatusPrzypisania(10L, StatusPrzypisania.AKTYWNE))
                .thenReturn(true);

        assertThatThrownBy(() -> serwis.przypiszWTransakcji(10L, 2L, 1L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("aktywnie przypisanego");
    }

    @Test
    @DisplayName("Przypisanie: technik nie istnieje → IllegalArgumentException z ID w komunikacie")
    void przypiszWTransakcji_brakTechnika_rzucaWyjatek() {
        when(zgloszenieRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(zgloszenie));
        when(przypisanieRepo.existsByZgloszenieIdAndStatusPrzypisania(10L, StatusPrzypisania.AKTYWNE))
                .thenReturn(false);
        when(uzytkownikRepo.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serwis.przypiszWTransakcji(10L, 77L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("77");
    }

    @Test
    @DisplayName("Przypisanie: przypisujący nie istnieje → IllegalArgumentException z ID w komunikacie")
    void przypiszWTransakcji_brakPrzypisujacego_rzucaWyjatek() {
        when(zgloszenieRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(zgloszenie));
        when(przypisanieRepo.existsByZgloszenieIdAndStatusPrzypisania(10L, StatusPrzypisania.AKTYWNE))
                .thenReturn(false);
        when(uzytkownikRepo.findById(2L)).thenReturn(Optional.of(technik));
        when(uzytkownikRepo.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serwis.przypiszWTransakcji(10L, 2L, 88L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("88");
    }

    // przypiszWTransakcji() — skutki uboczne

    @Test
    @DisplayName("Pomyślne przypisanie zmienia status zgłoszenia na W_TOKU")
    void przypiszWTransakcji_poprawne_ustawiaStatusWToku() {
        stubPoprawnePrzypisanie();

        serwis.przypiszWTransakcji(10L, 2L, 1L, null);

        assertThat(zgloszenie.getStatus()).isEqualTo(Zgloszenie.Status.W_TOKU);
    }

    @Test
    @DisplayName("Pomyślne przypisanie zwraca obiekt z technikiem i statusem AKTYWNE")
    void przypiszWTransakcji_poprawne_zwracaAktywneZTechnikiem() {
        stubPoprawnePrzypisanie();

        PrzypisanieTechnika wynik = serwis.przypiszWTransakcji(10L, 2L, 1L, null);

        assertThat(wynik.getStatusPrzypisania()).isEqualTo(StatusPrzypisania.AKTYWNE);
        assertThat(wynik.getTechnik()).isEqualTo(technik);
        assertThat(wynik.getPrzypisujacy()).isEqualTo(admin);
    }

    @Test
    @DisplayName("Pomyślne przypisanie oblicza planowane zakończenie wg szacowanego czasu kategorii")
    void przypiszWTransakcji_obliczaPlanowaneZakonczenie() {
        // kategoria.szacCzasGodz = 6, więc zakończenie = start + 6h
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 9, 0);
        stubPoprawnePrzypisanie();

        PrzypisanieTechnika wynik = serwis.przypiszWTransakcji(10L, 2L, 1L, start);

        assertThat(wynik.getPlanowaneZakonczenie()).isEqualTo(start.plusHours(6));
        assertThat(wynik.getPlanowanyStart()).isEqualTo(start);
    }

    @Test
    @DisplayName("Pomyślne przypisanie zapisuje zarówno zgłoszenie jak i przypisanie")
    void przypiszWTransakcji_zapisujeObaObiekty() {
        stubPoprawnePrzypisanie();

        serwis.przypiszWTransakcji(10L, 2L, 1L, null);

        verify(zgloszenieRepo, times(1)).save(zgloszenie);
        verify(przypisanieRepo, times(1)).save(any(PrzypisanieTechnika.class));
    }

    // zakoncz()

    @Test
    @DisplayName("zakoncz: ustawia status ZAKONCZONE, faktyczne zakończenie i notatkę")
    void zakoncz_ustawiaStatusIDate() {
        // Przypisanie musi mieć przypisane zgłoszenie (zakoncz aktualizuje też jego status)
        Zgloszenie z = noweZgloszenie();
        z.setStatus(Zgloszenie.Status.W_TOKU);

        PrzypisanieTechnika p = aktywne(5L);
        p.setZgloszenie(z);

        when(przypisanieRepo.findById(5L)).thenReturn(Optional.of(p));
        when(przypisanieRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(zgloszenieRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PrzypisanieTechnika wynik = serwis.zakoncz(5L, "Wykonano naprawę");

        assertThat(wynik.getStatusPrzypisania()).isEqualTo(StatusPrzypisania.ZAKONCZONE);
        assertThat(wynik.getFaktyczneZakonczenie()).isNotNull();
        assertThat(wynik.getNotatka()).isEqualTo("Wykonano naprawę");
        // Zgłoszenie powinno być automatycznie zmienione na ROZWIĄZANE
        assertThat(z.getStatus()).isEqualTo(Zgloszenie.Status.ROZWIAZANE);
        assertThat(z.getZamknieto()).isNotNull();
    }

    @Test
    @DisplayName("zakoncz: brak przypisania → IllegalArgumentException")
    void zakoncz_brakPrzypisania_rzucaWyjatek() {
        when(przypisanieRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serwis.zakoncz(999L, "notatka"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // anuluj()

    @Test
    @DisplayName("anuluj: ustawia status ANULOWANE i powód w notatce")
    void anuluj_ustawiaStatusIPowod() {
        PrzypisanieTechnika p = aktywne(7L);
        when(przypisanieRepo.findById(7L)).thenReturn(Optional.of(p));
        when(przypisanieRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PrzypisanieTechnika wynik = serwis.anuluj(7L, "Technik niedostępny");

        assertThat(wynik.getStatusPrzypisania()).isEqualTo(StatusPrzypisania.ANULOWANE);
        assertThat(wynik.getNotatka()).isEqualTo("Technik niedostępny");
    }

    @Test
    @DisplayName("anuluj: brak przypisania → IllegalArgumentException")
    void anuluj_brakPrzypisania_rzucaWyjatek() {
        when(przypisanieRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serwis.anuluj(999L, "powód"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Pomocniki

    private void stubPoprawnePrzypisanie() {
        when(zgloszenieRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(zgloszenie));
        when(przypisanieRepo.existsByZgloszenieIdAndStatusPrzypisania(10L, StatusPrzypisania.AKTYWNE))
                .thenReturn(false);
        when(uzytkownikRepo.findById(2L)).thenReturn(Optional.of(technik));
        when(uzytkownikRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(zgloszenieRepo.save(any(Zgloszenie.class))).thenAnswer(inv -> inv.getArgument(0));
        when(przypisanieRepo.save(any(PrzypisanieTechnika.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PrzypisanieTechnika aktywne(Long id) {
        PrzypisanieTechnika p = new PrzypisanieTechnika();
        p.setId(id);
        p.setStatusPrzypisania(StatusPrzypisania.AKTYWNE);
        return p;
    }

    private Zgloszenie noweZgloszenie() {
        Zgloszenie z = new Zgloszenie();
        z.setId(10L);
        z.setStatus(Zgloszenie.Status.NOWE);
        z.setKategoria(kategoria);
        z.setTytul("Testowe zgłoszenie");
        z.setOpis("Opis testowy");
        return z;
    }
}
