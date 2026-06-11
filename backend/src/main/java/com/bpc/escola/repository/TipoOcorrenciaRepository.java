package com.bpc.escola.repository;

import com.bpc.escola.domain.TipoOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoOcorrenciaRepository extends JpaRepository<TipoOcorrencia, Long> {

    Optional<TipoOcorrencia> findByNome(String nome);

    List<TipoOcorrencia> findByAtivoTrueOrderByOrdemAscNomeAsc();

    List<TipoOcorrencia> findAllByOrderByOrdemAscNomeAsc();
}
