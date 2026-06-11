package com.bpc.escola.dto;

import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.enums.OrigemReserva;
import com.bpc.escola.domain.enums.SituacaoAluno;
import com.bpc.escola.domain.enums.StatusReserva;

import java.time.LocalDate;

public record ReservaColetivaDTO(
        Long id,
        Long horarioId,
        String horarioTitulo,
        Long alunoId,
        String alunoNome,
        SituacaoAluno situacaoAluno,
        StatusReserva status,
        OrigemReserva origem,
        LocalDate dataReserva,
        Boolean presente
) {
    public static ReservaColetivaDTO from(ReservaColetiva r) {
        return from(r, null);
    }

    public static ReservaColetivaDTO from(ReservaColetiva r, SituacaoAluno situacao) {
        return new ReservaColetivaDTO(
                r.getId(),
                r.getHorario().getId(),
                r.getHorario().getTitulo(),
                r.getAluno().getId(),
                r.getAluno().getNome(),
                situacao,
                r.getStatus(),
                r.getOrigem(),
                r.getDataReserva(),
                r.getPresente()
        );
    }
}
