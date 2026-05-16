CREATE DATABASE IF NOT EXISTS infrastruktura
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE infrastruktura;

CREATE TABLE IF NOT EXISTS uzytkownicy (
    id_uzytkownika   BIGINT        NOT NULL AUTO_INCREMENT,
    login            VARCHAR(60)   NOT NULL,
    haslo_hash       VARCHAR(255)  NOT NULL,
    email            VARCHAR(120)  NOT NULL,
    imie             VARCHAR(60)   NOT NULL,
    nazwisko         VARCHAR(80)   NOT NULL,
    telefon          VARCHAR(20)   DEFAULT NULL,
    rola             VARCHAR(20)   NOT NULL DEFAULT 'ZGLASZAJACY',
    aktywny          TINYINT(1)    NOT NULL DEFAULT 1,
    utworzono        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ostatnie_logowanie DATETIME    DEFAULT NULL,
    wersja           INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id_uzytkownika),
    UNIQUE KEY uq_login (login),
    UNIQUE KEY uq_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS kategorie_usterek (
    id_kategorii       BIGINT        NOT NULL AUTO_INCREMENT,
    nazwa              VARCHAR(100)  NOT NULL,
    opis               TEXT          DEFAULT NULL,
    id_kategorii_nadrz BIGINT        DEFAULT NULL,
    domyslny_priorytet INT           NOT NULL DEFAULT 50,
    szac_czas_godz     INT           NOT NULL DEFAULT 8,
    wspolczynnik_wagi  DECIMAL(4,2)  NOT NULL DEFAULT 1.00,
    aktywna            TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id_kategorii),
    UNIQUE KEY uq_nazwa (nazwa),
    CONSTRAINT fk_kat_nadrz FOREIGN KEY (id_kategorii_nadrz)
        REFERENCES kategorie_usterek (id_kategorii)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS zgloszenia (
    id_zgloszenia      BIGINT        NOT NULL AUTO_INCREMENT,
    numer_zgloszenia   VARCHAR(20)   NOT NULL,
    id_zglaszajacego   BIGINT        NOT NULL,
    id_kategorii       BIGINT        NOT NULL,
    tytul              VARCHAR(200)  NOT NULL,
    opis               TEXT          NOT NULL,
    status             VARCHAR(20)   NOT NULL DEFAULT 'NOWE',
    priorytet_obliczony INT          NOT NULL DEFAULT 50,
    priorytet_reczny   INT           DEFAULT NULL,
    pilnosc            VARCHAR(20)   NOT NULL DEFAULT 'SREDNIA',
    szerokosc_geo      DECIMAL(10,7) DEFAULT NULL,
    dlugosc_geo        DECIMAL(10,7) DEFAULT NULL,
    adres              VARCHAR(300)  DEFAULT NULL,
    data_zauwazeniausterki DATETIME  DEFAULT NULL,
    zamknieto          DATETIME      DEFAULT NULL,
    utworzono          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    zaktualizowano     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    wersja             INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id_zgloszenia),
    UNIQUE KEY uq_numer (numer_zgloszenia),
    KEY idx_status_priorytet (status, priorytet_obliczony),
    KEY idx_data_zauwazeniausterki (data_zauwazeniausterki),
    CONSTRAINT fk_zgl_uzytkownik FOREIGN KEY (id_zglaszajacego)
        REFERENCES uzytkownicy (id_uzytkownika),
    CONSTRAINT fk_zgl_kategoria FOREIGN KEY (id_kategorii)
        REFERENCES kategorie_usterek (id_kategorii)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS przypisania_technikow (
    id_przypisania         BIGINT   NOT NULL AUTO_INCREMENT,
    id_zgloszenia          BIGINT   NOT NULL,
    id_technika            BIGINT   NOT NULL,
    id_przypisujacego      BIGINT   NOT NULL,
    przypisano             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    planowany_start        DATETIME DEFAULT NULL,
    planowane_zakonczenie  DATETIME DEFAULT NULL,
    faktyczne_zakonczenie  DATETIME DEFAULT NULL,
    status_przypisania     VARCHAR(20) NOT NULL DEFAULT 'AKTYWNE',
    notatka                TEXT     DEFAULT NULL,
    wersja                 INT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id_przypisania),
    KEY idx_technik_status (id_technika, status_przypisania),
    CONSTRAINT fk_prz_zgloszenie  FOREIGN KEY (id_zgloszenia)
        REFERENCES zgloszenia (id_zgloszenia),
    CONSTRAINT fk_prz_technik     FOREIGN KEY (id_technika)
        REFERENCES uzytkownicy (id_uzytkownika),
    CONSTRAINT fk_prz_przypisujacy FOREIGN KEY (id_przypisujacego)
        REFERENCES uzytkownicy (id_uzytkownika)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS historia_statusow (
    id_historii   BIGINT      NOT NULL AUTO_INCREMENT,
    id_zgloszenia BIGINT      NOT NULL,
    id_uzytkownika BIGINT     NOT NULL,
    stary_status  VARCHAR(20) DEFAULT NULL,
    nowy_status   VARCHAR(20) NOT NULL,
    komentarz     TEXT        DEFAULT NULL,
    zmieniono     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_historii),
    CONSTRAINT fk_hist_zgloszenie  FOREIGN KEY (id_zgloszenia)
        REFERENCES zgloszenia (id_zgloszenia),
    CONSTRAINT fk_hist_uzytkownik  FOREIGN KEY (id_uzytkownika)
        REFERENCES uzytkownicy (id_uzytkownika)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO kategorie_usterek
    (nazwa, opis, domyslny_priorytet, szac_czas_godz, wspolczynnik_wagi, aktywna)
VALUES
    ('Drogi i chodniki',     'Dziury, pęknięcia, uszkodzone chodniki',          30, 8,  1.50, 1),
    ('Oświetlenie',          'Niedziałające latarnie, uszkodzone słupy',        40, 4,  1.20, 1),
    ('Kanalizacja',          'Zatkane studzienki, wycieki, zalewanie',          20, 12, 1.80, 1),
    ('Zieleń miejska',       'Połamane drzewa, zaniedbane trawniki',            60, 6,  0.90, 1),
    ('Sygnalizacja świetlna','Niedziałające lub wadliwe sygnalizatory',         15, 3,  2.00, 1),
    ('Odpady i czystość',    'Dzikie wysypiska, przepełnione kosze',            50, 4,  1.00, 1),
    ('Infrastruktura wodno-kanalizacyjna', 'Awarie wodociągów, przerwy w dostawie', 10, 16, 2.00, 1),
    ('Ławki i mała architektura', 'Uszkodzone ławki, kosze, wiaty',            70, 4,  0.80, 1),
    ('Place zabaw',          'Uszkodzone urządzenia, zagrożenia dla dzieci',   25, 6,  1.60, 1),
    ('Inne',                 'Zgłoszenia nieprzypisane do innej kategorii',     55, 8,  1.00, 1);


SET @admin  = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'admin');
SET @tech1  = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'technik1');
SET @tech2  = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'technik2');
SET @tech3  = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'technik3');
SET @tech4  = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'technik4');
SET @tech5  = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'technik5');
SET @jan    = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'jan.kowalski');

-- ── Zmienne pomocnicze: kategorie ────────────────────────────
SET @kDrogi  = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Drogi i chodniki');
SET @kOswiet = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Oświetlenie');
SET @kKanal  = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Kanalizacja');
SET @kZielen = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Zieleń miejska');
SET @kSyg    = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Sygnalizacja świetlna');
SET @kOdpad  = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Odpady i czystość');
SET @kWoda   = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Infrastruktura wodno-kanalizacyjna');
SET @kLawki  = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Ławki i mała architektura');
SET @kPlace  = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Place zabaw');
SET @kInne   = (SELECT id_kategorii FROM kategorie_usterek WHERE nazwa = 'Inne');

-- ── 30 zgłoszeń ──────────────────────────────────────────────
INSERT INTO zgloszenia
  (numer_zgloszenia, id_zglaszajacego, id_kategorii,
   tytul, opis,
   status, priorytet_obliczony, pilnosc,
   szerokosc_geo, dlugosc_geo, adres,
   data_zauwazeniausterki, zamknieto,
   utworzono, zaktualizowano, wersja)
