package com.bpc.escola.dto;

import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.enums.TipoPlano;

public record PlanoDTO(
        Long id,
        String nome,
        TipoPlano tipoPlano,
        Integer quantidadeAulasSemana,
        Integer quantidadeRemadas,
        Integer validadeMeses,
        Boolean ilimitado
) {
    public static PlanoDTO from(Plano p) {
        return new PlanoDTO(p.getId(), p.getNome(), p.getTipoPlano(), p.getQuantidadeAulasSemana(),
                p.getQuantidadeRemadas(), p.getValidadeMeses(), p.getIlimitado());
    }
}
