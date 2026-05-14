package com.usterki.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "zgloszenia",
    indexes = {
        @Index(name = "idx_status_priorytet", columnList = "status, priorytet_obliczony"),
        @Index(name = "idx_data_zauwazeniausterki", columnList = "data_zauwazeniausterki")
    })
public class Zgloszenie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zgloszenia")
    private Long id;

    @Column(name = "numer_zgloszenia", nullable = false, unique = true, length = 20)
    private String numerZgloszenia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_zglaszajacego", nullable = false)
    private Uzytkownik zglaszajacy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kategorii", nullable = false)
    private KategoriaUsterki kategoria;

    @Column(nullable = false, length = 200)
    private String tytul;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String opis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NOWE;

    @Column(name = "priorytet_obliczony", nullable = false)
    private int priorytetObliczony = 50;

    @Column(name = "priorytet_reczny")
    private Integer priorytetReczny;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Pilnosc pilnosc = Pilnosc.SREDNIA;

    @Column(name = "szerokosc_geo", precision = 10, scale = 7)
    private BigDecimal szerokoscGeo;

    @Column(name = "dlugosc_geo", precision = 10, scale = 7)
    private BigDecimal dlugoscGeo;

    @Column(length = 300)
    private String adres;

    /** Data, kiedy zgłaszający zauważył usterkę (może być wcześniejsza niż data zgłoszenia). */
    @Column(name = "data_zauwazeniausterki")
    private LocalDateTime dataZauwazeniaUsterki;

    @Column
    private LocalDateTime zamknieto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime utworzono = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime zaktualizowano = LocalDateTime.now();

    @Version
    @Column(nullable = false)
    private int wersja = 0;

    @PreUpdate
    void preUpdate() { this.zaktualizowano = LocalDateTime.now(); }

    public int efektywnyPriorytet() {
        return priorytetReczny != null ? priorytetReczny : priorytetObliczony;
    }

    public enum Status { NOWE, W_TOKU, OCZEKUJE, ROZWIAZANE, ZAMKNIETE, ODRZUCONE }
    public enum Pilnosc { NATYCHMIASTOWA, WYSOKA, SREDNIA, NISKA }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumerZgloszenia() { return numerZgloszenia; }
    public void setNumerZgloszenia(String numerZgloszenia) { this.numerZgloszenia = numerZgloszenia; }

    public Uzytkownik getZglaszajacy() { return zglaszajacy; }
    public void setZglaszajacy(Uzytkownik zglaszajacy) { this.zglaszajacy = zglaszajacy; }

    public KategoriaUsterki getKategoria() { return kategoria; }
    public void setKategoria(KategoriaUsterki kategoria) { this.kategoria = kategoria; }

    public String getTytul() { return tytul; }
    public void setTytul(String tytul) { this.tytul = tytul; }

    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getPriorytetObliczony() { return priorytetObliczony; }
    public void setPriorytetObliczony(int priorytetObliczony) { this.priorytetObliczony = priorytetObliczony; }

    public Integer getPriorytetReczny() { return priorytetReczny; }
    public void setPriorytetReczny(Integer priorytetReczny) { this.priorytetReczny = priorytetReczny; }

    public Pilnosc getPilnosc() { return pilnosc; }
    public void setPilnosc(Pilnosc pilnosc) { this.pilnosc = pilnosc; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public LocalDateTime getDataZauwazeniaUsterki() { return dataZauwazeniaUsterki; }
    public void setDataZauwazeniaUsterki(LocalDateTime dataZauwazeniaUsterki) { this.dataZauwazeniaUsterki = dataZauwazeniaUsterki; }

    public LocalDateTime getZamknieto() { return zamknieto; }
    public void setZamknieto(LocalDateTime zamknieto) { this.zamknieto = zamknieto; }

    public LocalDateTime getUtworzono() { return utworzono; }
    public LocalDateTime getZaktualizowano() { return zaktualizowano; }

    public int getWersja() { return wersja; }
    public void setWersja(int wersja) { this.wersja = wersja; }
}