VALUES
-- NOWE ×8
('ZGL-20260401-000001', @jan, @kDrogi,
 'Dziura w jezdni na ul. Kwiatowej',
 'Na skrzyżowaniu ul. Kwiatowej i Różanej widnieje dziura w asfalcie o średnicy ok. 40 cm. Stanowi zagrożenie dla rowerzystów i motocyklistów, szczególnie w nocy.',
 'NOWE', 31, 'WYSOKA', 52.2315, 21.0089, 'ul. Kwiatowa 12, Warszawa',
 '2026-04-15 10:00:00', NULL, '2026-04-01 08:22:00', '2026-04-01 08:22:00', 0),

('ZGL-20260402-000002', @jan, @kOswiet,
 'Niedziałająca latarnia przy parku Wilanowskim',
 'Latarnia nr 47 przy wejściu głównym do parku jest wyłączona od ponad tygodnia. Po zmroku dojście do parku jest niebezpieczne.',
 'NOWE', 48, 'SREDNIA', 52.1652, 21.0890, 'ul. Wiertnicza 26, Warszawa',
 NULL, NULL, '2026-04-02 12:05:00', '2026-04-02 12:05:00', 0),

('ZGL-20260403-000003', @jan, @kWoda,
 'Pęknięta rura – wyciek wody na ul. Puławskiej',
 'Z pękniętego przewodu wodociągowego wycieka woda na jezdnię. Chodnik jest zalany, istnieje ryzyko podmycia podbudowy jezdni. Wymagana natychmiastowa interwencja.',
 'NOWE', 10, 'NATYCHMIASTOWA', 52.1981, 21.0338, 'ul. Puławska 140, Warszawa',
 '2026-04-05 08:00:00', NULL, '2026-04-03 07:14:00', '2026-04-03 07:14:00', 0),

('ZGL-20260404-000004', @jan, @kZielen,
 'Połamana gałąź blokuje chodnik przy al. Niepodległości',
 'Po nocnej burzy duża gałąź klonu (ok. 3 m) spadła na chodnik. Blokuje przejście na odcinku 2 m, w pobliżu przedszkola.',
 'NOWE', 54, 'SREDNIA', 52.2015, 21.0069, 'al. Niepodległości 34, Warszawa',
 NULL, NULL, '2026-04-04 09:30:00', '2026-04-04 09:30:00', 0),

('ZGL-20260405-000005', @jan, @kSyg,
 'Brak sygnału zielonego dla pieszych – Al. Jerozolimskie',
 'Sygnalizacja na skrzyżowaniu Al. Jerozolimskie / Nowy Świat nie wyświetla zielonego dla pieszych na przejściu po stronie zachodniej.',
 'NOWE', 21, 'WYSOKA', 52.2330, 21.0176, 'Al. Jerozolimskie 45, Warszawa',
 '2026-04-08 12:00:00', NULL, '2026-04-05 10:45:00', '2026-04-05 10:45:00', 0),

('ZGL-20260408-000006', @jan, @kWoda,
 'Brak wody w budynku – awaria głównego przewodu',
 'Cały blok przy ul. Mokotowskiej od godziny 5 rano pozbawiony jest wody. Prawdopodobnie awaria głównego przewodu w piwnicy.',
 'NOWE', 10, 'NATYCHMIASTOWA', 52.2205, 21.0171, 'ul. Mokotowska 22, Warszawa',
 '2026-04-09 06:00:00', NULL, '2026-04-08 06:30:00', '2026-04-08 06:30:00', 0),

('ZGL-20260410-000007', @jan, @kPlace,
 'Ostre krawędzie zjeżdżalni w parku Ujazdowskim',
 'Metalowa zjeżdżalnia ma skorodowane, ostre krawędzie przy bocznych prowadnicach. Jedno z dzieci skaleczyło się w dłoń.',
 'NOWE', 28, 'WYSOKA', 52.2195, 21.0262, 'Park Ujazdowski, Warszawa',
 '2026-04-18 10:00:00', NULL, '2026-04-10 14:22:00', '2026-04-10 14:22:00', 0),

('ZGL-20260412-000008', @jan, @kOdpad,
 'Dzikie wysypisko za garażami przy ul. Bielańskiej',
 'Za rzędem garaży nagromadziło się duże skupisko odpadów: gruz budowlany, stare meble, worki z odpadkami. Szacowana objętość ok. 8 m³.',
 'NOWE', 50, 'SREDNIA', 52.2492, 21.0059, 'ul. Bielańska 7, Warszawa',
 NULL, NULL, '2026-04-12 11:00:00', '2026-04-12 11:00:00', 0),

-- W_TOKU ×6
('ZGL-20260315-000009', @jan, @kDrogi,
 'Spękane płyty chodnikowe przy SP nr 12',
 'Płyty chodnikowe przy Szkole Podstawowej nr 12 są głęboko spękane i się ruszają. Dzieci regularnie się potykają.',
 'W_TOKU', 31, 'WYSOKA', 52.2421, 21.0211, 'ul. Szkolna 3, Warszawa',
 '2026-04-20 10:00:00', NULL, '2026-03-15 08:00:00', '2026-03-18 09:00:00', 1),

('ZGL-20260316-000010', @jan, @kSyg,
 'Sygnalizacja w trybie awaryjnym – Rondo Waszyngtona',
 'Sygnalizacja przy Rondzie Waszyngtona pracuje w trybie migającego żółtego od 3 dni. Kierowcy są zdezorientowani.',
 'W_TOKU', 15, 'NATYCHMIASTOWA', 52.2386, 21.0523, 'Rondo Waszyngtona, Warszawa',
 '2026-03-20 08:00:00', NULL, '2026-03-16 07:30:00', '2026-03-17 10:00:00', 1),

('ZGL-20260318-000011', @jan, @kKanal,
 'Zatkana studzienka – zalewanie chodnika przy ul. Narbutta',
 'Kratka ściekowa przy ul. Narbutta 31 jest całkowicie zatkana. Po każdym deszczu chodnik zalewany przez kilka godzin.',
 'W_TOKU', 25, 'WYSOKA', 52.2113, 21.0203, 'ul. Narbutta 31, Warszawa',
 '2026-04-25 10:00:00', NULL, '2026-03-18 14:00:00', '2026-03-20 09:00:00', 1),

('ZGL-20260320-000012', @jan, @kOswiet,
 'Trzy latarnie wyłączone na ul. Wawelskiej',
 'Słupki oświetleniowe nr 12, 13 i 14 są wyłączone. Odcinek ok. 200 m jest całkowicie ciemny nocą.',
 'W_TOKU', 33, 'WYSOKA', 52.2198, 20.9932, 'ul. Wawelska 45, Warszawa',
 '2026-04-10 12:00:00', NULL, '2026-03-20 20:15:00', '2026-03-22 08:00:00', 1),

('ZGL-20260322-000013', @jan, @kWoda,
 'Awaria magistrali ciepłowniczej – brak ciepłej wody na Żoliborzu',
 'Od rana brak ciepłej wody w rejonie Żoliborza (ul. Mickiewicza, Słowackiego, Czarnieckiego). MPWiK zgłoszone.',
 'W_TOKU', 10, 'NATYCHMIASTOWA', 52.2636, 20.9884, 'ul. Mickiewicza 10, Warszawa',
 '2026-03-25 12:00:00', NULL, '2026-03-22 05:00:00', '2026-03-22 08:00:00', 1),

('ZGL-20260325-000014', @jan, @kPlace,
 'Zerwany łańcuch huśtawki – park przy ul. Stalowej',
 'Huśtawka nr 2 ma całkowicie zerwany łańcuch po jednej stronie. Siedzisko zwisa, dziecko może spaść.',
 'W_TOKU', 28, 'WYSOKA', 52.2558, 21.0418, 'ul. Stalowa 15, Warszawa',
 '2026-04-10 10:00:00', NULL, '2026-03-25 10:30:00', '2026-03-26 09:00:00', 1),

-- OCZEKUJE ×5
('ZGL-20260301-000015', @jan, @kDrogi,
 'Zapadnięta nawierzchnia po remoncie rur na ul. Chmielnej',
 'Po lutowym remoncie sieci wodociągowej nawierzchnia zapadła się na ok. 3 cm. Zbiera wodę, zamarza zimą.',
 'OCZEKUJE', 45, 'SREDNIA', 52.2280, 21.0050, 'ul. Chmielna 89, Warszawa',
 '2026-05-01 10:00:00', NULL, '2026-03-01 09:00:00', '2026-03-10 11:00:00', 2),

