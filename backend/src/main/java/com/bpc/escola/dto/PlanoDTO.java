package com.bpc.escola.dto;

import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.enums.CategoriaPlano;
import com.bpc.escola.domain.enums.PeriodicidadePlano;
import com.bpc.escola.domain.enums.TipoPlano;

import java.math.BigDecimal;

public record PlanoDTO(
        Long id,
        String nome,
        TipoPlano tipoPlano,
        CategoriaPlano categoriaPlano,
        PeriodicidadePlano periodicidade,
        Integer quantidadeAulasSemana,
        Integer quantidadeAulasMes,
        Integer quantidadeRemadas,
        Integer validadeMeses,
        BigDecimal valor,
        Boolean ilimitado
) {
    public static PlanoDTO from(Plano p) {
        return new PlanoDTO(
                p.getId(),
                p.getNome(),
                p.getTipoPlano(),
                categoriaDe(p),
                p.getPeriodicidade(),
                p.getQuantidadeAulasSemana(),
                p.getQuantidadeAulasMes(),
                p.getQuantidadeRemadas(),
                p.getValidadeMeses(),
                p.getValor(),
                p.getIlimitado()
        );
    }

    public static CategoriaPlano categoriaDe(Plano p) {
        if (p.getCategoriaPlano() != null) {
            return p.getCategoriaPlano();
        }
        return switch (p.getTipoPlano()) {
            case AVULSO_REMADAS -> CategoriaPlano.AVULSO;
            case WELLHUB -> CategoriaPlano.WELLHUB;
            default -> CategoriaPlano.RECORRENTE;
        };
    }
}
