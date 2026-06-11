package com.bpc.escola.repository;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ListaEspera;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusListaEspera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Long> {

    List<ListaEspera> findByAlunoAndStatus(Usuario aluno, StatusListaEspera status);

    List<ListaEspera> findByHorarioAndDataReservaAndStatusOrderByCriadoEmAsc(
            HorarioColetivo horario, LocalDate data, StatusListaEspera status);

    Optional<ListaEspera> findFirstByHorarioAndDataReservaAndStatusOrderByCriadoEmAsc(
            HorarioColetivo horario, LocalDate data, StatusListaEspera status);

    boolean existsByHorarioAndDataReservaAndAlunoAndStatusIn(
            HorarioColetivo horario, LocalDate data, Usuario aluno, List<StatusListaEspera> statuses);

    long countByHorarioAndDataReservaAndStatus(
            HorarioColetivo horario, LocalDate data, StatusListaEspera status);
}
