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
        create("admin",        "admin@miasto.pl",     "Admin",    "Systemowy",    "admin123",    Uzytkownik.Rola.ADMINISTRATOR);
        create("technik1",     "technik1@miasto.pl",  "Marek",    "Kowalski",     "technik123",  Uzytkownik.Rola.TECHNIK);
        create("technik2",     "technik2@miasto.pl",  "Piotr",    "Nowak",        "technik123",  Uzytkownik.Rola.TECHNIK);
        create("technik3",     "technik3@miasto.pl",  "Tomasz",   "Wiśniewski",   "technik123",  Uzytkownik.Rola.TECHNIK);
        create("technik4",     "technik4@miasto.pl",  "Michał",   "Wójcik",       "technik123",  Uzytkownik.Rola.TECHNIK);
        create("technik5",     "technik5@miasto.pl",  "Adam",     "Lewandowski",  "technik123",  Uzytkownik.Rola.TECHNIK);
        create("jan.kowalski", "jan@example.pl",      "Jan",      "Kowalski",     "user123",     Uzytkownik.Rola.ZGLASZAJACY);
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
