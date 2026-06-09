package com.usterki.controller;

import com.usterki.facade.PulpitFasada;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Punkt dostępu REST dla pulpitu. Cała złożoność składania danych jest ukryta
 * w {@link PulpitFasada} — kontroler tylko ją wywołuje.
 */
@RestController
@RequestMapping("/api/pulpit")
public class PulpitController {

    private final PulpitFasada pulpitFasada;

    public PulpitController(PulpitFasada pulpitFasada) {
        this.pulpitFasada = pulpitFasada;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> pulpit() {
        return ResponseEntity.ok(pulpitFasada.pobierzDanePulpitu());
    }
}
