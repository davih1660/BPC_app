package com.bpc.escola.repository;

import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.enums.StatusEmbarcacao;
import com.bpc.escola.domain.enums.TipoEmbarcacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmbarcacaoRepository extends JpaRepository<Embarcacao, Long> {

    Page<Embarcacao> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    List<Embarcacao> findByStatus(StatusEmbarcacao status);

    List<Embarcacao> findByTipo(TipoEmbarcacao tipo);
}
