package com.bpc.escola.util;

import com.bpc.escola.dto.ReservaColetivaDTO;

import java.util.Comparator;
import java.util.List;

public final class OrdemAluno {

    private static final Comparator<ReservaColetivaDTO> POR_NOME = Comparator.comparing(
            ReservaColetivaDTO::alunoNome,
            String.CASE_INSENSITIVE_ORDER);

    private OrdemAluno() {
    }

    public static List<ReservaColetivaDTO> ordenarReservas(List<ReservaColetivaDTO> lista) {
        return lista.stream().sorted(POR_NOME).toList();
    }
}
