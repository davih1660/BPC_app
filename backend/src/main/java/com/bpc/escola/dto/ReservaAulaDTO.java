package com.bpc.escola.dto;

import com.bpc.escola.domain.ReservaAula;
import com.bpc.escola.domain.enums.SituacaoAluno;
import com.bpc.escola.domain.enums.StatusReserva;

import java.time.LocalDate;

public record ReservaAulaDTO(
        Long id,
        Long aulaId,
        String aulaTitulo,
        Long alunoId,
        String alunoNome,
        SituacaoAluno situacaoAluno,
        StatusReserva status,
        LocalDate dataReserva,
        Boolean presente
) {
    public static ReservaAulaDTO from(ReservaAula r) {
        return from(r, null);
    }

    public static ReservaAulaDTO from(ReservaAula r, SituacaoAluno situacaoAluno) {
        return new ReservaAulaDTO(
                r.getId(),
                r.getAula().getId(),
                r.getAula().getTitulo(),
                r.getAluno().getId(),
                r.getAluno().getNome(),
                situacaoAluno,
                r.getStatus(),
                r.getDataReserva(),
                r.getPresente()
        );
    }
}
