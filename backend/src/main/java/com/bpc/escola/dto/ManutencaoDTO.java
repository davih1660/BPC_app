package com.bpc.escola.dto;

import com.bpc.escola.domain.Manutencao;
import com.bpc.escola.domain.enums.StatusManutencao;

import java.time.LocalDate;

public record ManutencaoDTO(
        Long id,
        Long embarcacaoId,
        String embarcacaoNome,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusManutencao status
) {
    public static ManutencaoDTO from(Manutencao m) {
        return new ManutencaoDTO(
                m.getId(),
                m.getEmbarcacao().getId(),
                m.getEmbarcacao().getNome(),
                m.getDescricao(),
                m.getDataInicio(),
                m.getDataFim(),
                m.getStatus()
        );
    }
}
