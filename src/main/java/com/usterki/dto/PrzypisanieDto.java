package com.usterki.dto;

import com.usterki.model.PrzypisanieTechnika;
import java.time.LocalDateTime;

public class PrzypisanieDto {

    public static class Zapis {
        private Long idTechnika;
        private Long idPrzypisujacego;
        private LocalDateTime planowanyStart;

        public Long getIdTechnika() { return idTechnika; }
        public void setIdTechnika(Long idTechnika) { this.idTechnika = idTechnika; }

        public Long getIdPrzypisujacego() { return idPrzypisujacego; }
        public void setIdPrzypisujacego(Long idPrzypisujacego) { this.idPrzypisujacego = idPrzypisujacego; }

        public LocalDateTime getPlanowanyStart() { return planowanyStart; }
        public void setPlanowanyStart(LocalDateTime planowanyStart) { this.planowanyStart = planowanyStart; }
    }

    public static class Odpowiedz {
        private Long id;
        private String numerZgloszenia;
        private String tytul;
        private String technik;
        private PrzypisanieTechnika.StatusPrzypisania status;
        private LocalDateTime planowanyStart;
        private LocalDateTime planowaneZakonczenie;
        private LocalDateTime faktyczneZakonczenie;

        public static Odpowiedz z(PrzypisanieTechnika p) {
            Odpowiedz o = new Odpowiedz();
            o.id                   = p.getId();
            o.numerZgloszenia      = p.getZgloszenie().getNumerZgloszenia();
            o.tytul                = p.getZgloszenie().getTytul();
            o.technik              = p.getTechnik().pelneNazwisko();
            o.status               = p.getStatusPrzypisania();
            o.planowanyStart       = p.getPlanowanyStart();
            o.planowaneZakonczenie = p.getPlanowaneZakonczenie();
            o.faktyczneZakonczenie = p.getFaktyczneZakonczenie();
            return o;
        }

        public Long getId() { return id; }
        public String getNumerZgloszenia() { return numerZgloszenia; }
        public String getTytul() { return tytul; }
        public String getTechnik() { return technik; }
        public PrzypisanieTechnika.StatusPrzypisania getStatus() { return status; }
        public LocalDateTime getPlanowanyStart() { return planowanyStart; }
        public LocalDateTime getPlanowaneZakonczenie() { return planowaneZakonczenie; }
        public LocalDateTime getFaktyczneZakonczenie() { return faktyczneZakonczenie; }
    }
}
