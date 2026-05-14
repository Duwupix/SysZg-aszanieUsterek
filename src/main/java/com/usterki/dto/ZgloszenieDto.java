package com.usterki.dto;

import com.usterki.model.Zgloszenie;
import java.time.LocalDateTime;

public class ZgloszenieDto {

    public static class Zapis {
        private Long idKategorii;
        private String tytul;
        private String opis;
        private Zgloszenie.Pilnosc pilnosc;
        private String adres;
        private LocalDateTime dataZauwazeniaUsterki;

        public Long getIdKategorii() { return idKategorii; }
        public void setIdKategorii(Long idKategorii) { this.idKategorii = idKategorii; }

        public String getTytul() { return tytul; }
        public void setTytul(String tytul) { this.tytul = tytul; }

        public String getOpis() { return opis; }
        public void setOpis(String opis) { this.opis = opis; }

        public Zgloszenie.Pilnosc getPilnosc() { return pilnosc; }
        public void setPilnosc(Zgloszenie.Pilnosc pilnosc) { this.pilnosc = pilnosc; }

        public String getAdres() { return adres; }
        public void setAdres(String adres) { this.adres = adres; }

        public LocalDateTime getDataZauwazeniaUsterki() { return dataZauwazeniaUsterki; }
        public void setDataZauwazeniaUsterki(LocalDateTime dataZauwazeniaUsterki) { this.dataZauwazeniaUsterki = dataZauwazeniaUsterki; }
    }

    public static class Odpowiedz {
        private Long id;
        private String numerZgloszenia;
        private String tytul;
        private String opis;
        private Zgloszenie.Status status;
        private int efektywnyPriorytet;
        private Zgloszenie.Pilnosc pilnosc;
        private String adres;
        private LocalDateTime dataZauwazeniaUsterki;
        private LocalDateTime utworzono;
        private LocalDateTime zamknieto;
        private Long idKategorii;
        private String kategoria;
        private String zglaszajacy;

        public static Odpowiedz z(Zgloszenie z) {
            Odpowiedz o = new Odpowiedz();
            o.id                      = z.getId();
            o.numerZgloszenia         = z.getNumerZgloszenia();
            o.tytul                   = z.getTytul();
            o.opis                    = z.getOpis();
            o.status                  = z.getStatus();
            o.efektywnyPriorytet      = z.efektywnyPriorytet();
            o.pilnosc                 = z.getPilnosc();
            o.adres                   = z.getAdres();
            o.dataZauwazeniaUsterki   = z.getDataZauwazeniaUsterki();
            o.utworzono               = z.getUtworzono();
            o.zamknieto               = z.getZamknieto();
            o.idKategorii             = z.getKategoria().getId();
            o.kategoria               = z.getKategoria().getNazwa();
            o.zglaszajacy             = z.getZglaszajacy().pelneNazwisko();
            return o;
        }

        public Long getId() { return id; }
        public String getNumerZgloszenia() { return numerZgloszenia; }
        public String getTytul() { return tytul; }
        public String getOpis() { return opis; }
        public Zgloszenie.Status getStatus() { return status; }
        public int getEfektywnyPriorytet() { return efektywnyPriorytet; }
        public Zgloszenie.Pilnosc getPilnosc() { return pilnosc; }
        public String getAdres() { return adres; }
        public LocalDateTime getDataZauwazeniaUsterki() { return dataZauwazeniaUsterki; }
        public LocalDateTime getUtworzono() { return utworzono; }
        public LocalDateTime getZamknieto() { return zamknieto; }
        public Long getIdKategorii() { return idKategorii; }
        public String getKategoria() { return kategoria; }
        public String getZglaszajacy() { return zglaszajacy; }
    }

    public static class ZmianaStatusu {
        private Zgloszenie.Status nowyStatus;
        private Long idUzytkownika;
        private String komentarz;

        public Zgloszenie.Status getNowyStatus() { return nowyStatus; }
        public void setNowyStatus(Zgloszenie.Status nowyStatus) { this.nowyStatus = nowyStatus; }

        public Long getIdUzytkownika() { return idUzytkownika; }
        public void setIdUzytkownika(Long idUzytkownika) { this.idUzytkownika = idUzytkownika; }

        public String getKomentarz() { return komentarz; }
        public void setKomentarz(String komentarz) { this.komentarz = komentarz; }
    }
}
