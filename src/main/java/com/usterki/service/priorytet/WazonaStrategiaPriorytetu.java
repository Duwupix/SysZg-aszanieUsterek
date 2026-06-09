package com.usterki.service.priorytet;

import com.usterki.model.KategoriaUsterki;
import com.usterki.model.Zgloszenie;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Domyślna (produkcyjna) strategia obliczania priorytetu — wariant ważony.
 *
 * Priorytet = domyślny priorytet kategorii × współczynnik wagi kategorii
 *           × mnożnik pilności × mnożnik zwłoki.
 *
 * Oznaczona {@code @Primary}, więc Spring wstrzykuje właśnie ją jako domyślną
 * implementację interfejsu {@link StrategiaPriorytetu}.
 */
@Component
@Primary
public class WazonaStrategiaPriorytetu implements StrategiaPriorytetu {

    @Override
    public int oblicz(Zgloszenie z) {
        KategoriaUsterki kat = z.getKategoria();
        double priorytet = kat.getDomyslnyPriorytet()
                         * kat.getWspolczynnikWagi().doubleValue()
                         * mnoznikPilnosci(z.getPilnosc())
                         * mnoznikZwloki(z.getDataZauwazeniaUsterki());
        return clamp((int) Math.round(priorytet));
    }

    private double mnoznikPilnosci(Zgloszenie.Pilnosc pilnosc) {
        return switch (pilnosc) {
            case NATYCHMIASTOWA -> 0.50;
            case WYSOKA         -> 0.70;
            case SREDNIA        -> 1.00;
            case NISKA          -> 1.30;
        };
    }

    private double mnoznikZwloki(LocalDateTime dataZauwazenia) {
        if (dataZauwazenia == null || !dataZauwazenia.isBefore(LocalDateTime.now())) return 1.0;
        long godziny = ChronoUnit.HOURS.between(dataZauwazenia, LocalDateTime.now());
        return Math.max(0.5, 1.0 - (godziny / 24.0) * 0.05);
    }

    private int clamp(int v) { return Math.max(1, Math.min(100, v)); }
}
