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

    @GetMapping
    public ResponseEntity<Map<String, Object>> statystyki() {
        return ResponseEntity.ok(zgloszenieService.pobierzStatystyki());
    }
}
