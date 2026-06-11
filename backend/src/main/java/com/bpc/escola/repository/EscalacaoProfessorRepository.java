package com.bpc.escola.repository;

import com.bpc.escola.domain.EscalacaoProfessor;
import com.bpc.escola.domain.HorarioColetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EscalacaoProfessorRepository extends JpaRepository<EscalacaoProfessor, Long> {

    List<EscalacaoProfessor> findByHorarioAndData(HorarioColetivo horario, LocalDate data);
}
