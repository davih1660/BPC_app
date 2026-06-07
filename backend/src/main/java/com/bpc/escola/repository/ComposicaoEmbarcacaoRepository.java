package com.bpc.escola.repository;

import com.bpc.escola.domain.ComposicaoEmbarcacao;
import com.bpc.escola.domain.Embarcacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComposicaoEmbarcacaoRepository extends JpaRepository<ComposicaoEmbarcacao, Long> {

    List<ComposicaoEmbarcacao> findByEmbarcacaoPrincipal(Embarcacao principal);

    List<ComposicaoEmbarcacao> findByEmbarcacaoFilha(Embarcacao filha);

    Optional<ComposicaoEmbarcacao> findByEmbarcacaoFilhaAndEmbarcacaoPrincipal(
            Embarcacao filha, Embarcacao principal);
}
