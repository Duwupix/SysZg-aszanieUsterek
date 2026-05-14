-- ============================================================
--  System zgłaszania usterek w mieście
--  Skrypt inicjalizacji bazy danych
--  Uruchom raz w MySQL Workbench lub wierszu poleceń
-- ============================================================

CREATE DATABASE IF NOT EXISTS infrastruktura
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE infrastruktura;

-- ── 1. Użytkownicy ──────────────────────────────────────────
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

-- ── 2. Kategorie usterek ────────────────────────────────────
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

-- ── 3. Zgłoszenia ────────────────────────────────────────────
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
    termin_realizacji  DATETIME      DEFAULT NULL,
    zamknieto          DATETIME      DEFAULT NULL,
    utworzono          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    zaktualizowano     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    wersja             INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id_zgloszenia),
    UNIQUE KEY uq_numer (numer_zgloszenia),
    KEY idx_status_priorytet (status, priorytet_obliczony),
    KEY idx_termin (termin_realizacji),
    CONSTRAINT fk_zgl_uzytkownik FOREIGN KEY (id_zglaszajacego)
        REFERENCES uzytkownicy (id_uzytkownika),
    CONSTRAINT fk_zgl_kategoria FOREIGN KEY (id_kategorii)
        REFERENCES kategorie_usterek (id_kategorii)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 4. Przypisania techników ─────────────────────────────────
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

-- ── 5. Historia statusów ─────────────────────────────────────
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

-- ============================================================
--  Dane startowe – kategorie usterek
-- ============================================================
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

-- ============================================================
--  Użytkownicy są tworzeni automatycznie przez aplikację
--  przy pierwszym uruchomieniu (DataInitializer.java):
--
--  admin        / admin123   → ADMINISTRATOR
--  technik1..5  / technik123 → TECHNIK
--  jan.kowalski / user123    → ZGLASZAJACY
-- ============================================================