('ZGL-20260303-000016', @jan, @kOdpad,
 'Codziennie przepełniony kosz przy wyjściu z metra Centrum',
 'Kosz przy wyjściu A z metra Centrum przepełnia się każdego dnia. Śmieci wysypują się na chodnik.',
 'OCZEKUJE', 50, 'SREDNIA', 52.2297, 21.0122, 'Al. Jerozolimskie 54, Warszawa',
 NULL, NULL, '2026-03-03 13:00:00', '2026-03-08 10:00:00', 1),

('ZGL-20260305-000017', @jan, @kLawki,
 'Wyłamane oparcie ławki przy fontannie na Starym Mieście',
 'Ławka nr 3 przy fontannie na Rynku Starego Miasta ma całkowicie wyłamane oparcie.',
 'OCZEKUJE', 56, 'SREDNIA', 52.2513, 21.0023, 'Rynek Starego Miasta, Warszawa',
 NULL, NULL, '2026-03-05 16:00:00', '2026-03-12 09:00:00', 1),

('ZGL-20260307-000018', @jan, @kKanal,
 'Nieszczelna studzienka kanalizacyjna – zapach na ul. Konstruktorskiej',
 'Z nieszczelnej studzienki wydobywa się intensywny zapach siarkowodoru. Mieszkańcy zgłaszają bóle głowy.',
 'OCZEKUJE', 36, 'SREDNIA', 52.1934, 21.0003, 'ul. Konstruktorska 8, Warszawa',
 '2026-04-30 10:00:00', NULL, '2026-03-07 11:30:00', '2026-03-14 10:00:00', 2),

('ZGL-20260310-000019', @jan, @kZielen,
 'Zaniedbany trawnik – trawa ponad 50 cm na os. Ursynów',
 'Trawnik przy blokach na os. Ursynów nie był koszony przez całą jesień i zimę. Trawa ponad 50 cm, obecność kleszczy.',
 'OCZEKUJE', 70, 'NISKA', 52.1451, 21.0301, 'ul. Romera 4, Warszawa',
 NULL, NULL, '2026-03-10 15:00:00', '2026-03-18 08:00:00', 1),

-- ROZWIAZANE ×5
('ZGL-20260201-000020', @jan, @kDrogi,
 'Pęknięty i wystający krawężnik – ul. Mokotowska 55',
 'Krawężnik jest pęknięty w poprzek i sterczy o ok. 5 cm ponad poziom chodnika. Kilka osób się potknęło. Naprawiony 18.02.',
 'ROZWIAZANE', 31, 'WYSOKA', 52.2205, 21.0171, 'ul. Mokotowska 55, Warszawa',
 '2026-02-20 10:00:00', '2026-02-18 14:00:00', '2026-02-01 10:00:00', '2026-02-18 14:00:00', 2),

('ZGL-20260203-000021', @jan, @kOswiet,
 'Przechylony słup oświetleniowy po wypadku drogowym',
 'Słup przy ul. Dolnej 12 uderzony przez samochód, pochylony pod kątem 30°. Wyprostowany i zabezpieczony 14.02.',
 'ROZWIAZANE', 48, 'SREDNIA', 52.2091, 21.0271, 'ul. Dolna 12, Warszawa',
 '2026-02-15 10:00:00', '2026-02-14 16:00:00', '2026-02-03 09:00:00', '2026-02-14 16:00:00', 2),

('ZGL-20260205-000022', @jan, @kPlace,
 'Zacięty mechanizm karuzeli – pl. zabaw ul. Gagarina',
 'Karuzela na placu przy ul. Gagarina 14 całkowicie się zablokowała. Łożysko zardzewiałe. Naprawiona 25.02.',
 'ROZWIAZANE', 40, 'SREDNIA', 52.2003, 21.0483, 'ul. Gagarina 14, Warszawa',
 '2026-02-28 10:00:00', '2026-02-25 11:00:00', '2026-02-05 14:00:00', '2026-02-25 11:00:00', 2),

('ZGL-20260207-000023', @jan, @kOdpad,
 'Gruz budowlany porzucony na pasie zieleni przy ul. Wiślanej',
 'Nieznany sprawca wysypał ok. 3 m³ gruzu na trawnik. Uprzątnięty przez ekipę komunalną 22.02.',
 'ROZWIAZANE', 65, 'NISKA', 52.2480, 21.0360, 'ul. Wiślana 3, Warszawa',
 NULL, '2026-02-22 10:00:00', '2026-02-07 11:00:00', '2026-02-22 10:00:00', 2),

('ZGL-20260210-000024', @jan, @kKanal,
 'Zatkany odpływ deszczowy – zalewanie ul. Górnośląskiej',
 'Kratka odpływu zatkana przez liście i błoto. Po deszczu jezdnia zalana na 15 cm. Problem rozwiązany 23.02.',
 'ROZWIAZANE', 36, 'SREDNIA', 52.2250, 21.0162, 'ul. Górnośląska 23, Warszawa',
 '2026-02-25 10:00:00', '2026-02-23 15:00:00', '2026-02-10 08:00:00', '2026-02-23 15:00:00', 2),

-- ZAMKNIETE ×4
('ZGL-20260101-000025', @jan, @kDrogi,
 'Uszkodzone bariery drogowe po kolizji – ul. Górczewska',
 'Bariery energochłonne staranowane przez pojazd ciężarowy. Elementy leżą na jezdni. Naprawione i odebrane 09.01.',
 'ZAMKNIETE', 22, 'NATYCHMIASTOWA', 52.2450, 20.9620, 'ul. Górczewska 150, Warszawa',
 '2026-01-10 10:00:00', '2026-01-09 17:00:00', '2026-01-01 13:00:00', '2026-01-09 17:00:00', 3),

('ZGL-20260103-000026', @jan, @kOswiet,
 'Rozbita latarnia przy przystanku autobusowym Centrum',
 'Latarnia przy przystanku Centrum rozbita przez wandali. Wymieniona głowica i klosz. Zamknięte 15.01.',
 'ZAMKNIETE', 62, 'NISKA', 52.2312, 21.0098, 'ul. Marszałkowska 1, Warszawa',
 NULL, '2026-01-15 12:00:00', '2026-01-03 19:00:00', '2026-01-15 12:00:00', 3),

('ZGL-20260105-000027', @jan, @kLawki,
 'Wybite szyby w wiacie przystankowej przy Ratuszu',
 'Wiata przy przystanku Ratusz ma wybite wszystkie trzy szyby boczne. Szyby wymienione, zamknięte 18.01.',
 'ZAMKNIETE', 72, 'NISKA', 52.2313, 21.0057, 'ul. Senatorska 20, Warszawa',
 '2026-01-20 10:00:00', '2026-01-18 14:00:00', '2026-01-05 10:00:00', '2026-01-18 14:00:00', 3),

('ZGL-20260110-000028', @jan, @kZielen,
 'Wywrócona lipa po wichurze – ul. Sierakowskiego',
 'Stara lipa (obwód 90 cm) przewróciła się w wichurę, blokując chodnik i jezdnię. Uprzątnięta 12.01.',
 'ZAMKNIETE', 54, 'SREDNIA', 52.2506, 21.0322, 'ul. Sierakowskiego 7, Warszawa',
 '2026-01-12 08:00:00', '2026-01-12 11:00:00', '2026-01-10 06:30:00', '2026-01-12 11:00:00', 3),

-- ODRZUCONE ×2
('ZGL-20260115-000029', @jan, @kInne,
 'Prośba o budowę siłowni plenerowej na os. Ursynów',
 'Mieszkańcy proszą o zamontowanie siłowni plenerowej. Nie jest to usterka – odrzucone jako wniosek inwestycyjny.',
 'ODRZUCONE', 71, 'NISKA', 52.1480, 21.0305, 'ul. KEN 58, Warszawa',
 NULL, NULL, '2026-01-15 15:00:00', '2026-01-17 09:00:00', 1),

('ZGL-20260120-000030', @jan, @kDrogi,
 'Wniosek o instalację progów zwalniających – ul. Spokojna',
 'Mieszkańcy proszą o progi zwalniające. Odrzucone – nie dotyczy usterki, należy skierować do Rady Dzielnicy.',
 'ODRZUCONE', 58, 'NISKA', 52.2560, 21.0020, 'ul. Spokojna 5, Warszawa',
 NULL, NULL, '2026-01-20 12:00:00', '2026-01-22 10:00:00', 1);


