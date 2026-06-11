package com.bpc.escola.seed;

import com.bpc.escola.domain.enums.CategoriaPlano;
import com.bpc.escola.domain.enums.PeriodicidadePlano;
import com.bpc.escola.domain.enums.TipoPlano;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class PlanoCatalogo {

    private PlanoCatalogo() {
    }

    public record Definicao(
            String nome,
            TipoPlano tipoPlano,
            CategoriaPlano categoriaPlano,
            PeriodicidadePlano periodicidade,
            Integer quantidadeAulasSemana,
            Integer quantidadeAulasMes,
            Integer quantidadeRemadas,
            Integer validadeMeses,
            BigDecimal valor,
            boolean ilimitado
    ) {
    }

    public static List<Definicao> todos() {
        List<Definicao> planos = new ArrayList<>();

        planos.addAll(recorrentes("1x Semana", TipoPlano.UMA_AULA_SEMANA, 1,
                "198", "165", "132", "110"));
        planos.addAll(recorrentes("2x Semana", TipoPlano.DUAS_AULAS_SEMANA, 2,
                "308", "275", "242", "209"));
        planos.addAll(recorrentes("3x Semana", TipoPlano.TRES_AULAS_SEMANA, 3,
                "363", "330", "297", "264"));
        planos.addAll(recorrentes("Livre", TipoPlano.ILIMITADO, null,
                "418", "385", "352", "319"));

        planos.add(new Definicao(
                "Remada Avulsa — Adulto", TipoPlano.AVULSO_REMADAS, CategoriaPlano.AVULSO,
                null, null, null, 1, 1, bd("55"), false));
        planos.add(new Definicao(
                "Remada Avulsa — Criança", TipoPlano.AVULSO_REMADAS, CategoriaPlano.AVULSO,
                null, null, null, 1, 1, bd("35"), false));

        int[][] pacotes = {{10, 3, 420}, {20, 4, 770}, {30, 5, 1056}, {40, 6, 1320}, {50, 7, 1540}};
        for (int[] pacote : pacotes) {
            planos.add(new Definicao(
                    "Pacote " + pacote[0] + " Remadas",
                    TipoPlano.AVULSO_REMADAS,
                    CategoriaPlano.AVULSO,
                    null,
                    null,
                    null,
                    pacote[0],
                    pacote[1],
                    bd(String.valueOf(pacote[2])),
                    false));
        }

        planos.add(new Definicao(
                "Wellhub", TipoPlano.WELLHUB, CategoriaPlano.WELLHUB,
                PeriodicidadePlano.MENSAL, null, 8, null, 1, null, false));

        planos.add(new Definicao(
                "SUP", TipoPlano.AVULSO_REMADAS, CategoriaPlano.EQUIPAMENTO,
                null, null, null, 1, null, bd("35"), false));
        planos.add(new Definicao(
                "Caiaque Individual", TipoPlano.AVULSO_REMADAS, CategoriaPlano.EQUIPAMENTO,
                null, null, null, 1, null, bd("35"), false));
        planos.add(new Definicao(
                "Caiaque Duplo", TipoPlano.AVULSO_REMADAS, CategoriaPlano.EQUIPAMENTO,
                null, null, null, 1, null, bd("60"), false));
        planos.add(new Definicao(
                "Pedalinho (20 min)", TipoPlano.AVULSO_REMADAS, CategoriaPlano.EQUIPAMENTO,
                null, null, null, 1, null, bd("50"), false));

        return planos;
    }

    private static List<Definicao> recorrentes(
            String rotulo,
            TipoPlano tipo,
            Integer aulasSemana,
            String mensal,
            String trimestral,
            String semestral,
            String anual
    ) {
        PeriodicidadePlano[] periodicidades = {
                PeriodicidadePlano.MENSAL,
                PeriodicidadePlano.TRIMESTRAL,
                PeriodicidadePlano.SEMESTRAL,
                PeriodicidadePlano.ANUAL
        };
        int[] meses = {1, 3, 6, 12};
        String[] sufixos = {"Mensal", "Trimestral", "Semestral", "Anual"};
        String[] valores = {mensal, trimestral, semestral, anual};
        List<Definicao> lista = new ArrayList<>();
        for (int i = 0; i < periodicidades.length; i++) {
            lista.add(new Definicao(
                    rotulo + " — " + sufixos[i],
                    tipo,
                    CategoriaPlano.RECORRENTE,
                    periodicidades[i],
                    aulasSemana,
                    null,
                    null,
                    meses[i],
                    bd(valores[i]),
                    tipo == TipoPlano.ILIMITADO));
        }
        return lista;
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
