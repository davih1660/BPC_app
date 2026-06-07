package com.bpc.escola.dto;

import com.bpc.escola.domain.ReservaAula;
import com.bpc.escola.domain.enums.StatusReserva;

import java.time.LocalDate;

public record ReservaAulaDTO(
        Long id,
        Long aulaId,
        String aulaTitulo,
        Long alunoId,
        String alunoNome,
        StatusReserva status,
        LocalDate dataReserva,
        Boolean presente
) {
    public static ReservaAulaDTO from(ReservaAula r) {
        return new ReservaAulaDTO(
                r.getId(),
                r.getAula().getId(),
                r.getAula().getTitulo(),
                r.getAluno().getId(),
                r.getAluno().getNome(),
                r.getStatus(),
                r.getDataReserva(),
                r.getPresente()
        );
    }
}
