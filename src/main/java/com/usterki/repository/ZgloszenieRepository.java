package com.usterki.repository;

import com.usterki.model.Zgloszenie;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ZgloszenieRepository
        extends JpaRepository<Zgloszenie, Long>, JpaSpecificationExecutor<Zgloszenie> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT z FROM Zgloszenie z WHERE z.id = :id")
    Optional<Zgloszenie> findByIdForUpdate(@Param("id") Long id);

    List<Zgloszenie> findByStatusNotInOrderByPriorytetObliczonyAscDataZauwazeniaUsterkiAsc(
            List<Zgloszenie.Status> statusy);

    List<Zgloszenie> findByZglaszajacyId(Long idZglaszajacego);

    @Query("""
        SELECT z FROM Zgloszenie z
        WHERE z.status NOT IN ('ZAMKNIETE','ODRZUCONE')
          AND z.priorytetReczny IS NULL
        """)
    List<Zgloszenie> findOtwarteDoPrezeliczenia();

    // ── Statystyki ──────────────────────────────────────────────

    /** Liczba zgłoszeń w każdym statusie: [Status, Long] */
    @Query("SELECT z.status, COUNT(z) FROM Zgloszenie z GROUP BY z.status")
    List<Object[]> groupByStatus();

    /** Liczba zgłoszeń wg kategorii malejąco: [String nazwa, Long] */
    @Query("SELECT z.kategoria.nazwa, COUNT(z) FROM Zgloszenie z GROUP BY z.kategoria.nazwa ORDER BY COUNT(z) DESC")
    List<Object[]> groupByKategoria();

    /** Średni czas realizacji w godzinach (MySQL native) */
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, utworzono, zamknieto)) FROM zgloszenia WHERE zamknieto IS NOT NULL",
           nativeQuery = true)
    Double avgCzasRealizacjiGodz();

    /** Liczba zgłoszeń utworzonych od podanej daty */
    @Query("SELECT COUNT(z) FROM Zgloszenie z WHERE z.utworzono >= :od")
    Long countOd(@Param("od") LocalDateTime od);
}
