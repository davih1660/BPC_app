package com.bpc.escola.dto;

import com.bpc.escola.domain.Ocorrencia;
import com.bpc.escola.domain.enums.GravidadeOcorrencia;
import com.bpc.escola.domain.enums.StatusOcorrencia;

import java.time.LocalDateTime;
import java.util.List;

public record OcorrenciaDTO(
        Long id,
        Long embarcacaoId,
        String embarcacaoNome,
        Long usuarioId,
        String usuarioNome,
        Long tipoOcorrenciaId,
        String titulo,
        String descricao,
        GravidadeOcorrencia gravidade,
        StatusOcorrencia status,
        LocalDateTime dataAbertura,
        List<OcorrenciaImagemDTO> imagens
) {
    public static OcorrenciaDTO from(Ocorrencia o) {
        return from(o, List.of());
    }

    public static OcorrenciaDTO from(Ocorrencia o, List<OcorrenciaImagemDTO> imagens) {
        return new OcorrenciaDTO(
                o.getId(),
                o.getEmbarcacao().getId(),
                o.getEmbarcacao().getNome(),
                o.getUsuario().getId(),
                o.getUsuario().getNome(),
                o.getTipoOcorrencia() != null ? o.getTipoOcorrencia().getId() : null,
                o.getTitulo(),
                o.getDescricao(),
                o.getGravidade(),
                o.getStatus(),
                o.getDataAbertura(),
                imagens
        );
    }
}
