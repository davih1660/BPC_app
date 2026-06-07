package com.bpc.escola.repository;

import com.bpc.escola.domain.OcorrenciaImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcorrenciaImagemRepository extends JpaRepository<OcorrenciaImagem, Long> {

    List<OcorrenciaImagem> findByOcorrenciaIdOrderByCriadoEmAsc(Long ocorrenciaId);

    List<OcorrenciaImagem> findByOcorrencia_IdIn(List<Long> ocorrenciaIds);

    long countByOcorrenciaId(Long ocorrenciaId);
}
