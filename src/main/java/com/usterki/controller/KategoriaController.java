package com.usterki.controller;

import com.usterki.model.KategoriaUsterki;
import com.usterki.repository.KategoriaUsterkiRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kategorie")
@CrossOrigin(origins = "*")
public class KategoriaController {

    private final KategoriaUsterkiRepository kategoriaRepo;

    public KategoriaController(KategoriaUsterkiRepository kategoriaRepo) {
        this.kategoriaRepo = kategoriaRepo;
    }

    @GetMapping
    public ResponseEntity<List<KategoriaUsterki>> pobierzWszystkie() {
        return ResponseEntity.ok(kategoriaRepo.findByAktywnaTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KategoriaUsterki> pobierzJedna(@PathVariable Long id) {
        return kategoriaRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
