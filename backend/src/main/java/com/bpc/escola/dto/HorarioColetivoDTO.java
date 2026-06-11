package com.bpc.escola.dto;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.enums.DiaSemana;

import java.time.LocalTime;

public record HorarioColetivoDTO(
        Long id,
        String titulo,
        DiaSemana diaSemana,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        Integer capacidadeSlot
) {
    public static HorarioColetivoDTO from(HorarioColetivo h) {
        return new HorarioColetivoDTO(
                h.getId(),
                h.getTitulo(),
                h.getDiaSemana(),
                h.getHorarioInicio(),
                h.getHorarioFim(),
                h.getCapacidadeSlot()
        );
    }
}
