package com.usterki.controller;

import com.usterki.model.Uzytkownik;
import com.usterki.service.UzytkownikService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UzytkownikController {

    private final UzytkownikService uzytkownikService;
    private final AuthenticationManager authenticationManager;

    public UzytkownikController(UzytkownikService uzytkownikService,
                                 AuthenticationManager authenticationManager) {
        this.uzytkownikService = uzytkownikService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        String login = body.get("login");
        String haslo = body.get("haslo");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login, haslo));
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", ctx);
            uzytkownikService.aktualizujOstatnieLogowanie(login);
            return ResponseEntity.ok(toMap(uzytkownikService.pobierzPrzezLogin(login)));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Nieprawidłowy login lub hasło"));
        }
    }

    @PostMapping("/auth/rejestracja")
    public ResponseEntity<?> rejestracja(@RequestBody Map<String, String> body) {
        String login    = body.getOrDefault("login", "").trim();
        String email    = body.getOrDefault("email", "").trim();
        String imie     = body.getOrDefault("imie", "").trim();
        String nazwisko = body.getOrDefault("nazwisko", "").trim();
        String haslo    = body.getOrDefault("haslo", "");
        String telefon  = body.get("telefon");

        if (login.isBlank() || email.isBlank() || imie.isBlank() || nazwisko.isBlank() || haslo.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Wszystkie wymagane pola muszą być wypełnione"));
        if (haslo.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("error", "Hasło musi mieć co najmniej 6 znaków"));

        try {
            Uzytkownik u = uzytkownikService.zarejestruj(login, email, imie, nazwisko, haslo, telefon);
            return ResponseEntity.status(201).body(toMap(u));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Wylogowano"));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Nie zalogowano"));
        }
        return ResponseEntity.ok(toMap(uzytkownikService.pobierzPrzezLogin(authentication.getName())));
    }

    @GetMapping("/uzytkownicy")
    public ResponseEntity<List<Map<String, Object>>> wszyscy() {
        return ResponseEntity.ok(uzytkownikService.pobierzWszystkich().stream()
                .map(this::toMap).toList());
    }

    @GetMapping("/uzytkownicy/technicy")
    public ResponseEntity<List<Map<String, Object>>> technicy() {
        return ResponseEntity.ok(uzytkownikService.pobierzTechnikow().stream()
                .map(this::toMap).toList());
    }

    @PutMapping("/uzytkownicy/{id}/rola")
    public ResponseEntity<?> zmienRole(@PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        try {
            Uzytkownik.Rola rola = Uzytkownik.Rola.valueOf(body.get("rola"));
            return ResponseEntity.ok(toMap(uzytkownikService.zmienRole(id, rola)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nieznana rola: " + body.get("rola")));
        }
    }

    private Map<String, Object> toMap(Uzytkownik u) {
        return Map.of(
            "id",       u.getId(),
            "login",    u.getLogin(),
            "imie",     u.getImie(),
            "nazwisko", u.getNazwisko(),
            "email",    u.getEmail(),
            "rola",     u.getRola().name(),
            "aktywny",  u.isAktywny()
        );
    }
}
