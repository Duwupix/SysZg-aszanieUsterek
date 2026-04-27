package com.usterki.repository;

import com.usterki.model.KategoriaUsterki;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KategoriaUsterkiRepository extends JpaRepository<KategoriaUsterki, Long> {
    List<KategoriaUsterki> findByAktywnaTrue();
    List<KategoriaUsterki> findByKategoriaUsterkiIsNull();
}
