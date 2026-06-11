package com.bpc.escola.repository;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.SessaoHorario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SessaoHorarioRepository extends JpaRepository<SessaoHorario, Long> {

    Optional<SessaoHorario> findByHorarioAndData(HorarioColetivo horario, LocalDate data);
}
