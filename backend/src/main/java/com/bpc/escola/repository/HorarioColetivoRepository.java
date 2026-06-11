package com.bpc.escola.repository;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HorarioColetivoRepository extends JpaRepository<HorarioColetivo, Long> {

    List<HorarioColetivo> findByDiaSemana(DiaSemana diaSemana);

    Optional<HorarioColetivo> findByDiaSemanaAndHorarioInicioAndHorarioFim(
            DiaSemana diaSemana,
            java.time.LocalTime horarioInicio,
            java.time.LocalTime horarioFim
    );
}
