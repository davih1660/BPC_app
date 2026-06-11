package com.bpc.escola.repository;

import com.bpc.escola.domain.BloqueioAgenda;
import com.bpc.escola.domain.HorarioColetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {

    List<BloqueioAgenda> findByDataBetweenOrderByDataAsc(LocalDate de, LocalDate ate);

    boolean existsByDataAndHorarioIsNull(LocalDate data);

    boolean existsByDataAndHorario(LocalDate data, HorarioColetivo horario);

    List<BloqueioAgenda> findByData(LocalDate data);
}
