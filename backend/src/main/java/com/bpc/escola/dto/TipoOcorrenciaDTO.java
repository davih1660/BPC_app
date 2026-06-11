package com.bpc.escola.dto;

import com.bpc.escola.domain.TipoOcorrencia;
import com.bpc.escola.domain.enums.GravidadeOcorrencia;

public record TipoOcorrenciaDTO(
        Long id,
        String nome,
        GravidadeOcorrencia gravidade,
        Boolean ativo,
        Integer ordem
) {
    public static TipoOcorrenciaDTO from(TipoOcorrencia t) {
        return new TipoOcorrenciaDTO(
                t.getId(),
                t.getNome(),
                t.getGravidade(),
                t.getAtivo(),
                t.getOrdem()
        );
    }
}
