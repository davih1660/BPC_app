package com.bpc.escola.repository;

import com.bpc.escola.domain.Ocorrencia;
import com.bpc.escola.domain.enums.StatusOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    List<Ocorrencia> findByStatus(StatusOcorrencia status);
}
