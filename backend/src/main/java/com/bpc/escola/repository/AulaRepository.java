package com.bpc.escola.repository;

import com.bpc.escola.domain.Aula;
import com.bpc.escola.domain.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    List<Aula> findByDiaSemana(DiaSemana diaSemana);
}
