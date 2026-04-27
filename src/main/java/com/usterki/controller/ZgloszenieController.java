package com.usterki.controller;

import com.usterki.dto.ZgloszenieDto;
import com.usterki.model.Zgloszenie;
import com.usterki.service.ZgloszenieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zgloszenia")
@CrossOrigin(origins = "*")
public class ZgloszenieController {

    private final ZgloszenieService zgloszenieService;

    public ZgloszenieController(ZgloszenieService zgloszenieService) {
        this.zgloszenieService = zgloszenieService;
    }

    @GetMapping
    public ResponseEntity<List<ZgloszenieDto.Odpowiedz>> pobierzKolejke() {
        List<ZgloszenieDto.Odpowiedz> lista = zgloszenieService.pobierzKolejke()
                .stream().map(ZgloszenieDto.Odpowiedz::z).toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/uzytkownik/{idUzytkownika}")
    public ResponseEntity<List<ZgloszenieDto.Odpowiedz>> pobierzUzytkownika(@PathVariable Long idUzytkownika) {
        List<ZgloszenieDto.Odpowiedz> lista = zgloszenieService.pobierzZgloszeniaUzytkownika(idUzytkownika)
                .stream().map(ZgloszenieDto.Odpowiedz::z).toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{idZglaszajacego}")
    public ResponseEntity<ZgloszenieDto.Odpowiedz> utworz(@PathVariable Long idZglaszajacego,
                                                           @RequestBody ZgloszenieDto.Zapis dto) {
        Zgloszenie zapisane = zgloszenieService.utworz(
                idZglaszajacego,
                dto.getIdKategorii(),
                dto.getTytul(),
                dto.getOpis(),
                dto.getPilnosc(),
                dto.getAdres(),
                dto.getTerminRealizacji());
        return ResponseEntity.status(HttpStatus.CREATED).body(ZgloszenieDto.Odpowiedz.z(zapisane));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ZgloszenieDto.Odpowiedz> zmienStatus(@PathVariable Long id,
                                                                @RequestBody ZgloszenieDto.ZmianaStatusu dto) {
        Zgloszenie z = zgloszenieService.zmienStatus(id, dto.getNowyStatus(), dto.getIdUzytkownika(), dto.getKomentarz());
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
