package com.usterki.facade;

import com.usterki.dto.ZgloszenieDto;
import com.usterki.model.KategoriaUsterki;
import com.usterki.model.Uzytkownik;
import com.usterki.model.Zgloszenie;
import com.usterki.model.ZgloszenieBuilder;
import com.usterki.service.ZgloszenieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Testy wzorca Fasada (Facade) — pulpit. */
@ExtendWith(MockitoExtension.class)
class PulpitFasadaTest {

    @Mock private ZgloszenieService zgloszenieService;

    private Zgloszenie zgloszenie(int i) {
        Uzytkownik u = new Uzytkownik();
        u.setImie("Jan");
        u.setNazwisko("Kowalski");
        KategoriaUsterki k = new KategoriaUsterki();
        k.setId(1L);
        k.setNazwa("Drogi i chodniki");
        return new ZgloszenieBuilder()
                .numer("ZGL-20260101-" + String.format("%06d", i))
                .zglaszajacy(u)
                .kategoria(k)
                .tytul("Zgłoszenie " + i)
                .opis("Opis")
                .pilnosc(Zgloszenie.Pilnosc.SREDNIA)
                .status(Zgloszenie.Status.NOWE)
                .build();
    }

    @Test
    @DisplayName("Fasada zwraca komplet danych pulpitu: statystyki i najpilniejsze")
    void pobierzDanePulpitu_zwracaObaKluczy() {
        when(zgloszenieService.pobierzStatystyki()).thenReturn(Map.of("lacznie", 5L));
        when(zgloszenieService.pobierzKolejke()).thenReturn(List.of(zgloszenie(1)));

        Map<String, Object> dane = new PulpitFasada(zgloszenieService).pobierzDanePulpitu();

        assertThat(dane).containsKeys("statystyki", "najpilniejsze");
    }

    @Test
    @DisplayName("Fasada ogranicza listę najpilniejszych do 10 pozycji")
    void pobierzDanePulpitu_ograniczaDoDziesieciu() {
        when(zgloszenieService.pobierzStatystyki()).thenReturn(Map.of());
        List<Zgloszenie> dwanascie = IntStream.rangeClosed(1, 12).mapToObj(this::zgloszenie).toList();
        when(zgloszenieService.pobierzKolejke()).thenReturn(dwanascie);

        Map<String, Object> dane = new PulpitFasada(zgloszenieService).pobierzDanePulpitu();

        @SuppressWarnings("unchecked")
        List<ZgloszenieDto.Odpowiedz> najpilniejsze =
                (List<ZgloszenieDto.Odpowiedz>) dane.get("najpilniejsze");
        assertThat(najpilniejsze).hasSize(PulpitFasada.LIMIT_KOLEJKI);
    }

    @Test
    @DisplayName("Fasada mapuje encje na DTO (bezpieczne do serializacji)")
    void pobierzDanePulpitu_mapujeNaDto() {
        when(zgloszenieService.pobierzStatystyki()).thenReturn(Map.of());
        when(zgloszenieService.pobierzKolejke()).thenReturn(List.of(zgloszenie(7)));

        Map<String, Object> dane = new PulpitFasada(zgloszenieService).pobierzDanePulpitu();

        @SuppressWarnings("unchecked")
        List<ZgloszenieDto.Odpowiedz> najpilniejsze =
                (List<ZgloszenieDto.Odpowiedz>) dane.get("najpilniejsze");
        assertThat(najpilniejsze.get(0).getKategoria()).isEqualTo("Drogi i chodniki");
        assertThat(najpilniejsze.get(0).getZglaszajacy()).isEqualTo("Jan Kowalski");
    }
}
