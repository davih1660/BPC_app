package com.bpc.escola.dto;

import com.bpc.escola.domain.enums.OrigemReserva;

import java.time.LocalDate;

public record ProximaReservaDTO(
        Long reservaId,
        Long horarioId,
        String horarioTitulo,
        LocalDate dataReserva,
        String horarioInicio,
        String horarioFim,
        OrigemReserva origem,
        boolean podeCancelar
) {}
