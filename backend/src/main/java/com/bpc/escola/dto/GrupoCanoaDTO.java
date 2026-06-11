package com.bpc.escola.dto;

import com.bpc.escola.domain.GrupoCanoa;

import java.util.List;

public record GrupoCanoaDTO(
        Long id,
        Long professorId,
        String professorNome,
        Long embarcacaoId,
        String embarcacaoNome,
        boolean confirmado,
        SugestaoCanoaDTO sugestao,
        List<ReservaColetivaDTO> membros
) {
    public static GrupoCanoaDTO from(
            GrupoCanoa g,
            SugestaoCanoaDTO sugestao,
            List<ReservaColetivaDTO> membros
    ) {
        return new GrupoCanoaDTO(
                g.getId(),
                g.getProfessor().getId(),
                g.getProfessor().getNome(),
                g.getEmbarcacao() != null ? g.getEmbarcacao().getId() : null,
                g.getEmbarcacao() != null ? g.getEmbarcacao().getNome() : null,
                Boolean.TRUE.equals(g.getConfirmado()),
                sugestao,
                membros
        );
    }
}
