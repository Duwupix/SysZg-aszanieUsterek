package com.usterki.controller;

import com.usterki.dto.PrzypisanieDto;
import com.usterki.model.PrzypisanieTechnika;
import com.usterki.model.Uzytkownik;
import com.usterki.service.PrzypisanieService;
import com.usterki.service.UzytkownikService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/przypisania")
public class PrzypisanieController {

    private final PrzypisanieService przypisanieService;
    private final UzytkownikService uzytkownikService;

    public PrzypisanieController(PrzypisanieService przypisanieService,
                                  UzytkownikService uzytkownikService) {
        this.przypisanieService = przypisanieService;
        this.uzytkownikService = uzytkownikService;
    }

    @GetMapping("/technik/{idTechnika}")
    public ResponseEntity<List<PrzypisanieDto.Odpowiedz>> kolejkaTechnika(
            @PathVariable Long idTechnika) {
        return ResponseEntity.ok(
                przypisanieService.kolejkaTechnika(idTechnika)
                        .stream().map(PrzypisanieDto.Odpowiedz::z).toList());
    }

    @PostMapping("/{idZgloszenia}")
    public ResponseEntity<PrzypisanieDto.Odpowiedz> przypisz(@PathVariable Long idZgloszenia,
                                                               @RequestBody PrzypisanieDto.Zapis dto,
                                                               Authentication authentication) {
        Uzytkownik przypisujacy = uzytkownikService.pobierzPrzezLogin(authentication.getName());
        PrzypisanieTechnika p = przypisanieService.przypisz(
                idZgloszenia, dto.getIdTechnika(), przypisujacy.getId(), dto.getPlanowanyStart());
        return ResponseEntity.status(HttpStatus.CREATED).body(PrzypisanieDto.Odpowiedz.z(p));
    }

    @PutMapping("/{id}/zakoncz")
    public ResponseEntity<PrzypisanieDto.Odpowiedz> zakoncz(@PathVariable Long id,
                                                              @RequestParam(required = false) String notatka) {
        return ResponseEntity.ok(PrzypisanieDto.Odpowiedz.z(przypisanieService.zakoncz(id, notatka)));
    }

    @PutMapping("/{id}/anuluj")
    public ResponseEntity<PrzypisanieDto.Odpowiedz> anuluj(@PathVariable Long id,
                                                             @RequestParam(required = false) String powod) {
        return ResponseEntity.ok(PrzypisanieDto.Odpowiedz.z(przypisanieService.anuluj(id, powod)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Konflikt współbieżności – spróbuj ponownie.");
    }
}
