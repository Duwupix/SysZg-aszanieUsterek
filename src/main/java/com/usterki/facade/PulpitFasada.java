package com.usterki.facade;

import com.usterki.dto.ZgloszenieDto;
import com.usterki.service.ZgloszenieService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wzorzec projektowy: FASADA (Facade) — grupa strukturalna.
 *
 * Problem: pulpit (dashboard) potrzebuje danych pochodzących z kilku operacji
 * podsystemu zgłoszeń — statystyk oraz najpilniejszej części kolejki. Frontend
 * musiał wykonywać wiele osobnych wywołań i samodzielnie je składać, co wiązało
 * go ze szczegółami działania backendu.
 *
 * Rozwiązanie: fasada udostępnia jeden uproszczony punkt wejścia
 * ({@link #pobierzDanePulpitu()}), który ukrywa złożoność i kolejność wywołań
 * podsystemu, zwracając gotowy komplet danych pulpitu.
 */
@Component
public class PulpitFasada {

    /** Ile najpilniejszych zgłoszeń pokazać na pulpicie. */
    static final int LIMIT_KOLEJKI = 10;

    private final ZgloszenieService zgloszenieService;

    public PulpitFasada(ZgloszenieService zgloszenieService) {
        this.zgloszenieService = zgloszenieService;
    }

    /** Zwraca komplet danych pulpitu (statystyki + najpilniejsze zgłoszenia) w jednym wywołaniu. */
    public Map<String, Object> pobierzDanePulpitu() {
        Map<String, Object> dane = new LinkedHashMap<>();
        dane.put("statystyki", zgloszenieService.pobierzStatystyki());
        dane.put("najpilniejsze", najpilniejsze());
        return dane;
    }

    private List<ZgloszenieDto.Odpowiedz> najpilniejsze() {
        return zgloszenieService.pobierzKolejke().stream()
                .limit(LIMIT_KOLEJKI)
                .map(ZgloszenieDto.Odpowiedz::z)
                .toList();
    }
}
