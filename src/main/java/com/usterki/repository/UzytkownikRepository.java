package com.usterki.repository;

import com.usterki.model.Uzytkownik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UzytkownikRepository extends JpaRepository<Uzytkownik, Long> {
    Optional<Uzytkownik> findByLogin(String login);
    List<Uzytkownik> findByRola(Uzytkownik.Rola rola);
    List<Uzytkownik> findByRolaAndAktywny(Uzytkownik.Rola rola, boolean aktywny);
}
