package com.usterki.repository;

import com.usterki.model.PrzypisanieTechnika;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrzypisanieTechnikaRepository extends JpaRepository<PrzypisanieTechnika, Long> {

    boolean existsByZgloszenieIdAndStatusPrzypisania(
            Long idZgloszenia, PrzypisanieTechnika.StatusPrzypisania status);

    List<PrzypisanieTechnika> findByTechnikIdAndStatusPrzypisania(
            Long idTechnika, PrzypisanieTechnika.StatusPrzypisania status);

    Optional<PrzypisanieTechnika> findByZgloszenieIdAndStatusPrzypisania(
            Long idZgloszenia, PrzypisanieTechnika.StatusPrzypisania status);
}
