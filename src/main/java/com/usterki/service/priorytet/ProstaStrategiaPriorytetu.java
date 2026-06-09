package com.usterki.service.priorytet;

import com.usterki.model.Zgloszenie;
import org.springframework.stereotype.Component;

/**
 * Alternatywna strategia obliczania priorytetu — wariant prosty.
 *
 * Priorytet = domyślny priorytet kategorii (bez modyfikatorów pilności i zwłoki).
 * Przydatna np. do szybkich zestawień lub porównań A/B działania algorytmu.
 *
 * Istnieje obok {@link WazonaStrategiaPriorytetu}; nie jest domyślna, ale można
 * ją wstrzyknąć po nazwie ({@code @Qualifier("prostaStrategiaPriorytetu")}).
 */
@Component
public class ProstaStrategiaPriorytetu implements StrategiaPriorytetu {

    @Override
    public int oblicz(Zgloszenie z) {
        int p = z.getKategoria().getDomyslnyPriorytet();
        return Math.max(1, Math.min(100, p));
    }
}
