package com.bpc.escola.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservaAulaRequest(
        @NotNull Long aulaId,
        @NotNull Long alunoId,
        @NotNull LocalDate dataReserva
) {
}
