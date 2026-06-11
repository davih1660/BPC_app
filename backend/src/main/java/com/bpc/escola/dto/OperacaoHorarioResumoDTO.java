package com.bpc.escola.dto;

public record OperacaoHorarioResumoDTO(
        HorarioColetivoDTO horario,
        int totalInscritos,
        int capacidade,
        boolean lotada
) {
    public static OperacaoHorarioResumoDTO from(OperacaoHorarioSlotDTO slot) {
        return new OperacaoHorarioResumoDTO(
                slot.horario(),
                slot.totalInscritos(),
                slot.capacidade(),
                slot.lotada()
        );
    }
}
