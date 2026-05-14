package com.usterki.controller;

import com.usterki.service.ZgloszenieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statystyki")
public class StatystykiController {

    private final ZgloszenieService zgloszenieService;

    public StatystykiController(ZgloszenieService zgloszenieService) {
        this.zgloszenieService = zgloszenieService;
    }

    /**
     * Zwraca agregaty do widoku statystyk:
     * <pre>
     * {
     *   porStatus:            { "NOWE": 5, "W_TOKU": 3, … },
     *   porKategoria:         [ { "nazwa": "Drogi", "liczba": 10 }, … ],
     *   srCzasRealizacjiGodz: 14.5,
     *   dzisiaj:              3,
     *   tenTydzien:           15,
     *   tenMiesiac:           42,
     *   lacznie:              78
     * }
     * </pre>
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> statystyki() {
        return ResponseEntity.ok(zgloszenieService.pobierzStatystyki());
    }
}
