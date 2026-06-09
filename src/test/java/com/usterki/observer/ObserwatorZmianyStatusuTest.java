package com.usterki.observer;

import com.usterki.model.HistoriaStatusow;
import com.usterki.model.Uzytkownik;
import com.usterki.model.Zgloszenie;
import com.usterki.repository.HistoriaStatusowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Testy wzorca Obserwator (Observer). */
@ExtendWith(MockitoExtension.class)
class ObserwatorZmianyStatusuTest {

    @Mock private HistoriaStatusowRepository historiaRepo;

    private ZdarzenieZmianyStatusu przykladoweZdarzenie() {
        Zgloszenie z = new Zgloszenie();
        z.setNumerZgloszenia("ZGL-20260101-000001");
        Uzytkownik u = new Uzytkownik();
        u.setLogin("admin");
        return new ZdarzenieZmianyStatusu(z, Zgloszenie.Status.NOWE,
                Zgloszenie.Status.W_TOKU, u, "przyjęto");
    }

    @Test
    @DisplayName("Publikator powiadamia wszystkich zarejestrowanych obserwatorów")
    void publikator_powiadamiaWszystkichObserwatorow() {
        ZmianaStatusuObserver o1 = mock(ZmianaStatusuObserver.class);
        ZmianaStatusuObserver o2 = mock(ZmianaStatusuObserver.class);
        PublikatorZmianyStatusu publikator = new PublikatorZmianyStatusu(List.of(o1, o2));

        ZdarzenieZmianyStatusu zdarzenie = przykladoweZdarzenie();
        publikator.publikuj(zdarzenie);

        verify(o1, times(1)).onZmianaStatusu(zdarzenie);
        verify(o2, times(1)).onZmianaStatusu(zdarzenie);
    }

    @Test
    @DisplayName("Publikator bez obserwatorów nie rzuca wyjątku")
    void publikator_bezObserwatorow_niczegoNieRobi() {
        PublikatorZmianyStatusu publikator = new PublikatorZmianyStatusu(List.of());

        publikator.publikuj(przykladoweZdarzenie()); // nie powinno rzucić wyjątku
    }

    @Test
    @DisplayName("HistoriaStatusowObserver zapisuje wpis o poprawnych danych")
    void historiaObserver_zapisujePoprawnyWpis() {
        HistoriaStatusowObserver obserwator = new HistoriaStatusowObserver(historiaRepo);

        obserwator.onZmianaStatusu(przykladoweZdarzenie());

        ArgumentCaptor<HistoriaStatusow> captor = ArgumentCaptor.forClass(HistoriaStatusow.class);
        verify(historiaRepo, times(1)).save(captor.capture());
        HistoriaStatusow zapisany = captor.getValue();
        assertThat(zapisany.getStaryStatus()).isEqualTo(Zgloszenie.Status.NOWE);
        assertThat(zapisany.getNowyStatus()).isEqualTo(Zgloszenie.Status.W_TOKU);
        assertThat(zapisany.getKomentarz()).isEqualTo("przyjęto");
    }

    @Test
    @DisplayName("LogObserver nie dotyka bazy danych")
    void logObserver_nieDotykaBazy() {
        LogObserver obserwator = new LogObserver();

        obserwator.onZmianaStatusu(przykladoweZdarzenie()); // tylko log, brak interakcji z repo
        verifyNoInteractions(historiaRepo);
    }
}
