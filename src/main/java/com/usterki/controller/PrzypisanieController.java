package com.usterki.controller;

import com.usterki.dto.PrzypisanieDto;
import com.usterki.model.PrzypisanieTechnika;
import com.usterki.service.PrzypisanieService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/przypisania")
@CrossOrigin(origins = "*")
public class PrzypisanieController {

    private final PrzypisanieService przypisanieService;

    public PrzypisanieController(PrzypisanieService przypisanieService) {
        this.przypisanieService = przypisanieService;
    }

    @GetMapping("/technik/{idTechnika}")
    public ResponseEntity<List<PrzypisanieDto.Odpowiedz>> kolejkaTechnika(@PathVariable Long idTechnika) {
        List<PrzypisanieDto.Odpowiedz> lista = przypisanieService.kolejkaTechnika(idTechnika)
                .stream().map(PrzypisanieDto.Odpowiedz::z).toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{idZgloszenia}")
    public ResponseEntity<PrzypisanieDto.Odpowiedz> przypisz(@PathVariable Long idZgloszenia,
                                                               @RequestBody PrzypisanieDto.Zapis dto) {
        PrzypisanieTechnika p = przypisanieService.przypisz(
                idZgloszenia, dto.getIdTechnika(), dto.getIdPrzypisujacego(), dto.getPlanowanyStart());
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