INSERT INTO historia_statusow
  (id_zgloszenia, id_uzytkownika, stary_status, nowy_status, komentarz, zmieniono)
VALUES
-- NOWE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260401-000001'), @jan, NULL, 'NOWE', 'Zgłoszenie utworzone', '2026-04-01 08:22:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260402-000002'), @jan, NULL, 'NOWE', 'Zgłoszenie utworzone', '2026-04-02 12:05:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260403-000003'), @jan, NULL, 'NOWE', 'Zgłoszenie utworzone', '2026-04-03 07:14:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260404-000004'), @jan, NULL, 'NOWE', 'Zgłoszenie utworzone', '2026-04-04 09:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260405-000005'), @jan, NULL, 'NOWE', 'Zgłoszenie utworzone', '2026-04-05 10:45:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260408-000006'), @jan, NULL, 'NOWE', 'Zgłoszenie utworzone', '2026-04-08 06:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260410-000007'), @jan, NULL, 'NOWE', 'Zgłoszenie utworzone', '2026-04-10 14:22:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260412-000008'), @jan, NULL, 'NOWE', 'Zgłoszenie utworzone', '2026-04-12 11:00:00'),
-- W_TOKU
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260315-000009'), @jan,   NULL,   'NOWE',   'Zgłoszenie utworzone',          '2026-03-15 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260315-000009'), @admin, 'NOWE', 'W_TOKU', 'Przyjęto do realizacji',        '2026-03-18 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260316-000010'), @jan,   NULL,   'NOWE',   'Zgłoszenie utworzone',          '2026-03-16 07:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260316-000010'), @admin, 'NOWE', 'W_TOKU', 'Technik skierowany na miejsce', '2026-03-17 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260318-000011'), @jan,   NULL,   'NOWE',   'Zgłoszenie utworzone',          '2026-03-18 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260318-000011'), @admin, 'NOWE', 'W_TOKU', 'Przyjęto do realizacji',        '2026-03-20 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260320-000012'), @jan,   NULL,   'NOWE',   'Zgłoszenie utworzone',          '2026-03-20 20:15:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260320-000012'), @admin, 'NOWE', 'W_TOKU', 'Zlecono wymianę żarówek',       '2026-03-22 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260322-000013'), @jan,   NULL,   'NOWE',   'Zgłoszenie utworzone',          '2026-03-22 05:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260322-000013'), @admin, 'NOWE', 'W_TOKU', 'MPWiK poinformowane, ekipa w drodze', '2026-03-22 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260325-000014'), @jan,   NULL,   'NOWE',   'Zgłoszenie utworzone',          '2026-03-25 10:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260325-000014'), @admin, 'NOWE', 'W_TOKU', 'Wydano zlecenie naprawy',       '2026-03-26 09:00:00'),
-- OCZEKUJE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260301-000015'), @jan,   NULL,     'NOWE',     'Zgłoszenie utworzone',                     '2026-03-01 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260301-000015'), @admin, 'NOWE',   'OCZEKUJE', 'Oczekiwanie na ekipę drogową',             '2026-03-10 11:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260303-000016'), @jan,   NULL,     'NOWE',     'Zgłoszenie utworzone',                     '2026-03-03 13:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260303-000016'), @admin, 'NOWE',   'OCZEKUJE', 'Przekazano do ZOM',                        '2026-03-08 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260305-000017'), @jan,   NULL,     'NOWE',     'Zgłoszenie utworzone',                     '2026-03-05 16:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260305-000017'), @admin, 'NOWE',   'OCZEKUJE', 'Zamówiono część zamienną, dostawa 2 tyg.', '2026-03-12 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260307-000018'), @jan,   NULL,     'NOWE',     'Zgłoszenie utworzone',                     '2026-03-07 11:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260307-000018'), @admin, 'NOWE',   'W_TOKU',   'Przyjęto, sprawdzenie na miejscu',         '2026-03-10 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260307-000018'), @admin, 'W_TOKU', 'OCZEKUJE', 'Potrzebny sprzęt inspekcji kanalizacji',   '2026-03-14 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260310-000019'), @jan,   NULL,     'NOWE',     'Zgłoszenie utworzone',                     '2026-03-10 15:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260310-000019'), @admin, 'NOWE',   'OCZEKUJE', 'Sezon koszenia rusza w kwietniu',          '2026-03-18 08:00:00'),
-- ROZWIAZANE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260201-000020'), @jan,   NULL,     'NOWE',       'Zgłoszenie utworzone',                '2026-02-01 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260201-000020'), @admin, 'NOWE',   'W_TOKU',     'Przyjęto, ekipa w drodze',            '2026-02-05 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260201-000020'), @tech1, 'W_TOKU', 'ROZWIAZANE', 'Krawężnik wymieniony i wypoziomowany', '2026-02-18 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260203-000021'), @jan,   NULL,     'NOWE',       'Zgłoszenie utworzone',                '2026-02-03 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260203-000021'), @admin, 'NOWE',   'W_TOKU',     'Zlecono wyprostowanie słupa',         '2026-02-06 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260203-000021'), @tech2, 'W_TOKU', 'ROZWIAZANE', 'Słup wyprostowany, instalacja sprawna', '2026-02-14 16:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260205-000022'), @jan,   NULL,     'NOWE',       'Zgłoszenie utworzone',                '2026-02-05 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260205-000022'), @admin, 'NOWE',   'W_TOKU',     'Technik skierowany na plac zabaw',    '2026-02-10 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260205-000022'), @tech3, 'W_TOKU', 'ROZWIAZANE', 'Łożysko wymienione, karuzela sprawna', '2026-02-25 11:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260207-000023'), @jan,   NULL,     'NOWE',       'Zgłoszenie utworzone',                '2026-02-07 11:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260207-000023'), @admin, 'NOWE',   'W_TOKU',     'Zlecono uprzątnięcie',                '2026-02-10 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260207-000023'), @tech4, 'W_TOKU', 'ROZWIAZANE', 'Gruz wywieziony, teren uprzątnięty',  '2026-02-22 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260210-000024'), @jan,   NULL,     'NOWE',       'Zgłoszenie utworzone',                '2026-02-10 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260210-000024'), @admin, 'NOWE',   'W_TOKU',     'Kontrola odpływu zaplanowana',        '2026-02-14 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260210-000024'), @tech5, 'W_TOKU', 'ROZWIAZANE', 'Kratka oczyszczona, drożność przywrócona', '2026-02-23 15:00:00'),
-- ZAMKNIETE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260101-000025'), @jan,   NULL,        'NOWE',        'Zgłoszenie utworzone',             '2026-01-01 13:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260101-000025'), @admin, 'NOWE',      'W_TOKU',      'Ekipa drogowa na miejscu',         '2026-01-02 07:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260101-000025'), @tech1, 'W_TOKU',    'ROZWIAZANE',  'Bariery wymienione',               '2026-01-09 17:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260101-000025'), @admin, 'ROZWIAZANE','ZAMKNIETE',   'Odebrane przez nadzór inwestorski', '2026-01-10 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260103-000026'), @jan,   NULL,        'NOWE',        'Zgłoszenie utworzone',             '2026-01-03 19:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260103-000026'), @admin, 'NOWE',      'W_TOKU',      'Zlecono wymianę głowicy',          '2026-01-06 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260103-000026'), @tech2, 'W_TOKU',    'ROZWIAZANE',  'Latarnia naprawiona',              '2026-01-15 12:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260103-000026'), @admin, 'ROZWIAZANE','ZAMKNIETE',   'Zamknięte po odbiorze',            '2026-01-15 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260105-000027'), @jan,   NULL,        'NOWE',        'Zgłoszenie utworzone',             '2026-01-05 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260105-000027'), @admin, 'NOWE',      'W_TOKU',      'Zamówiono szyby i uszczelki',      '2026-01-08 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260105-000027'), @tech3, 'W_TOKU',    'ROZWIAZANE',  'Szyby zamontowane, wiata szczelna','2026-01-18 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260105-000027'), @admin, 'ROZWIAZANE','ZAMKNIETE',   'Zamknięto po inspekcji',           '2026-01-18 16:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260110-000028'), @jan,   NULL,        'NOWE',        'Zgłoszenie utworzone',             '2026-01-10 06:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260110-000028'), @admin, 'NOWE',      'W_TOKU',      'Ekipa interwencyjna wysłana',      '2026-01-10 07:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260110-000028'), @tech4, 'W_TOKU',    'ROZWIAZANE',  'Drzewo uprzątnięte, droga wolna',  '2026-01-12 11:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260110-000028'), @admin, 'ROZWIAZANE','ZAMKNIETE',   'Teren posprzątany',                '2026-01-12 13:00:00'),
-- ODRZUCONE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260115-000029'), @jan,   NULL,   'NOWE',     'Zgłoszenie utworzone',                               '2026-01-15 15:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260115-000029'), @admin, 'NOWE', 'ODRZUCONE','Nie jest usterką – skierować do wydziału inwestycji', '2026-01-17 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260120-000030'), @jan,   NULL,   'NOWE',     'Zgłoszenie utworzone',                               '2026-01-20 12:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260120-000030'), @admin, 'NOWE', 'ODRZUCONE','Wniosek inwestycyjny – skierować do Rady Dzielnicy',  '2026-01-22 10:00:00');

