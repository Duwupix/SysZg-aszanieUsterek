package com.usterki.observer;

import com.usterki.model.HistoriaStatusow;
import com.usterki.repository.HistoriaStatusowRepository;
import org.springframework.stereotype.Component;

/**
 * Konkretny obserwator zapisujący każdą zmianę statusu do tabeli
 * {@code historia_statusow} (ślad audytowy).
 */
@Component
public class HistoriaStatusowObserver implements ZmianaStatusuObserver {

    private final HistoriaStatusowRepository historiaRepo;

    public HistoriaStatusowObserver(HistoriaStatusowRepository historiaRepo) {
        this.historiaRepo = historiaRepo;
    }

    @Override
    public void onZmianaStatusu(ZdarzenieZmianyStatusu zdarzenie) {
        HistoriaStatusow h = new HistoriaStatusow();
        h.setZgloszenie(zdarzenie.zgloszenie());
        h.setUzytkownik(zdarzenie.uzytkownik());
        h.setStaryStatus(zdarzenie.staryStatus());
        h.setNowyStatus(zdarzenie.nowyStatus());
        h.setKomentarz(zdarzenie.komentarz());
        historiaRepo.save(h);
    }
}
