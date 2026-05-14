package com.usterki.service;

import com.usterki.model.KategoriaUsterki;
import com.usterki.model.Zgloszenie;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class PriorytetService {

    public int oblicz(Zgloszenie z) {
        if (z.getPriorytetReczny() != null) {
            return clamp(z.getPriorytetReczny());
        }
        KategoriaUsterki kat = z.getKategoria();
        double priorytet = kat.getDomyslnyPriorytet()
                         * kat.getWspolczynnikWagi().doubleValue()
                         * mnoznikPilnosci(z.getPilnosc())
                         * mnoznikZwloki(z.getDataZauwazeniaUsterki());
        return clamp((int) Math.round(priorytet));
    }

    public void zaktualizujPriorytet(Zgloszenie z) {
        z.setPriorytetObliczony(oblicz(z));
    }

    private double mnoznikPilnosci(Zgloszenie.Pilnosc pilnosc) {
        return switch (pilnosc) {
            case NATYCHMIASTOWA -> 0.50;
            case WYSOKA         -> 0.70;
            case SREDNIA        -> 1.00;
            case NISKA          -> 1.30;
        };
    }

    /**
     * Im starsza data zauważenia usterki, tym niższy mnożnik (= wyższy priorytet w kolejce).
     * Podłoga 0.50 — usterka nie może mieć priorytetu mniejszego niż połowa bazowego.
     */
    private double mnoznikZwloki(LocalDateTime dataZauważenia) {
        if (dataZauważenia == null || !dataZauważenia.isBefore(LocalDateTime.now())) return 1.0;
        long godziny = ChronoUnit.HOURS.between(dataZauważenia, LocalDateTime.now());
        return Math.max(0.5, 1.0 - (godziny / 24.0) * 0.05);
    }

    private int clamp(int v) { return Math.max(1, Math.min(100, v)); }
}