INSERT INTO przypisania_technikow
  (id_zgloszenia, id_technika, id_przypisujacego,
   przypisano, planowany_start, planowane_zakonczenie,
   status_przypisania, notatka, wersja)
VALUES
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260315-000009'),
 @tech1, @admin, '2026-03-18 09:00:00', '2026-03-18 10:00:00', '2026-03-18 18:00:00', 'AKTYWNE', 'Naprawa płyt chodnikowych – wymiana 6 szt.', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260316-000010'),
 @tech2, @admin, '2026-03-17 10:00:00', '2026-03-17 11:00:00', '2026-03-17 14:00:00', 'AKTYWNE', 'Diagnoza i naprawa sterownika sygnalizacji', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260318-000011'),
 @tech3, @admin, '2026-03-20 09:00:00', '2026-03-20 10:00:00', '2026-03-20 22:00:00', 'AKTYWNE', 'Czyszczenie studzienki i kratki ściekowej', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260320-000012'),
 @tech4, @admin, '2026-03-22 08:00:00', '2026-03-22 09:00:00', '2026-03-22 13:00:00', 'AKTYWNE', 'Wymiana 3 lamp sodowych na LED', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260322-000013'),
 @tech5, @admin, '2026-03-22 08:00:00', '2026-03-22 08:30:00', '2026-03-23 20:00:00', 'AKTYWNE', 'Awaria magistrali – ekipa MPWiK + wsparcie miejskie', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260325-000014'),
 @tech1, @admin, '2026-03-26 09:00:00', '2026-03-26 10:00:00', '2026-03-26 14:00:00', 'AKTYWNE', 'Wymiana łańcucha huśtawki i przegląd pozostałych', 0);


SELECT CONCAT('Zgłoszeń:  ', COUNT(*)) AS wynik FROM zgloszenia;
SELECT CONCAT('Historii:  ', COUNT(*)) AS wynik FROM historia_statusow;
SELECT CONCAT('Przypisań: ', COUNT(*)) AS wynik FROM przypisania_technikow;


SET @anna    = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'anna.nowak');
SET @krzys   = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'krzysztof.w');
SET @marta   = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'marta.zielinska');
SET @pawel   = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'pawel.dąbrowski');
SET @ewa     = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'ewa.kaminska');
SET @robert  = (SELECT id_uzytkownika FROM uzytkownicy WHERE login = 'robert.lewicki');

INSERT INTO zgloszenia
  (numer_zgloszenia, id_zglaszajacego, id_kategorii,
   tytul, opis,
   status, priorytet_obliczony, pilnosc,
   szerokosc_geo, dlugosc_geo, adres,
   data_zauwazeniausterki, zamknieto,
   utworzono, zaktualizowano, wersja)
VALUES
('ZGL-20260501-000031', @anna, @kDrogi,
 'Duża dziura w jezdni przy dworcu PKP',
 'Na ul. Czarnowskiej tuż przy wjeździe na teren dworca PKP widnieje dziura o średnicy ok. 50 cm i głębokości ~8 cm. Stwarza zagrożenie dla samochodów i rowerzystów, szczególnie nocą.',
 'NOWE', 29, 'WYSOKA', 50.8748, 20.6283, 'ul. Czarnowska 12, Kielce',
 '2026-04-28 07:30:00', NULL, '2026-05-01 08:15:00', '2026-05-01 08:15:00', 0),

('ZGL-20260502-000032', @krzys, @kOswiet,
 'Niedziałające latarnie na os. Barwinek – sektor C',
 'Trzy kolejne latarnie przy blokach C1-C3 nie świecą od ponad tygodnia. Ciemny chodnik stanowi zagrożenie dla pieszych, w okolicy jest szkoła podstawowa.',
 'NOWE', 42, 'SREDNIA', 50.8823, 20.6297, 'os. Barwinek, Kielce',
 '2026-04-25 20:00:00', NULL, '2026-05-02 09:45:00', '2026-05-02 09:45:00', 0),

('ZGL-20260503-000033', @marta, @kDrogi,
 'Uszkodzona nawierzchnia chodnika – ul. Sienkiewicza',
 'Na odcinku ok. 15 metrów przy ul. Sienkiewicza 25 chodnik jest silnie popękany i wybrzuszony – korzenie drzew uniosły płyty chodnikowe. Osoby starsze i matki z wózkami mają problem z przejściem.',
 'NOWE', 35, 'SREDNIA', 50.8706, 20.6321, 'ul. Sienkiewicza 25, Kielce',
 '2026-04-30 11:00:00', NULL, '2026-05-03 10:20:00', '2026-05-03 10:20:00', 0),

('ZGL-20260504-000034', @pawel, @kSyg,
 'Awaria sygnalizacji – skrzyżowanie al. IX Wieków Kielc / ul. Krakowska',
 'Sygnalizacja świetlna na tym ruchliwym skrzyżowaniu świeci na wszystkich kierunkach jednocześnie na zielono. Kierowcy nie wiedzą kto ma pierwszeństwo, doszło już do kilku niebezpiecznych sytuacji.',
 'NOWE', 14, 'NATYCHMIASTOWA', 50.8674, 20.6398, 'al. IX Wieków Kielc / ul. Krakowska, Kielce',
 '2026-05-03 07:50:00', NULL, '2026-05-04 08:00:00', '2026-05-04 08:00:00', 0),

('ZGL-20260505-000035', @ewa, @kOdpad,
 'Dzikie wysypisko przy ul. Tarnowskiej',
 'W zagłębieniu terenu przy ul. Tarnowskiej (za stacją benzynową) ktoś wyrzucił gruz, stare meble i worki ze śmieciami. Wysypisko rozrasta się od ok. 2 tygodni.',
 'NOWE', 52, 'SREDNIA', 50.8745, 20.6180, 'ul. Tarnowska 38, Kielce',
 '2026-04-22 14:00:00', NULL, '2026-05-05 12:30:00', '2026-05-05 12:30:00', 0),

('ZGL-20260506-000036', @robert, @kLawki,
 'Uszkodzona ławka w parku im. T. Kościuszki',
 'Ławka przy alejce głównej (naprzeciwko fontanny) ma złamane oparcie – sterczą metalowe elementy, które mogą skaleczyć użytkowników. Ławka jest intensywnie używana przez seniorów.',
 'NOWE', 68, 'NISKA', 50.8701, 20.6333, 'Park im. T. Kościuszki, Kielce',
 '2026-05-04 10:00:00', NULL, '2026-05-06 09:00:00', '2026-05-06 09:00:00', 0),

('ZGL-20260507-000037', @anna, @kZielen,
 'Połamana gałąź zagraża przechodniom – ul. Żeromskiego',
 'Silne wiatry z ostatniego weekendu złamały grubą gałąź lipy przy ul. Żeromskiego 8. Gałąź zwisa na chodnik i może spaść na przechodniów. Drzewo wymaga pilnej interwencji.',
 'NOWE', 23, 'WYSOKA', 50.8688, 20.6285, 'ul. Żeromskiego 8, Kielce',
 '2026-05-05 09:00:00', NULL, '2026-05-07 07:30:00', '2026-05-07 07:30:00', 0),

