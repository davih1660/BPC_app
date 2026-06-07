package com.bpc.escola.dto;

import com.bpc.escola.domain.ReservaEmbarcacao;
import com.bpc.escola.domain.enums.StatusReserva;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaEmbarcacaoDTO(
        Long id,
        Long alunoId,
        String alunoNome,
        Long embarcacaoId,
        String embarcacaoNome,
        LocalDate data,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        StatusReserva status
) {
    public static ReservaEmbarcacaoDTO from(ReservaEmbarcacao r) {
        return new ReservaEmbarcacaoDTO(
                r.getId(),
                r.getAluno().getId(),
                r.getAluno().getNome(),
                r.getEmbarcacao().getId(),
                r.getEmbarcacao().getNome(),
                r.getData(),
                r.getHorarioInicio(),
                r.getHorarioFim(),
                r.getStatus()
        );
    }
}
