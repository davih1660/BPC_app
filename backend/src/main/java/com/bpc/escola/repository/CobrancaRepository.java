package com.bpc.escola.repository;

import com.bpc.escola.domain.Cobranca;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusCobranca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

    List<Cobranca> findByAlunoOrderByVencimentoDesc(Usuario aluno);

    List<Cobranca> findAllByOrderByVencimentoDesc();

    boolean existsByAlunoAndStatus(Usuario aluno, StatusCobranca status);

    boolean existsByAlunoAndStatusAndVencimentoBefore(Usuario aluno, StatusCobranca status, LocalDate data);

    List<Cobranca> findByStatusAndPagoEmBetween(StatusCobranca status, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);
}