('ZGL-20260508-000038', @krzys, @kKanal,
 'Zatkana studzienka kanalizacyjna przy Rynku',
 'Studzienka przy rogu Rynku i ul. Bodzentyńskiej jest całkowicie zatkana – po każdym deszczu woda stoi na chodniku przez kilka godzin. Numer studni: KL-447.',
 'NOWE', 22, 'SREDNIA', 50.8698, 20.6333, 'Rynek / ul. Bodzentyńska, Kielce',
 '2026-05-02 16:00:00', NULL, '2026-05-08 11:00:00', '2026-05-08 11:00:00', 0),

('ZGL-20260509-000039', @marta, @kPlace,
 'Uszkodzona zjeżdżalnia na placu zabaw – os. Na Stoku',
 'Plastikowa rynna zjeżdżalni ma pęknięcie wzdłuż całej długości z ostrymi krawędziami. Dziecko z mojego podwórka skaleczyło się dziś rano. Plac zabaw wymaga natychmiastowej naprawy lub zamknięcia.',
 'NOWE', 24, 'WYSOKA', 50.8834, 20.6451, 'os. Na Stoku, bl. 14, Kielce',
 '2026-05-09 08:30:00', NULL, '2026-05-09 09:00:00', '2026-05-09 09:00:00', 0),

('ZGL-20260510-000040', @pawel, @kWoda,
 'Wyciek wody z rury pod jezdnią – ul. Złota',
 'Na ul. Złotej przy nr 15 woda wypływa spod asfaltu od ok. 3 dni. Mokra plama na jezdni stale rośnie, asfalt zaczął się osiadać. Prawdopodobnie pęknięta rura wodociągowa DN150.',
 'NOWE', 11, 'NATYCHMIASTOWA', 50.8712, 20.6250, 'ul. Złota 15, Kielce',
 '2026-05-07 06:00:00', NULL, '2026-05-10 07:45:00', '2026-05-10 07:45:00', 0),

('ZGL-20260415-000041', @ewa, @kDrogi,
 'Wyrwa w asfalcie – al. IX Wieków Kielc przy Galerii Echo',
 'Koło wjazdu do parkingu Galerii Echo na al. IX Wieków Kielc jest podłużna wyrwa w asfalcie (ok. 2 m × 30 cm). Codziennie przejeżdżają tamtędy setki samochodów, ryzyko uszkodzenia opon.',
 'W_TOKU', 28, 'WYSOKA', 50.8665, 20.6410, 'al. IX Wieków Kielc 3, Kielce',
 '2026-04-10 12:00:00', NULL, '2026-04-15 10:00:00', '2026-04-18 09:00:00', 1),

('ZGL-20260416-000042', @robert, @kOswiet,
 'Przepalone oprawy LED – ul. Żeromskiego odcinek 10-30',
 'Na odcinku numerów 10-30 przy ul. Żeromskiego przepalone są 4 oprawy LED. Ciemny fragment ulicy w centrum – szczególnie niebezpieczny po godz. 22:00.',
 'W_TOKU', 40, 'SREDNIA', 50.8688, 20.6286, 'ul. Żeromskiego 15, Kielce',
 '2026-04-12 19:30:00', NULL, '2026-04-16 08:00:00', '2026-04-19 09:00:00', 1),

('ZGL-20260417-000043', @anna, @kPlace,
 'Uszkodzone ogrodzenie placu zabaw – ul. Paderewskiego',
 'Metalowe ogrodzenie placu zabaw przy ul. Paderewskiego 22 ma wyrwany segment długości ok. 3 m. Dzieci wychodzą na jezdnię, rodzice zaniepokojeni.',
 'W_TOKU', 26, 'WYSOKA', 50.8715, 20.6301, 'ul. Paderewskiego 22, Kielce',
 '2026-04-13 14:00:00', NULL, '2026-04-17 09:30:00', '2026-04-20 10:00:00', 1),

('ZGL-20260418-000044', @krzys, @kSyg,
 'Zepsuta sygnalizacja – skrzyżowanie ul. Krakowska / ul. Sandomierska',
 'Sygnalizator pieszych przy ul. Krakowskiej 48 nie wydaje sygnałów dźwiękowych od ponad 2 tygodni. Problem dla osób niewidomych i słabowidzących.',
 'W_TOKU', 19, 'WYSOKA', 50.8615, 20.6345, 'ul. Krakowska 48, Kielce',
 '2026-04-05 10:00:00', NULL, '2026-04-18 11:00:00', '2026-04-21 09:00:00', 1),

('ZGL-20260419-000045', @marta, @kDrogi,
 'Pęknięty chodnik z ostrymi krawędziami – ul. Radomska',
 'Płyty chodnikowe przy ul. Radomskiej 34 są silnie popękane, krawędzie podniesione do ~4 cm. W ubiegłym tygodniu potknęła się o nie starsza pani i doznała urazu kolana.',
 'W_TOKU', 33, 'WYSOKA', 50.8762, 20.6508, 'ul. Radomska 34, Kielce',
 '2026-04-15 09:00:00', NULL, '2026-04-19 12:00:00', '2026-04-22 10:00:00', 1),

('ZGL-20260401-000046', @pawel, @kDrogi,
 'Podmyte pobocze i osuwający się nasyp – ul. Sandomierska',
 'Po wiosennych roztopach przy ul. Sandomierskiej (odcinek 100-120) pobocze podmokło i nasyp zaczął osuwać się w stronę rowu. Konieczna interwencja drogowa przed sezonem letnim.',
 'OCZEKUJE', 36, 'SREDNIA', 50.8589, 20.6412, 'ul. Sandomierska 108, Kielce',
 '2026-03-28 08:00:00', NULL, '2026-04-01 10:00:00', '2026-04-10 11:00:00', 2),

('ZGL-20260402-000047', @ewa, @kOswiet,
 'Brak oświetlenia całej uliczki – os. Na Stoku, ul. wewnętrzna',
 'Uliczka wewnętrzna łącząca bloki 8-12 na os. Na Stoku jest całkowicie pozbawiona oświetlenia – jeden ze słupów jest przewrócony, drugi uszkodzony. Interwencja wymaga zakupu nowego osprzętu.',
 'OCZEKUJE', 44, 'SREDNIA', 50.8834, 20.6452, 'os. Na Stoku, ul. wewnętrzna, Kielce',
 '2026-03-25 20:00:00', NULL, '2026-04-02 08:00:00', '2026-04-12 10:00:00', 2),

('ZGL-20260403-000048', @robert, @kKanal,
 'Cofająca się kanalizacja w piwnicy – ul. Bodzentyńska',
 'W bloku przy ul. Bodzentyńskiej 15 po każdym większym deszczu cofa się kanalizacja w piwnicach. Zapach jest nieznośny, mieszkańcy zgłaszają straty materialne. Trzeba przeprowadzić inspekcję TV kanału zbiorczego.',
 'OCZEKUJE', 21, 'WYSOKA', 50.8810, 20.6185, 'ul. Bodzentyńska 15, Kielce',
 '2026-03-30 14:00:00', NULL, '2026-04-03 09:00:00', '2026-04-15 08:00:00', 2),

('ZGL-20260404-000049', @anna, @kOdpad,
 'Przepełnione kosze i śmieci na Plantach',
 'Kosze na śmieci przy głównej alei Plant są przepełnione – śmieci leżą na ziemi wokół nich. Problem utrzymuje się w weekendy i poniedziałki. Brak regularnego opróżniania.',
 'OCZEKUJE', 55, 'NISKA', 50.8701, 20.6333, 'Planty – aleja główna, Kielce',
 '2026-04-01 09:00:00', NULL, '2026-04-04 10:00:00', '2026-04-14 09:00:00', 2),

('ZGL-20260301-000050', @krzys, @kDrogi,
 'Uszkodzone bariery energochłonne – ul. Radomska wiadukt',
 'Bariery energochłonne przy wiadukcie na ul. Radomskiej zostały uszkodzone przez pojazd. Segment 6 m jest wygięty i nie spełnia funkcji ochronnej.',
 'ROZWIAZANE', 30, 'WYSOKA', 50.8762, 20.6507, 'ul. Radomska – wiadukt, Kielce',
 '2026-02-28 07:00:00', '2026-03-22 14:00:00', '2026-03-01 08:00:00', '2026-03-22 14:00:00', 2),

