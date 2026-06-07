package com.bpc.escola.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateReservaEmbarcacaoRequest(
        @NotNull Long alunoId,
        @NotNull Long embarcacaoId,
        @NotNull LocalDate data,
        @NotNull LocalTime horarioInicio,
        @NotNull LocalTime horarioFim
) {
}
