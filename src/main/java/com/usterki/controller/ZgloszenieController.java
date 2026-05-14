package com.usterki.controller;

import com.usterki.dto.ZgloszenieDto;
import com.usterki.model.Uzytkownik;
import com.usterki.model.Zgloszenie;
import com.usterki.service.UzytkownikService;
import com.usterki.service.ZgloszenieService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/zgloszenia")
public class ZgloszenieController {

    private final ZgloszenieService zgloszenieService;
    private final UzytkownikService uzytkownikService;

    public ZgloszenieController(ZgloszenieService zgloszenieService,
                                 UzytkownikService uzytkownikService) {
        this.zgloszenieService = zgloszenieService;
        this.uzytkownikService = uzytkownikService;
    }

    /**
     * Lista zgłoszeń z opcjonalnym filtrowaniem po stronie serwera.
     * Bez parametrów: kolejka aktywnych zgłoszeń (bez ZAMKNIETE/ODRZUCONE).
     * Z parametrami: pełne wyszukiwanie wg statusu, kategorii, tekstu, dat.
     */
    @GetMapping
    public ResponseEntity<List<ZgloszenieDto.Odpowiedz>> pobierz(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long idKategorii,
            @RequestParam(required = false) String szukaj,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate od,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDo) {

        List<Zgloszenie> wynik;
        if (status != null || idKategorii != null || szukaj != null || od != null || dataDo != null) {
            wynik = zgloszenieService.filtruj(status, idKategorii, szukaj, od, dataDo);
        } else {
            wynik = zgloszenieService.pobierzKolejke();
        }
        return ResponseEntity.ok(wynik.stream().map(ZgloszenieDto.Odpowiedz::z).toList());
    }

    /** Szczegóły pojedynczego zgłoszenia. */
    @GetMapping("/{id}")
    public ResponseEntity<ZgloszenieDto.Odpowiedz> pobierzJedno(@PathVariable Long id) {
        return ResponseEntity.ok(ZgloszenieDto.Odpowiedz.z(
                zgloszenieService.pobierzZgloszeniePoId(id)));
    }

    /** Zgłoszenia zalogowanego użytkownika. */
    @GetMapping("/moje")
    public ResponseEntity<List<ZgloszenieDto.Odpowiedz>> moje(Authentication authentication) {
        Uzytkownik u = uzytkownikService.pobierzPrzezLogin(authentication.getName());
        return ResponseEntity.ok(
                zgloszenieService.pobierzZgloszeniaUzytkownika(u.getId())
                        .stream().map(ZgloszenieDto.Odpowiedz::z).toList());
    }

    @GetMapping("/uzytkownik/{idUzytkownika}")
    public ResponseEntity<List<ZgloszenieDto.Odpowiedz>> pobierzUzytkownika(
            @PathVariable Long idUzytkownika) {
        return ResponseEntity.ok(
                zgloszenieService.pobierzZgloszeniaUzytkownika(idUzytkownika)
                        .stream().map(ZgloszenieDto.Odpowiedz::z).toList());
    }

    @PostMapping
    public ResponseEntity<ZgloszenieDto.Odpowiedz> utworz(Authentication authentication,
                                                           @RequestBody ZgloszenieDto.Zapis dto) {
        Uzytkownik u = uzytkownikService.pobierzPrzezLogin(authentication.getName());
        Zgloszenie zapisane = zgloszenieService.utworz(
                u.getId(), dto.getIdKategorii(), dto.getTytul(), dto.getOpis(),
                dto.getPilnosc(), dto.getAdres(), dto.getTerminRealizacji());
        return ResponseEntity.status(HttpStatus.CREATED).body(ZgloszenieDto.Odpowiedz.z(zapisane));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ZgloszenieDto.Odpowiedz> zmienStatus(@PathVariable Long id,
                                                                @RequestBody ZgloszenieDto.ZmianaStatusu dto,
                                                                Authentication authentication) {
        Uzytkownik u = uzytkownikService.pobierzPrzezLogin(authentication.getName());
        Zgloszenie z = zgloszenieService.zmienStatus(id, dto.getNowyStatus(), u.getId(), dto.getKomentarz());
        return ResponseEntity.ok(ZgloszenieDto.Odpowiedz.z(z));
    }

    @PutMapping("/{id}/priorytet")
    public ResponseEntity<ZgloszenieDto.Odpowiedz> ustawPriorytet(@PathVariable Long id,
                                                                    @RequestParam Integer priorytet) {
        return ResponseEntity.ok(ZgloszenieDto.Odpowiedz.z(zgloszenieService.ustawPriorytetReczny(id, priorytet)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