('ZGL-20260302-000051', @marta, @kOswiet,
 'Niedziałające oświetlenie parku przy ul. Massalskiego',
 'Cały ciąg lamp (8 szt.) przy ścieżce spacerowej parku przy ul. Massalskiego nie świeci. Usterka spowodowana przepalonym bezpiecznikiem skrzynki zasilającej.',
 'ROZWIAZANE', 43, 'SREDNIA', 50.8720, 20.6350, 'Park – ul. Massalskiego, Kielce',
 '2026-02-26 18:00:00', '2026-03-15 12:00:00', '2026-03-02 09:00:00', '2026-03-15 12:00:00', 2),

('ZGL-20260303-000052', @pawel, @kOdpad,
 'Nagromadzenie odpadów wielkogabarytowych – ul. Chęcińska',
 'Przy ul. Chęcińskiej 22 (przy kontenerach) nagromadzono stare meble, lodówkę i materace. Mieszkańcy nie wystawili ich w wyznaczonym terminie – konieczna interwencja.',
 'ROZWIAZANE', 51, 'SREDNIA', 50.8635, 20.6190, 'ul. Chęcińska 22, Kielce',
 '2026-02-25 10:00:00', '2026-03-18 10:00:00', '2026-03-03 11:00:00', '2026-03-18 10:00:00', 2),

('ZGL-20260304-000053', @ewa, @kDrogi,
 'Głęboka dziura w ul. Bodzentyńskiej przy przystanku',
 'Przy przystanku MPK „Bodzentyńska – szpital" jest wyrwa w jezdni głębokości ok. 10 cm. Autobusy omijają ją pasem przeciwnym – stwarza to zagrożenie w godzinach szczytu.',
 'ROZWIAZANE', 27, 'WYSOKA', 50.8810, 20.6190, 'ul. Bodzentyńska – przystanek szpital, Kielce',
 '2026-02-20 07:00:00', '2026-03-10 15:00:00', '2026-03-04 08:30:00', '2026-03-10 15:00:00', 2),

('ZGL-20260305-000054', @robert, @kInne,
 'Uszkodzona kładka dla pieszych nad rzeką Silnicą',
 'Kładka przy ul. Wesoła 5 ma przegniłe deski pomostu – jedna z desek jest wyłamana, tworząc otwór ok. 20×30 cm. Niebezpieczne dla dzieci i rowerzystów.',
 'ROZWIAZANE', 34, 'WYSOKA', 50.8730, 20.6420, 'ul. Wesoła 5 – kładka nad Silnicą, Kielce',
 '2026-02-22 15:00:00', '2026-03-20 11:00:00', '2026-03-05 09:00:00', '2026-03-20 11:00:00', 2),

('ZGL-20260201-000055', @anna, @kWoda,
 'Awaria wodociągu – ul. Wesoła, zalanie jezdni',
 'Przerwa w rurociągu DN200 przy ul. Wesoła 12. Woda zalała jezdnię na długości 30 m, tworząc jezioro głębokości 10 cm. Awaria trwała 6 godzin, brak wody w 4 blokach.',
 'ZAMKNIETE', 10, 'NATYCHMIASTOWA', 50.8730, 20.6421, 'ul. Wesoła 12, Kielce',
 '2026-01-31 05:30:00', '2026-02-15 09:00:00', '2026-02-01 06:00:00', '2026-02-15 09:00:00', 3),

('ZGL-20260202-000056', @krzys, @kDrogi,
 'Wypadnięte płyty chodnikowe zagrażające pieszym – ul. Jagiellońska',
 'Przy ul. Jagiellońskiej 18 wypadły 4 duże płyty chodnikowe po 50×50 cm, odsłaniając jamę pod spodem. Grozi wpadnięciem, szczególnie nocą.',
 'ZAMKNIETE', 25, 'NATYCHMIASTOWA', 50.8663, 20.6362, 'ul. Jagiellońska 18, Kielce',
 '2026-01-30 18:00:00', '2026-02-20 10:00:00', '2026-02-02 08:00:00', '2026-02-20 10:00:00', 3),

('ZGL-20260203-000057', @marta, @kOswiet,
 'Uszkodzony słup oświetleniowy – ul. 1 Maja',
 'Słup oświetleniowy przy ul. 1 Maja 33 jest silnie pochylony po kolizji z pojazdem. Linia kablowa napięta, grozi zerwaniem. Słup wymaga pilnej wymiany.',
 'ZAMKNIETE', 15, 'NATYCHMIASTOWA', 50.8696, 20.6310, 'ul. 1 Maja 33, Kielce',
 '2026-01-29 07:00:00', '2026-02-12 14:00:00', '2026-02-03 07:30:00', '2026-02-12 14:00:00', 3),

('ZGL-20260204-000058', @pawel, @kLawki,
 'Uszkodzone urządzenia siłowni plenerowej – os. Ślichowice',
 'Na siłowni plenerowej przy os. Ślichowice 45 zepsute są 2 z 6 urządzeń: orbitrek (urwana rączka) i wioślarz (zablokowany mechanizm). Urządzenia wymagają naprawy lub wymiany.',
 'ZAMKNIETE', 65, 'NISKA', 50.8625, 20.6280, 'os. Ślichowice 45, Kielce',
 '2026-01-28 11:00:00', '2026-02-25 11:00:00', '2026-02-04 10:00:00', '2026-02-25 11:00:00', 3),


('ZGL-20260120-000059', @ewa, @kInne,
 'Prośba o dodanie ławek na Plant – niewystarczająca liczba miejsc siedzących',
 'Zgłaszający prosi o ustawienie dodatkowych ławek na Plantach. Nie jest to usterka infrastruktury – wniosek przekierowano do Wydziału Inwestycji UM Kielce.',
 'ODRZUCONE', 72, 'NISKA', 50.8701, 20.6333, 'Planty, Kielce',
 NULL, NULL, '2026-01-20 14:00:00', '2026-01-25 09:00:00', 1),

('ZGL-20260125-000060', @robert, @kInne,
 'Wniosek o budowę ścieżki rowerowej wzdłuż rzeki Silnicy',
 'Mieszkaniec wnioskuje o budowę ścieżki rowerowej wzdłuż rzeki Silnicy od ul. Jagiellońskiej do al. IX Wieków Kielc. Wniosek inwestycyjny – nie dotyczy usterki.',
 'ODRZUCONE', 63, 'NISKA', 50.8688, 20.6355, 'rzeka Silnica, Kielce',
 NULL, NULL, '2026-01-25 10:00:00', '2026-01-28 10:00:00', 1);

INSERT INTO historia_statusow
  (id_zgloszenia, id_uzytkownika, stary_status, nowy_status, komentarz, zmieniono)
