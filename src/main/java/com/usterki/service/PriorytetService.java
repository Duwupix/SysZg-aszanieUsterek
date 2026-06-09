package com.usterki.service;

import com.usterki.model.Zgloszenie;
import com.usterki.service.priorytet.StrategiaPriorytetu;
import com.usterki.service.priorytet.WazonaStrategiaPriorytetu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Kontekst wzorca STRATEGIA. Oblicza priorytet zgłoszenia, delegując właściwy
 * algorytm do wstrzykniętej {@link StrategiaPriorytetu}. Priorytet ręczny
 * (jeśli ustawiony) zawsze ma pierwszeństwo nad obliczeniami strategii.
 */
@Component
public class PriorytetService {

    private final StrategiaPriorytetu strategia;

    /** Konstruktor produkcyjny — Spring wstrzykuje strategię oznaczoną {@code @Primary}. */
    @Autowired
    public PriorytetService(StrategiaPriorytetu strategia) {
        this.strategia = strategia;
    }

    /** Konstruktor domyślny (m.in. dla testów) — używa strategii ważonej. */
    public PriorytetService() {
        this(new WazonaStrategiaPriorytetu());
    }

    public int oblicz(Zgloszenie z) {
        if (z.getPriorytetReczny() != null) {
            return clamp(z.getPriorytetReczny());
        }
        return strategia.oblicz(z);
    }

    public void zaktualizujPriorytet(Zgloszenie z) {
        z.setPriorytetObliczony(oblicz(z));
    }

    private int clamp(int v) { return Math.max(1, Math.min(100, v)); }
}
