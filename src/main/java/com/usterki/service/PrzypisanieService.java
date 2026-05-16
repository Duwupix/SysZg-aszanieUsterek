package com.usterki.service;

import com.usterki.model.PrzypisanieTechnika;
import com.usterki.model.PrzypisanieTechnika.StatusPrzypisania;
import com.usterki.model.Uzytkownik;
import com.usterki.model.Zgloszenie;
import com.usterki.repository.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PrzypisanieService {

    private static final Logger log = Logger.getLogger(PrzypisanieService.class.getName());
    private static final int MAX_RETRY = 3;

    private final ZgloszenieRepository zgloszenieRepo;
    private final UzytkownikRepository uzytkownikRepo;
    private final PrzypisanieTechnikaRepository przypisanieRepo;
    private final PrzypisanieService self;

    public PrzypisanieService(ZgloszenieRepository zgloszenieRepo,
                               UzytkownikRepository uzytkownikRepo,
                               PrzypisanieTechnikaRepository przypisanieRepo,
                               @Lazy PrzypisanieService self) {
        this.zgloszenieRepo  = zgloszenieRepo;
        this.uzytkownikRepo  = uzytkownikRepo;
        this.przypisanieRepo = przypisanieRepo;
        this.self            = self;
    }


    public PrzypisanieTechnika przypisz(Long idZgloszenia, Long idTechnika,
                                        Long idPrzypisujacego, LocalDateTime planowanyStart) {
        int proba = 0;
        while (true) {
            try {
                return self.przypiszWTransakcji(idZgloszenia, idTechnika, idPrzypisujacego, planowanyStart);
            } catch (ObjectOptimisticLockingFailureException e) {
                proba++;
                if (proba >= MAX_RETRY) {
                    throw new IllegalStateException("Konflikt współbieżności – spróbuj ponownie.");
                }
                log.warning("Konflikt wersji przy przypisaniu, próba " + proba + "/" + MAX_RETRY);
                try { Thread.sleep(200L * proba); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PrzypisanieTechnika przypiszWTransakcji(Long idZgloszenia, Long idTechnika,
                                                    Long idPrzypisujacego, LocalDateTime planowanyStart) {

        Zgloszenie zgloszenie = zgloszenieRepo.findByIdForUpdate(idZgloszenia)
                .orElseThrow(() -> new IllegalArgumentException("Zgłoszenie nie istnieje: " + idZgloszenia));

        if (przypisanieRepo.existsByZgloszenieIdAndStatusPrzypisania(idZgloszenia, StatusPrzypisania.AKTYWNE)) {
            throw new IllegalStateException("Zgłoszenie ma już aktywnie przypisanego technika.");
        }

        Uzytkownik technik = uzytkownikRepo.findById(idTechnika)
                .orElseThrow(() -> new IllegalArgumentException("Technik nie istnieje: " + idTechnika));
        Uzytkownik przypisujacy = uzytkownikRepo.findById(idPrzypisujacego)
                .orElseThrow(() -> new IllegalArgumentException("Przypisujący nie istnieje: " + idPrzypisujacego));

        LocalDateTime start       = planowanyStart != null ? planowanyStart : LocalDateTime.now();
        LocalDateTime zakonczenie = start.plusHours(zgloszenie.getKategoria().getSzacCzasGodz());

        PrzypisanieTechnika p = new PrzypisanieTechnika();
        p.setZgloszenie(zgloszenie);
        p.setTechnik(technik);
        p.setPrzypisujacy(przypisujacy);
        p.setPlanowanyStart(start);
        p.setPlanowaneZakonczenie(zakonczenie);
        p.setStatusPrzypisania(StatusPrzypisania.AKTYWNE);

        zgloszenie.setStatus(Zgloszenie.Status.W_TOKU);
        zgloszenieRepo.save(zgloszenie);

        return przypisanieRepo.save(p);
    }

    @Transactional
    public PrzypisanieTechnika zakoncz(Long idPrzypisania, String notatka) {
        PrzypisanieTechnika p = przypisanieRepo.findById(idPrzypisania)
                .orElseThrow(() -> new IllegalArgumentException("Przypisanie nie istnieje"));
        p.setStatusPrzypisania(StatusPrzypisania.ZAKONCZONE);
        p.setFaktyczneZakonczenie(LocalDateTime.now());
        p.setNotatka(notatka);
        przypisanieRepo.save(p);

        // Automatyczna zmiana statusu zgłoszenia na ROZWIĄZANE po zakończeniu prac technika.
        // Administrator może je następnie zamknąć (ZAMKNIETE) lub ponownie otworzyć.
        Zgloszenie zgl = p.getZgloszenie();
        zgl.setStatus(Zgloszenie.Status.ROZWIAZANE);
        zgl.setZamknieto(LocalDateTime.now());
        zgloszenieRepo.save(zgl);

        return p;
    }

    @Transactional
    public PrzypisanieTechnika anuluj(Long idPrzypisania, String powod) {
        PrzypisanieTechnika p = przypisanieRepo.findById(idPrzypisania)
                .orElseThrow(() -> new IllegalArgumentException("Przypisanie nie istnieje"));
        p.setStatusPrzypisania(StatusPrzypisania.ANULOWANE);
        p.setNotatka(powod);
        return przypisanieRepo.save(p);
    }

    @Transactional(readOnly = true)
    public List<PrzypisanieTechnika> kolejkaTechnika(Long idTechnika) {
        return przypisanieRepo.findByTechnikIdAndStatusPrzypisania(idTechnika, StatusPrzypisania.AKTYWNE);
    }
}
