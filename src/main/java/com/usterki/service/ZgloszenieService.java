package com.usterki.service;

import com.usterki.model.*;
import com.usterki.model.Zgloszenie.Status;
import com.usterki.observer.PublikatorZmianyStatusu;
import com.usterki.observer.ZdarzenieZmianyStatusu;
import com.usterki.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ZgloszenieService {

    private final ZgloszenieRepository zgloszenieRepo;
    private final UzytkownikRepository uzytkownikRepo;
    private final KategoriaUsterkiRepository kategoriaRepo;
    private final PriorytetService priorytetService;
    private final PublikatorZmianyStatusu publikator;

    public ZgloszenieService(ZgloszenieRepository zgloszenieRepo,
                             UzytkownikRepository uzytkownikRepo,
                             KategoriaUsterkiRepository kategoriaRepo,
                             PriorytetService priorytetService,
                             PublikatorZmianyStatusu publikator) {
        this.zgloszenieRepo  = zgloszenieRepo;
        this.uzytkownikRepo  = uzytkownikRepo;
        this.kategoriaRepo   = kategoriaRepo;
        this.priorytetService = priorytetService;
        this.publikator      = publikator;
    }

    @Transactional
    public Zgloszenie utworz(Long idZglaszajacego, Long idKategorii,
                             String tytul, String opis,
                             Zgloszenie.Pilnosc pilnosc, String adres,
                             LocalDateTime dataZauwazeniaUsterki) {

        Uzytkownik zglaszajacy = uzytkownikRepo.findById(idZglaszajacego)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));
        KategoriaUsterki kategoria = kategoriaRepo.findById(idKategorii)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kategorii"));

        // Wzorzec Budowniczy — płynne, walidowane składanie encji zgłoszenia.
        Zgloszenie z = new ZgloszenieBuilder()
                .numer(generujNumer())
                .zglaszajacy(zglaszajacy)
                .kategoria(kategoria)
                .tytul(tytul)
                .opis(opis)
                .pilnosc(pilnosc)
                .adres(adres)
                .dataZauwazeniaUsterki(dataZauwazeniaUsterki)
                .status(Status.NOWE)
                .build();

        priorytetService.zaktualizujPriorytet(z);
        Zgloszenie saved = zgloszenieRepo.save(z);
        // Wzorzec Obserwator — powiadom obserwatorów (m.in. zapis do historii).
        publikator.publikuj(new ZdarzenieZmianyStatusu(saved, null, Status.NOWE, zglaszajacy, "Zgłoszenie utworzone"));
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
        // Wzorzec Obserwator — powiadom obserwatorów o zmianie statusu.
        publikator.publikuj(new ZdarzenieZmianyStatusu(saved, stary, nowyStatus, user, komentarz));
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
        return zgloszenieRepo.findByStatusNotInOrderByPriorytetObliczonyAscDataZauwazeniaUsterkiAsc(
                List.of(Status.ZAMKNIETE, Status.ODRZUCONE));
    }

    @Transactional(readOnly = true)
    public Zgloszenie pobierzZgloszeniePoId(Long id) {
        return zgloszenieRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zgłoszenia: " + id));
    }

    @Transactional(readOnly = true)
    public List<Zgloszenie> pobierzZgloszeniaUzytkownika(Long idUzytkownika) {
        return zgloszenieRepo.findByZglaszajacyId(idUzytkownika);
    }

    /** Filtrowanie po statusie, kategorii, tekście (tytuł/opis/adres) i zakresie dat. */
    @Transactional(readOnly = true)
    public List<Zgloszenie> filtruj(String status, Long idKategorii,
                                    String szukaj, LocalDate od, LocalDate dataDo) {
        return zgloszenieRepo.findAll((root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                try {
                    preds.add(cb.equal(root.get("status"), Status.valueOf(status)));
                } catch (IllegalArgumentException ignored) { /* nieznany status → ignoruj */ }
            }
            if (idKategorii != null) {
                preds.add(cb.equal(root.get("kategoria").get("id"), idKategorii));
            }
            if (szukaj != null && !szukaj.isBlank()) {
                String like = "%" + szukaj.toLowerCase() + "%";
                preds.add(cb.or(
                    cb.like(cb.lower(root.get("tytul")), like),
                    cb.like(cb.lower(root.get("opis")), like),
                    cb.like(cb.lower(root.get("adres")), like)
                ));
            }
            if (od != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("utworzono"), od.atStartOfDay()));
            }
            if (dataDo != null) {
                preds.add(cb.lessThan(root.get("utworzono"), dataDo.plusDays(1).atStartOfDay()));
            }

            query.orderBy(cb.asc(root.get("priorytetObliczony")));
            return cb.and(preds.toArray(new Predicate[0]));
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> pobierzStatystyki() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Liczba wg statusu
        Map<String, Long> porStatus = new LinkedHashMap<>();
        zgloszenieRepo.groupByStatus()
                .forEach(row -> porStatus.put(row[0].toString(), (Long) row[1]));
        stats.put("porStatus", porStatus);

        // Liczba wg kategorii (wszystkie)
        List<Map<String, Object>> porKat = new ArrayList<>();
        zgloszenieRepo.groupByKategoria().forEach(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("nazwa", row[0]);
            m.put("liczba", row[1]);
            porKat.add(m);
        });
        stats.put("porKategoria", porKat);

        // Średni czas realizacji
        stats.put("srCzasRealizacjiGodz", zgloszenieRepo.avgCzasRealizacjiGodz());

        // Liczniki czasowe
        LocalDateTime now = LocalDateTime.now();
        stats.put("dzisiaj",    zgloszenieRepo.countOd(now.toLocalDate().atStartOfDay()));
        stats.put("tenTydzien", zgloszenieRepo.countOd(now.minusDays(7)));
        stats.put("tenMiesiac", zgloszenieRepo.countOd(now.minusDays(30)));
        stats.put("lacznie",    zgloszenieRepo.count());

        return stats;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void przeliczPriorytety() {
        zgloszenieRepo.findOtwarteDoPrezeliczenia().forEach(z -> {
            priorytetService.zaktualizujPriorytet(z);
            zgloszenieRepo.save(z);
        });
    }

    private String generujNumer() {
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long liczba = zgloszenieRepo.count() + 1;
        return String.format("ZGL-%s-%06d", data, liczba);
    }
}