VALUES
-- NOWE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260501-000031'), @anna,   NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-01 08:15:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260502-000032'), @krzys,  NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-02 09:45:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260503-000033'), @marta,  NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-03 10:20:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260504-000034'), @pawel,  NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-04 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260505-000035'), @ewa,    NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-05 12:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260506-000036'), @robert, NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-06 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260507-000037'), @anna,   NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-07 07:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260508-000038'), @krzys,  NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-08 11:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260509-000039'), @marta,  NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-09 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260510-000040'), @pawel,  NULL,     'NOWE', 'Zgłoszenie utworzone', '2026-05-10 07:45:00'),
-- W_TOKU
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260415-000041'), @ewa,    NULL,     'NOWE',   'Zgłoszenie utworzone',            '2026-04-15 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260415-000041'), @admin,  'NOWE',   'W_TOKU', 'Przyjęto, ekipa skierowana',       '2026-04-18 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260416-000042'), @robert, NULL,     'NOWE',   'Zgłoszenie utworzone',            '2026-04-16 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260416-000042'), @admin,  'NOWE',   'W_TOKU', 'Zlecono wymianę opraw',            '2026-04-19 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260417-000043'), @anna,   NULL,     'NOWE',   'Zgłoszenie utworzone',            '2026-04-17 09:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260417-000043'), @admin,  'NOWE',   'W_TOKU', 'Zamówiono segment ogrodzenia',    '2026-04-20 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260418-000044'), @krzys,  NULL,     'NOWE',   'Zgłoszenie utworzone',            '2026-04-18 11:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260418-000044'), @admin,  'NOWE',   'W_TOKU', 'Zgłoszono do ENERGA – serwis',    '2026-04-21 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260419-000045'), @marta,  NULL,     'NOWE',   'Zgłoszenie utworzone',            '2026-04-19 12:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260419-000045'), @admin,  'NOWE',   'W_TOKU', 'Brygada drogowa skierowana',      '2026-04-22 10:00:00'),
-- OCZEKUJE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260401-000046'), @pawel,  NULL,     'NOWE',     'Zgłoszenie utworzone',               '2026-04-01 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260401-000046'), @admin,  'NOWE',   'OCZEKUJE', 'Oczekiwanie na opinię geologiczną',  '2026-04-10 11:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260402-000047'), @ewa,    NULL,     'NOWE',     'Zgłoszenie utworzone',               '2026-04-02 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260402-000047'), @admin,  'NOWE',   'OCZEKUJE', 'Zamówiono osprzęt, dostawa 3 tyg.',  '2026-04-12 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260403-000048'), @robert, NULL,     'NOWE',     'Zgłoszenie utworzone',               '2026-04-03 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260403-000048'), @admin,  'NOWE',   'W_TOKU',   'Ekipa na miejscu, wstępna ocena',    '2026-04-08 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260403-000048'), @admin,  'W_TOKU', 'OCZEKUJE', 'Potrzebny robot do inspekcji TV',    '2026-04-15 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260404-000049'), @anna,   NULL,     'NOWE',     'Zgłoszenie utworzone',               '2026-04-04 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260404-000049'), @admin,  'NOWE',   'OCZEKUJE', 'Przekazano do ZGM – harmonogram',    '2026-04-14 09:00:00'),
-- ROZWIAZANE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260301-000050'), @krzys,  NULL,     'NOWE',       'Zgłoszenie utworzone',                  '2026-03-01 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260301-000050'), @admin,  'NOWE',   'W_TOKU',     'Brygada drogowa skierowana',            '2026-03-05 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260301-000050'), @tech1,  'W_TOKU', 'ROZWIAZANE', 'Bariery wymienione, odbiór 22.03',      '2026-03-22 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260302-000051'), @marta,  NULL,     'NOWE',       'Zgłoszenie utworzone',                  '2026-03-02 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260302-000051'), @admin,  'NOWE',   'W_TOKU',     'Elektryk miejski skierowany',           '2026-03-06 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260302-000051'), @tech2,  'W_TOKU', 'ROZWIAZANE', 'Wymieniono bezpiecznik, lampy działają','2026-03-15 12:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260303-000052'), @pawel,  NULL,     'NOWE',       'Zgłoszenie utworzone',                  '2026-03-03 11:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260303-000052'), @admin,  'NOWE',   'W_TOKU',     'Zlecono wywóz ZGO Kielce',              '2026-03-07 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260303-000052'), @tech3,  'W_TOKU', 'ROZWIAZANE', 'Odpady wywiezione, teren posprzątany', '2026-03-18 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260304-000053'), @ewa,    NULL,     'NOWE',       'Zgłoszenie utworzone',                  '2026-03-04 08:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260304-000053'), @admin,  'NOWE',   'W_TOKU',     'Ekipa drogowa skierowana',              '2026-03-06 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260304-000053'), @tech4,  'W_TOKU', 'ROZWIAZANE', 'Wyrwa załatana masą bitumiczną',        '2026-03-10 15:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260305-000054'), @robert, NULL,     'NOWE',       'Zgłoszenie utworzone',                  '2026-03-05 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260305-000054'), @admin,  'NOWE',   'W_TOKU',     'Zlecono ciesielkę',                    '2026-03-10 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260305-000054'), @tech5,  'W_TOKU', 'ROZWIAZANE', 'Deska wymieniona, kładka bezpieczna',  '2026-03-20 11:00:00'),

((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260201-000055'), @anna,   NULL,        'NOWE',        'Zgłoszenie utworzone',            '2026-02-01 06:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260201-000055'), @admin,  'NOWE',      'W_TOKU',      'MPWiK Kielce skierowane na awarię','2026-02-01 07:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260201-000055'), @tech1,  'W_TOKU',    'ROZWIAZANE',  'Rura wymieniona, woda przywrócona','2026-02-12 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260201-000055'), @admin,  'ROZWIAZANE','ZAMKNIETE',   'Odbiór po tygodniu obserwacji',   '2026-02-15 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260202-000056'), @krzys,  NULL,        'NOWE',        'Zgłoszenie utworzone',            '2026-02-02 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260202-000056'), @admin,  'NOWE',      'W_TOKU',      'Brygada drogowa – pilna naprawa', '2026-02-03 07:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260202-000056'), @tech2,  'W_TOKU',    'ROZWIAZANE',  'Płyty ułożone, jama zasypana',    '2026-02-15 16:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260202-000056'), @admin,  'ROZWIAZANE','ZAMKNIETE',   'Odbiór techniczny 20.02',         '2026-02-20 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260203-000057'), @marta,  NULL,        'NOWE',        'Zgłoszenie utworzone',            '2026-02-03 07:30:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260203-000057'), @admin,  'NOWE',      'W_TOKU',      'Pogotowie energetyczne na miejscu','2026-02-03 08:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260203-000057'), @tech3,  'W_TOKU',    'ROZWIAZANE',  'Słup wymieniony, sieć naprawiona', '2026-02-10 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260203-000057'), @admin,  'ROZWIAZANE','ZAMKNIETE',   'Zamknięto po odbiorze 12.02',     '2026-02-12 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260204-000058'), @pawel,  NULL,        'NOWE',        'Zgłoszenie utworzone',            '2026-02-04 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260204-000058'), @admin,  'NOWE',      'W_TOKU',      'Serwis urządzeń zewnętrznych',    '2026-02-08 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260204-000058'), @tech4,  'W_TOKU',    'ROZWIAZANE',  'Naprawiono orbitrek i wioślarz',  '2026-02-22 12:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260204-000058'), @admin,  'ROZWIAZANE','ZAMKNIETE',   'Odbiór po próbach obciążeniowych','2026-02-25 11:00:00'),
-- ODRZUCONE
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260120-000059'), @ewa,    NULL,   'NOWE',     'Zgłoszenie utworzone',                                '2026-01-20 14:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260120-000059'), @admin,  'NOWE', 'ODRZUCONE','Nie jest usterką – wniosek do Wydziału Inwestycji',   '2026-01-25 09:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260125-000060'), @robert, NULL,   'NOWE',     'Zgłoszenie utworzone',                                '2026-01-25 10:00:00'),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260125-000060'), @admin,  'NOWE', 'ODRZUCONE','Wniosek inwestycyjny – przekazano do UM Kielce',      '2026-01-28 10:00:00');

INSERT INTO przypisania_technikow
  (id_zgloszenia, id_technika, id_przypisujacego,
   przypisano, planowany_start, planowane_zakonczenie,
   status_przypisania, notatka, wersja)
VALUES
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260415-000041'),
 @tech1, @admin, '2026-04-18 09:00:00', '2026-04-18 10:00:00', '2026-04-18 18:00:00', 'AKTYWNE', 'Frezowanie i uzupełnienie masy bitumicznej', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260416-000042'),
 @tech2, @admin, '2026-04-19 09:00:00', '2026-04-19 10:00:00', '2026-04-19 14:00:00', 'AKTYWNE', 'Wymiana 4 opraw LED SON-T 150W', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260417-000043'),
 @tech3, @admin, '2026-04-20 10:00:00', '2026-04-20 11:00:00', '2026-04-20 16:00:00', 'AKTYWNE', 'Montaż nowego segmentu ogrodzenia ocynkowanego', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260418-000044'),
 @tech4, @admin, '2026-04-21 09:00:00', '2026-04-21 10:00:00', '2026-04-21 13:00:00', 'AKTYWNE', 'Naprawa sterownika sygnalizacji akustycznej', 0),
((SELECT id_zgloszenia FROM zgloszenia WHERE numer_zgloszenia='ZGL-20260419-000045'),
 @tech5, @admin, '2026-04-22 10:00:00', '2026-04-22 11:00:00', '2026-04-22 19:00:00', 'AKTYWNE', 'Wymiana uszkodzonych płyt chodnikowych 60×60', 0);

SELECT CONCAT('Łącznie zgłoszeń:  ', COUNT(*)) AS wynik FROM zgloszenia;
SELECT CONCAT('Łącznie historii:  ', COUNT(*)) AS wynik FROM historia_statusow;
SELECT CONCAT('Łącznie przypisań: ', COUNT(*)) AS wynik FROM przypisania_technikow;
SELECT CONCAT('Kielce (nr ≥ 31):  ', COUNT(*)) AS wynik
  FROM zgloszenia WHERE adres LIKE '%Kielce%';
