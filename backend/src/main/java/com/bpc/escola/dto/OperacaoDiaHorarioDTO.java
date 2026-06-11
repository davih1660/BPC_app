package com.bpc.escola.dto;

import java.util.List;

public record OperacaoDiaHorarioDTO(
        OperacaoHorarioSlotDTO destaque,
        List<OperacaoHorarioSlotDTO> slotsDoDia,
        long alunosNoDia
) {
}
