package com.bpc.escola.dto;

import com.bpc.escola.domain.enums.OrigemReserva;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservaColetivaRequest(
        @NotNull Long horarioId,
        @NotNull Long alunoId,
        @NotNull LocalDate dataReserva,
        OrigemReserva origem
) {
}
