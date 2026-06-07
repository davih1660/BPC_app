package com.bpc.escola.dto;

import com.bpc.escola.domain.OcorrenciaImagem;

public record OcorrenciaImagemDTO(
        Long id,
        String nomeOriginal,
        String contentType,
        Long tamanhoBytes
) {
    public static OcorrenciaImagemDTO from(OcorrenciaImagem imagem) {
        return new OcorrenciaImagemDTO(
                imagem.getId(),
                imagem.getNomeOriginal(),
                imagem.getContentType(),
                imagem.getTamanhoBytes()
        );
    }
}
