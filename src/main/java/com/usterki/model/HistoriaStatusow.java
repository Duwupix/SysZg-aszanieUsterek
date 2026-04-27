package com.usterki.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historia_statusow")
public class HistoriaStatusow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historii")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_zgloszenia", nullable = false)
    private Zgloszenie zgloszenie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_uzytkownika", nullable = false)
    private Uzytkownik uzytkownik;

    @Enumerated(EnumType.STRING)
    @Column(name = "stary_status")
    private Zgloszenie.Status staryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "nowy_status", nullable = false)
    private Zgloszenie.Status nowyStatus;

    @Column(columnDefinition = "TEXT")
    private String komentarz;

    @Column(nullable = false, updatable = false)
    private LocalDateTime zmieniono = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Zgloszenie getZgloszenie() { return zgloszenie; }
    public void setZgloszenie(Zgloszenie zgloszenie) { this.zgloszenie = zgloszenie; }

    public Uzytkownik getUzytkownik() { return uzytkownik; }
    public void setUzytkownik(Uzytkownik uzytkownik) { this.uzytkownik = uzytkownik; }

    public Zgloszenie.Status getStaryStatus() { return staryStatus; }
    public void setStaryStatus(Zgloszenie.Status staryStatus) { this.staryStatus = staryStatus; }

    public Zgloszenie.Status getNowyStatus() { return nowyStatus; }
    public void setNowyStatus(Zgloszenie.Status nowyStatus) { this.nowyStatus = nowyStatus; }

    public String getKomentarz() { return komentarz; }
    public void setKomentarz(String komentarz) { this.komentarz = komentarz; }

    public LocalDateTime getZmieniono() { return zmieniono; }
}
