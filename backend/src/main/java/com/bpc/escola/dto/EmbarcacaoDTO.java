package com.bpc.escola.dto;

import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.enums.StatusEmbarcacao;
import com.bpc.escola.domain.enums.TipoEmbarcacao;

public record EmbarcacaoDTO(
        Long id,
        String nome,
        TipoEmbarcacao tipo,
        Integer capacidade,
        StatusEmbarcacao status,
        StatusEmbarcacao statusEfetivo,
        String observacoes
) {
    public static EmbarcacaoDTO from(Embarcacao e, StatusEmbarcacao efetivo) {
        return new EmbarcacaoDTO(e.getId(), e.getNome(), e.getTipo(), e.getCapacidade(),
                e.getStatus(), efetivo, e.getObservacoes());
    }
}
