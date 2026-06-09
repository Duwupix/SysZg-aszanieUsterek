package com.usterki.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testy wzorca Budowniczy (Builder). */
class ZgloszenieBuilderTest {

    private Uzytkownik zglaszajacy() {
        Uzytkownik u = new Uzytkownik();
        u.setLogin("jan.kowalski");
        u.setImie("Jan");
        u.setNazwisko("Kowalski");
        return u;
    }

    private KategoriaUsterki kategoria() {
        KategoriaUsterki k = new KategoriaUsterki();
        k.setNazwa("Drogi i chodniki");
        return k;
    }

    @Test
    @DisplayName("Budowniczy ustawia wszystkie przekazane pola w gotowym zgłoszeniu")
    void build_ustawiaWszystkiePola() {
        LocalDateTime data = LocalDateTime.now().minusDays(2);

        Zgloszenie z = new ZgloszenieBuilder()
                .numer("ZGL-20260101-000001")
                .zglaszajacy(zglaszajacy())
                .kategoria(kategoria())
                .tytul("Dziura w jezdni")
                .opis("Głęboka dziura przy przejściu")
                .pilnosc(Zgloszenie.Pilnosc.WYSOKA)
                .adres("ul. Testowa 1")
                .dataZauwazeniaUsterki(data)
                .status(Zgloszenie.Status.NOWE)
                .build();

        assertThat(z.getNumerZgloszenia()).isEqualTo("ZGL-20260101-000001");
        assertThat(z.getTytul()).isEqualTo("Dziura w jezdni");
        assertThat(z.getOpis()).isEqualTo("Głęboka dziura przy przejściu");
        assertThat(z.getPilnosc()).isEqualTo(Zgloszenie.Pilnosc.WYSOKA);
        assertThat(z.getAdres()).isEqualTo("ul. Testowa 1");
        assertThat(z.getDataZauwazeniaUsterki()).isEqualTo(data);
        assertThat(z.getStatus()).isEqualTo(Zgloszenie.Status.NOWE);
        assertThat(z.getKategoria().getNazwa()).isEqualTo("Drogi i chodniki");
        assertThat(z.getZglaszajacy().getLogin()).isEqualTo("jan.kowalski");
    }

    @Test
    @DisplayName("Budowniczy bez tytułu rzuca IllegalStateException")
    void build_brakTytulu_rzucaWyjatek() {
        assertThatThrownBy(() -> new ZgloszenieBuilder()
                .zglaszajacy(zglaszajacy())
                .kategoria(kategoria())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tytuł");
    }

    @Test
    @DisplayName("Budowniczy bez kategorii rzuca IllegalStateException")
    void build_brakKategorii_rzucaWyjatek() {
        assertThatThrownBy(() -> new ZgloszenieBuilder()
                .zglaszajacy(zglaszajacy())
                .tytul("Test")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("kategorię");
    }

    @Test
    @DisplayName("Budowniczy bez zgłaszającego rzuca IllegalStateException")
    void build_brakZglaszajacego_rzucaWyjatek() {
        assertThatThrownBy(() -> new ZgloszenieBuilder()
                .tytul("Test")
                .kategoria(kategoria())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("zgłaszającego");
    }
}
