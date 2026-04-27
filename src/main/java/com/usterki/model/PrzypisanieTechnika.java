package com.usterki.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "przypisania_technikow",
    indexes = @Index(name = "idx_technik_status", columnList = "id_technika, status_przypisania"))
public class PrzypisanieTechnika {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_przypisania")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_zgloszenia", nullable = false)
    private Zgloszenie zgloszenie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_technika", nullable = false)
    private Uzytkownik technik;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_przypisujacego", nullable = false)
    private Uzytkownik przypisujacy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime przypisano = LocalDateTime.now();

    @Column(name = "planowany_start")
    private LocalDateTime planowanyStart;

    @Column(name = "planowane_zakonczenie")
    private LocalDateTime planowaneZakonczenie;

    @Column(name = "faktyczne_zakonczenie")
    private LocalDateTime faktyczneZakonczenie;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_przypisania", nullable = false)
    private StatusPrzypisania statusPrzypisania = StatusPrzypisania.AKTYWNE;

    @Column(columnDefinition = "TEXT")
    private String notatka;

    @Version
    @Column(nullable = false)
    private int wersja = 0;

    public enum StatusPrzypisania { AKTYWNE, ZAKONCZONE, ANULOWANE, PRZEKAZANE }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Zgloszenie getZgloszenie() { return zgloszenie; }
    public void setZgloszenie(Zgloszenie zgloszenie) { this.zgloszenie = zgloszenie; }

    public Uzytkownik getTechnik() { return technik; }
    public void setTechnik(Uzytkownik technik) { this.technik = technik; }

    public Uzytkownik getPrzypisujacy() { return przypisujacy; }
    public void setPrzypisujacy(Uzytkownik przypisujacy) { this.przypisujacy = przypisujacy; }

    public LocalDateTime getPrzypisano() { return przypisano; }

    public LocalDateTime getPlanowanyStart() { return planowanyStart; }
    public void setPlanowanyStart(LocalDateTime planowanyStart) { this.planowanyStart = planowanyStart; }

    public LocalDateTime getPlanowaneZakonczenie() { return planowaneZakonczenie; }
    public void setPlanowaneZakonczenie(LocalDateTime planowaneZakonczenie) { this.planowaneZakonczenie = planowaneZakonczenie; }

    public LocalDateTime getFaktyczneZakonczenie() { return faktyczneZakonczenie; }
    public void setFaktyczneZakonczenie(LocalDateTime faktyczneZakonczenie) { this.faktyczneZakonczenie = faktyczneZakonczenie; }

    public StatusPrzypisania getStatusPrzypisania() { return statusPrzypisania; }
    public void setStatusPrzypisania(StatusPrzypisania statusPrzypisania) { this.statusPrzypisania = statusPrzypisania; }

    public String getNotatka() { return notatka; }
    public void setNotatka(String notatka) { this.notatka = notatka; }

    public int getWersja() { return wersja; }
    public void setWersja(int wersja) { this.wersja = wersja; }
}
