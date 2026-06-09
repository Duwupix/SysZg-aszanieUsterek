package com.usterki.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Konkretny obserwator logujący zmiany statusu do logów aplikacji. Pokazuje, że
 * obserwatorów można dodawać niezależnie — ten nie dotyka bazy danych, a mimo to
 * reaguje na to samo zdarzenie co {@link HistoriaStatusowObserver}.
 */
@Component
public class LogObserver implements ZmianaStatusuObserver {

    private static final Logger log = LoggerFactory.getLogger(LogObserver.class);

    @Override
    public void onZmianaStatusu(ZdarzenieZmianyStatusu z) {
        log.info("Zgłoszenie {}: {} -> {} (przez {})",
                z.zgloszenie().getNumerZgloszenia(),
                z.staryStatus(), z.nowyStatus(),
                z.uzytkownik() != null ? z.uzytkownik().getLogin() : "system");
    }
}
