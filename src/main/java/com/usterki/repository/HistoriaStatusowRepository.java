package com.usterki.repository;

import com.usterki.model.HistoriaStatusow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriaStatusowRepository extends JpaRepository<HistoriaStatusow, Long> {
    List<HistoriaStatusow> findByZgloszenieIdOrderByZmienionoAsc(Long idZgloszenia);
}
