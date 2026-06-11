package com.bpc.escola.dto;

import com.bpc.escola.domain.ListaEspera;
import com.bpc.escola.domain.enums.StatusListaEspera;

import java.time.LocalDate;

public record ListaEsperaDTO(
        Long id,
        Long horarioId,
        String horarioTitulo,
        String horarioInicio,
        String horarioFim,
        Long alunoId,
        String alunoNome,
        LocalDate dataReserva,
        StatusListaEspera status,
        Integer posicao
) {
    public static ListaEsperaDTO from(ListaEspera le, int posicao) {
        return new ListaEsperaDTO(
                le.getId(),
                le.getHorario().getId(),
                le.getHorario().getTitulo(),
                le.getHorario().getHorarioInicio().toString(),
                le.getHorario().getHorarioFim().toString(),
                le.getAluno().getId(),
                le.getAluno().getNome(),
                le.getDataReserva(),
                le.getStatus(),
                posicao
        );
    }
}
