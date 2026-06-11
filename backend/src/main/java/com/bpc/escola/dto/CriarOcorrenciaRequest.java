package com.bpc.escola.dto;

public record CriarOcorrenciaRequest(
        Long embarcacaoId,
        Long tipoOcorrenciaId,
        String descricao,
        Long usuarioId
) {
}
