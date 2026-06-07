package com.bpc.escola.dto;

import java.time.LocalDate;
import java.util.List;

public record OperacaoAulaDTO(
        AulaDTO aula,
        String statusSlot,
        LocalDate data,
        List<ReservaAulaDTO> inscritos,
        int totalInscritos,
        int capacidade,
        boolean lotada
) {
}
