package com.bpc.escola.repository;

import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.ReservaEmbarcacao;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaEmbarcacaoRepository extends JpaRepository<ReservaEmbarcacao, Long> {

    @Query("SELECT r FROM ReservaEmbarcacao r WHERE r.embarcacao = :embarcacao AND r.data = :data " +
           "AND r.status = :status AND r.horarioInicio < :fim AND r.horarioFim > :inicio")
    List<ReservaEmbarcacao> findOverlapping(
            @Param("embarcacao") Embarcacao embarcacao,
            @Param("data") LocalDate data,
            @Param("inicio") LocalTime inicio,
            @Param("fim") LocalTime fim,
            @Param("status") StatusReserva status);

    List<ReservaEmbarcacao> findByData(LocalDate data);

    List<ReservaEmbarcacao> findByAluno(Usuario aluno);

    List<ReservaEmbarcacao> findByAlunoAndStatus(Usuario aluno, StatusReserva status);

    List<ReservaEmbarcacao> findByStatus(StatusReserva status);
}
