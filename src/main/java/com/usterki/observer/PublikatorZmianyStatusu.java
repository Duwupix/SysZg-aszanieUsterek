package com.usterki.observer;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Podmiot (Subject) wzorca Obserwator. Utrzymuje listę obserwatorów i powiadamia
 * ich o zmianie statusu zgłoszenia. Komplet obserwatorów dostarcza Spring —
 * wstrzykuje wszystkie beany implementujące {@link ZmianaStatusuObserver}.
 */
@Component
public class PublikatorZmianyStatusu {

    private final List<ZmianaStatusuObserver> obserwatorzy;

    public PublikatorZmianyStatusu(List<ZmianaStatusuObserver> obserwatorzy) {
        this.obserwatorzy = obserwatorzy;
    }

    /** Powiadamia wszystkich zarejestrowanych obserwatorów o zdarzeniu. */
    public void publikuj(ZdarzenieZmianyStatusu zdarzenie) {
        for (ZmianaStatusuObserver obserwator : obserwatorzy) {
            obserwator.onZmianaStatusu(zdarzenie);
        }
    }
}
