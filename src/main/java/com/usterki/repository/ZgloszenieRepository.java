package com.usterki.repository;

import com.usterki.model.Zgloszenie;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZgloszenieRepository extends JpaRepository<Zgloszenie, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT z FROM Zgloszenie z WHERE z.id = :id")
    Optional<Zgloszenie> findByIdForUpdate(@Param("id") Long id);

    List<Zgloszenie> findByStatusNotInOrderByPriorytetObliczonyAscTerminRealizacjiAsc(
            List<Zgloszenie.Status> statusy);

    List<Zgloszenie> findByZglaszajacyId(Long idZglaszajacego);

    @Query("""
        SELECT z FROM Zgloszenie z
        WHERE z.status NOT IN ('ZAMKNIETE','ODRZUCONE')
          AND z.priorytetReczny IS NULL
        """)
    List<Zgloszenie> findOtwarteDoPrezeliczenia();
}
