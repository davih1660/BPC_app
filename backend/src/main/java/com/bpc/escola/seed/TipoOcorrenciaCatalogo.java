package com.bpc.escola.seed;

import com.bpc.escola.domain.enums.GravidadeOcorrencia;

import java.util.List;

public final class TipoOcorrenciaCatalogo {

    private TipoOcorrenciaCatalogo() {
    }

    public record Item(String nome, GravidadeOcorrencia gravidade, int ordem) {
    }

    public static List<Item> itens() {
        return List.of(
                new Item("Furo no casco", GravidadeOcorrencia.ALTA, 1),
                new Item("Rachadura no casco", GravidadeOcorrencia.ALTA, 2),
                new Item("Vazamento", GravidadeOcorrencia.ALTA, 3),
                new Item("Parafuso ou ferragem solta", GravidadeOcorrencia.MEDIA, 4),
                new Item("Assento danificado", GravidadeOcorrencia.MEDIA, 5),
                new Item("Remo com desgaste", GravidadeOcorrencia.BAIXA, 6),
                new Item("Cordame desgastado", GravidadeOcorrencia.BAIXA, 7),
                new Item("Outro", GravidadeOcorrencia.MEDIA, 99)
        );
    }
}
