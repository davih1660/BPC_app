package com.bpc.escola.repository;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.SolicitacaoUsoLivre;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusSolicitacaoUsoLivre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SolicitacaoUsoLivreRepository extends JpaRepository<SolicitacaoUsoLivre, Long> {

    List<SolicitacaoUsoLivre> findByAlunoOrderByCriadoEmDesc(Usuario aluno);

    List<SolicitacaoUsoLivre> findByStatusOrderByCriadoEmAsc(StatusSolicitacaoUsoLivre status);

    List<SolicitacaoUsoLivre> findByStatusInOrderByCriadoEmDesc(List<StatusSolicitacaoUsoLivre> statuses);

    boolean existsByAlunoAndHorarioAndDataAndStatus(Usuario aluno, HorarioColetivo horario, LocalDate data, StatusSolicitacaoUsoLivre status);
}
