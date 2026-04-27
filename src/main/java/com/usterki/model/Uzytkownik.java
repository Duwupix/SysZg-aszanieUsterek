package com.usterki.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uzytkownicy")
public class Uzytkownik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_uzytkownika")
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @Column(name = "haslo_hash", nullable = false, length = 255)
    private String hasloHash;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 60)
    private String imie;

    @Column(nullable = false, length = 80)
    private String nazwisko;

    @Column(length = 20)
    private String telefon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rola rola = Rola.ZGLASZAJACY;

    @Column(nullable = false)
    private boolean aktywny = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime utworzono = LocalDateTime.now();

    @Column(name = "ostatnie_logowanie")
    private LocalDateTime ostatnieLogowanie;

    @Version
    @Column(nullable = false)
    private int wersja = 0;

    public enum Rola { ZGLASZAJACY, TECHNIK, ADMINISTRATOR }

    public String pelneNazwisko() { return imie + " " + nazwisko; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getHasloHash() { return hasloHash; }
    public void setHasloHash(String hasloHash) { this.hasloHash = hasloHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public Rola getRola() { return rola; }
    public void setRola(Rola rola) { this.rola = rola; }

    public boolean isAktywny() { return aktywny; }
    public void setAktywny(boolean aktywny) { this.aktywny = aktywny; }

    public LocalDateTime getUtworzono() { return utworzono; }
    public LocalDateTime getOstatnieLogowanie() { return ostatnieLogowanie; }
    public void setOstatnieLogowanie(LocalDateTime ostatnieLogowanie) { this.ostatnieLogowanie = ostatnieLogowanie; }

    public int getWersja() { return wersja; }
    public void setWersja(int wersja) { this.wersja = wersja; }
}
