package com.usterki.config;

import com.usterki.model.Uzytkownik;
import com.usterki.repository.UzytkownikRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UzytkownikRepository uzytkownikRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UzytkownikRepository uzytkownikRepository, PasswordEncoder passwordEncoder) {
        this.uzytkownikRepository = uzytkownikRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // ── Administratorzy ───────────────────────────────────────────────
        create("admin",           "admin@miasto.pl",          "Admin",     "Systemowy",     "admin123",   Uzytkownik.Rola.ADMINISTRATOR);

        // ── Technicy ──────────────────────────────────────────────────────
        create("technik1",        "technik1@miasto.pl",       "Marek",     "Kowalski",      "technik123", Uzytkownik.Rola.TECHNIK);
        create("technik2",        "technik2@miasto.pl",       "Piotr",     "Nowak",         "technik123", Uzytkownik.Rola.TECHNIK);
        create("technik3",        "technik3@miasto.pl",       "Tomasz",    "Wiśniewski",    "technik123", Uzytkownik.Rola.TECHNIK);
        create("technik4",        "technik4@miasto.pl",       "Michał",    "Wójcik",        "technik123", Uzytkownik.Rola.TECHNIK);
        create("technik5",        "technik5@miasto.pl",       "Adam",      "Lewandowski",   "technik123", Uzytkownik.Rola.TECHNIK);

        // ── Zgłaszający ───────────────────────────────────────────────────
        create("jan.kowalski",    "jan@example.pl",           "Jan",       "Kowalski",      "user123",    Uzytkownik.Rola.ZGLASZAJACY);
        create("anna.nowak",      "anna.nowak@example.pl",    "Anna",      "Nowak",         "user123",    Uzytkownik.Rola.ZGLASZAJACY);
        create("krzysztof.w",     "krzysztof@example.pl",     "Krzysztof", "Wróblewski",    "user123",    Uzytkownik.Rola.ZGLASZAJACY);
        create("marta.zielinska", "marta.z@example.pl",       "Marta",     "Zielińska",     "user123",    Uzytkownik.Rola.ZGLASZAJACY);
        create("pawel.dąbrowski", "pawel.d@example.pl",       "Paweł",     "Dąbrowski",     "user123",    Uzytkownik.Rola.ZGLASZAJACY);
        create("ewa.kaminska",    "ewa.k@example.pl",         "Ewa",       "Kamińska",      "user123",    Uzytkownik.Rola.ZGLASZAJACY);
        create("robert.lewicki",  "robert.l@example.pl",      "Robert",    "Lewicki",       "user123",    Uzytkownik.Rola.ZGLASZAJACY);
    }

    private void create(String login, String email, String imie, String nazwisko,
                        String haslo, Uzytkownik.Rola rola) {
        if (uzytkownikRepository.findByLogin(login).isPresent()) return;
        Uzytkownik u = new Uzytkownik();
        u.setLogin(login);
        u.setEmail(email);
        u.setImie(imie);
        u.setNazwisko(nazwisko);
        u.setHasloHash(passwordEncoder.encode(haslo));
        u.setRola(rola);
        uzytkownikRepository.save(u);
    }
}
