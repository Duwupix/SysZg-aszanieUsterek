package com.usterki.service;

import com.usterki.model.Uzytkownik;
import com.usterki.repository.UzytkownikRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UzytkownikService implements UserDetailsService {

    private final UzytkownikRepository uzytkownikRepository;
    private final PasswordEncoder passwordEncoder;

    public UzytkownikService(UzytkownikRepository uzytkownikRepository, PasswordEncoder passwordEncoder) {
        this.uzytkownikRepository = uzytkownikRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Uzytkownik u = uzytkownikRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie istnieje: " + login));
        if (!u.isAktywny())
            throw new UsernameNotFoundException("Konto nieaktywne: " + login);
        return User.builder()
                .username(u.getLogin())
                .password(u.getHasloHash())
                .authorities(new SimpleGrantedAuthority("ROLE_" + u.getRola().name()))
                .build();
    }

    @Transactional
    public Uzytkownik zmienRole(Long id, Uzytkownik.Rola nowaRola) {
        Uzytkownik u = uzytkownikRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + id));
        u.setRola(nowaRola);
        return uzytkownikRepository.save(u);
    }

    @Transactional
    public void aktualizujOstatnieLogowanie(String login) {
        uzytkownikRepository.findByLogin(login).ifPresent(u -> {
            u.setOstatnieLogowanie(LocalDateTime.now());
            uzytkownikRepository.save(u);
        });
    }

    public Uzytkownik pobierzPrzezLogin(String login) {
        return uzytkownikRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + login));
    }

    public List<Uzytkownik> pobierzWszystkich() {
        return uzytkownikRepository.findAll();
    }

    public List<Uzytkownik> pobierzTechnikow() {
        return uzytkownikRepository.findByRolaAndAktywny(Uzytkownik.Rola.TECHNIK, true);
    }

    @Transactional
    public Uzytkownik zarejestruj(String login, String email, String imie,
                                   String nazwisko, String haslo, String telefon) {
        if (uzytkownikRepository.findByLogin(login).isPresent())
            throw new IllegalStateException("Login jest już zajęty: " + login);
        if (uzytkownikRepository.findByEmail(email).isPresent())
            throw new IllegalStateException("Adres e-mail jest już używany");

        Uzytkownik u = new Uzytkownik();
        u.setLogin(login);
        u.setEmail(email);
        u.setImie(imie);
        u.setNazwisko(nazwisko);
        u.setHasloHash(passwordEncoder.encode(haslo));
        u.setTelefon(telefon);
        u.setRola(Uzytkownik.Rola.ZGLASZAJACY);
        return uzytkownikRepository.save(u);
    }
}
