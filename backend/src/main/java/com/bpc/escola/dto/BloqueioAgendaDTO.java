package com.bpc.escola.dto;

import com.bpc.escola.domain.BloqueioAgenda;
import com.bpc.escola.domain.enums.TipoBloqueioAgenda;

import java.time.LocalDate;

public record BloqueioAgendaDTO(
        Long id,
        LocalDate data,
        Long horarioId,
        String horarioTitulo,
        TipoBloqueioAgenda tipo,
        String motivo
) {
    public static BloqueioAgendaDTO from(BloqueioAgenda b) {
        return new BloqueioAgendaDTO(
                b.getId(),
                b.getData(),
                b.getHorario() != null ? b.getHorario().getId() : null,
                b.getHorario() != null ? b.getHorario().getTitulo() : null,
                b.getTipo(),
                b.getMotivo()
        );
    }
}
