package com.bpc.escola.dto;

import java.time.LocalDate;
import java.util.List;

public record OperacaoHorarioSlotDTO(
        HorarioColetivoDTO horario,
        String statusSlot,
        LocalDate data,
        List<ReservaColetivaDTO> inscritos,
        int totalInscritos,
        int capacidade,
        boolean lotada,
        SugestaoCanoaDTO sugestaoPresentes,
        Long sessaoId
) {
}
