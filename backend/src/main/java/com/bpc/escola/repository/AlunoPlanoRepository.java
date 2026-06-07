package com.bpc.escola.repository;

import com.bpc.escola.domain.AlunoPlano;
import com.bpc.escola.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoPlanoRepository extends JpaRepository<AlunoPlano, Long> {

    List<AlunoPlano> findByAluno(Usuario aluno);

    Optional<AlunoPlano> findFirstByAlunoAndAtivoTrue(Usuario aluno);
}
