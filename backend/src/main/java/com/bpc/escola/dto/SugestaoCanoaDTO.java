package com.bpc.escola.dto;

import com.bpc.escola.domain.enums.TipoEmbarcacao;

public record SugestaoCanoaDTO(
        int presentes,
        TipoEmbarcacao tipoSugerido,
        String descricao,
        Integer capacidade,
        Long embarcacaoId,
        String embarcacaoNome,
        boolean disponivel
) {
}
