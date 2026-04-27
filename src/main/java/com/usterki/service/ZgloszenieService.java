package com.usterki.service;

import com.usterki.model.*;
import com.usterki.model.Zgloszenie.Status;
import com.usterki.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ZgloszenieService {

    private final ZgloszenieRepository zgloszenieRepo;
    private final UzytkownikRepository uzytkownikRepo;
    private final KategoriaUsterkiRepository kategoriaRepo;
    private final HistoriaStatusowRepository historiaRepo;
    private final PriorytetService priorytetService;

    public ZgloszenieService(ZgloszenieRepository zgloszenieRepo,
                             UzytkownikRepository uzytkownikRepo,
                             KategoriaUsterkiRepository kategoriaRepo,
                             HistoriaStatusowRepository historiaRepo,
                             PriorytetService priorytetService) {
        this.zgloszenieRepo  = zgloszenieRepo;
        this.uzytkownikRepo  = uzytkownikRepo;
        this.kategoriaRepo   = kategoriaRepo;
        this.historiaRepo    = historiaRepo;
        this.priorytetService = priorytetService;
    }

    @Transactional
    public Zgloszenie utworz(Long idZglaszajacego, Long idKategorii,
                             String tytul, String opis,
                             Zgloszenie.Pilnosc pilnosc, String adres,
                             LocalDateTime terminRealizacji) {

        Uzytkownik zglaszajacy = uzytkownikRepo.findById(idZglaszajacego)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));
        KategoriaUsterki kategoria = kategoriaRepo.findById(idKategorii)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kategorii"));

        Zgloszenie z = new Zgloszenie();
        z.setNumerZgloszenia(generujNumer());
        z.setZglaszajacy(zglaszajacy);
        z.setKategoria(kategoria);
        z.setTytul(tytul);
        z.setOpis(opis);
        z.setPilnosc(pilnosc);
        z.setAdres(adres);
        z.setTerminRealizacji(terminRealizacji);
        z.setStatus(Status.NOWE);

        priorytetService.zaktualizujPriorytet(z);
        Zgloszenie saved = zgloszenieRepo.save(z);
        zapiszHistorie(saved, null, Status.NOWE, zglaszajacy, "Zgłoszenie utworzone");
        return saved;
    }

    @Transactional
    public Zgloszenie zmienStatus(Long idZgloszenia, Status nowyStatus,
                                  Long idUzytkownika, String komentarz) {
        Zgloszenie z = zgloszenieRepo.findById(idZgloszenia)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zgłoszenia"));
        Uzytkownik user = uzytkownikRepo.findById(idUzytkownika)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));

        Status stary = z.getStatus();
        z.setStatus(nowyStatus);
        if (nowyStatus == Status.ZAMKNIETE || nowyStatus == Status.ROZWIAZANE) {
            z.setZamknieto(LocalDateTime.now());
        }
        priorytetService.zaktualizujPriorytet(z);
        Zgloszenie saved = zgloszenieRepo.save(z);
        zapiszHistorie(saved, stary, nowyStatus, user, komentarz);
        return saved;
    }

    @Transactional
    public Zgloszenie ustawPriorytetReczny(Long idZgloszenia, Integer priorytet) {
        Zgloszenie z = zgloszenieRepo.findById(idZgloszenia)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zgłoszenia"));
        z.setPriorytetReczny(priorytet);
        priorytetService.zaktualizujPriorytet(z);
        return zgloszenieRepo.save(z);
    }

    @Transactional(readOnly = true)
    public List<Zgloszenie> pobierzKolejke() {
        return zgloszenieRepo.findByStatusNotInOrderByPriorytetObliczonyAscTerminRealizacjiAsc(
                List.of(Status.ZAMKNIETE, Status.ODRZUCONE));
    }

    @Transactional(readOnly = true)
    public List<Zgloszenie> pobierzZgloszeniaUzytkownika(Long idUzytkownika) {
        return zgloszenieRepo.findByZglaszajacyId(idUzytkownika);
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void przeliczPriorytety() {
        zgloszenieRepo.findOtwarteDoPrezeliczenia().forEach(z -> {
            priorytetService.zaktualizujPriorytet(z);
            zgloszenieRepo.save(z);
        });
    }

    private void zapiszHistorie(Zgloszenie z, Status stary, Status nowy,
                                Uzytkownik user, String komentarz) {
        HistoriaStatusow h = new HistoriaStatusow();
        h.setZgloszenie(z);
        h.setUzytkownik(user);
        h.setStaryStatus(stary);
        h.setNowyStatus(nowy);
        h.setKomentarz(komentarz);
        historiaRepo.save(h);
    }

    private String generujNumer() {
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long liczba = zgloszenieRepo.count() + 1;
        return String.format("ZGL-%s-%06d", data, liczba);
    }
}
