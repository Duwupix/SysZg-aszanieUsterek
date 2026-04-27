package com.usterki.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "kategorie_usterek")
public class KategoriaUsterki {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kategorii")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nazwa;

    @Column(columnDefinition = "TEXT")
    private String opis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kategorii_nadrz")
    private KategoriaUsterki kategoriaUsterki;

    @Column(name = "domyslny_priorytet", nullable = false)
    private int domyslnyPriorytet = 50;

    @Column(name = "szac_czas_godz", nullable = false)
    private int szacCzasGodz = 8;

    @Column(name = "wspolczynnik_wagi", nullable = false, precision = 4, scale = 2)
    private BigDecimal wspolczynnikWagi = BigDecimal.ONE;

    @Column(nullable = false)
    private boolean aktywna = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }

    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }

    public KategoriaUsterki getKategoriaUsterki() { return kategoriaUsterki; }
    public void setKategoriaUsterki(KategoriaUsterki kategoriaUsterki) { this.kategoriaUsterki = kategoriaUsterki; }

    public int getDomyslnyPriorytet() { return domyslnyPriorytet; }
    public void setDomyslnyPriorytet(int domyslnyPriorytet) { this.domyslnyPriorytet = domyslnyPriorytet; }

    public int getSzacCzasGodz() { return szacCzasGodz; }
    public void setSzacCzasGodz(int szacCzasGodz) { this.szacCzasGodz = szacCzasGodz; }

    public BigDecimal getWspolczynnikWagi() { return wspolczynnikWagi; }
    public void setWspolczynnikWagi(BigDecimal wspolczynnikWagi) { this.wspolczynnikWagi = wspolczynnikWagi; }

    public boolean isAktywna() { return aktywna; }
    public void setAktywna(boolean aktywna) { this.aktywna = aktywna; }
}
