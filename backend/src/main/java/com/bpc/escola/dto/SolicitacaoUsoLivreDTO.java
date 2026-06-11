package com.bpc.escola.dto;

import com.bpc.escola.domain.SolicitacaoUsoLivre;
import com.bpc.escola.domain.enums.StatusSolicitacaoUsoLivre;
import com.bpc.escola.domain.enums.TipoEmbarcacao;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitacaoUsoLivreDTO(
        Long id,
        Long alunoId,
        String alunoNome,
        Long horarioId,
        String horarioTitulo,
        String horarioInicio,
        String horarioFim,
        LocalDate data,
        TipoEmbarcacao tipoCanoaDesejada,
        String observacao,
        StatusSolicitacaoUsoLivre status,
        Long embarcacaoId,
        String embarcacaoNome,
        Long reservaEmbarcacaoId,
        String motivoRecusa,
        String processadoPorNome,
        LocalDateTime criadoEm,
        LocalDateTime processadoEm
) {
    public static SolicitacaoUsoLivreDTO from(SolicitacaoUsoLivre s) {
        return new SolicitacaoUsoLivreDTO(
                s.getId(),
                s.getAluno().getId(),
                s.getAluno().getNome(),
                s.getHorario().getId(),
                s.getHorario().getTitulo(),
                s.getHorario().getHorarioInicio().toString().substring(0, 5),
                s.getHorario().getHorarioFim().toString().substring(0, 5),
                s.getData(),
                s.getTipoCanoaDesejada(),
                s.getObservacao(),
                s.getStatus(),
                s.getEmbarcacaoAtribuida() != null ? s.getEmbarcacaoAtribuida().getId() : null,
                s.getEmbarcacaoAtribuida() != null ? s.getEmbarcacaoAtribuida().getNome() : null,
                s.getReservaEmbarcacao() != null ? s.getReservaEmbarcacao().getId() : null,
                s.getMotivoRecusa(),
                s.getProcessadoPor() != null ? s.getProcessadoPor().getNome() : null,
                s.getCriadoEm(),
                s.getProcessadoEm()
        );
    }
}
