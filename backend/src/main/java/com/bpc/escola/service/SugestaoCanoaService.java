package com.bpc.escola.service;

import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.enums.TipoEmbarcacao;
import com.bpc.escola.dto.SugestaoCanoaDTO;
import com.bpc.escola.repository.EmbarcacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SugestaoCanoaService {

    private final EmbarcacaoRepository embarcacaoRepository;
    private final DisponibilidadeEmbarcacaoService disponibilidadeService;

    public SugestaoCanoaDTO sugerir(int presentes, LocalDate data, LocalTime inicio, LocalTime fim) {
        if (presentes <= 0) {
            return new SugestaoCanoaDTO(0, null, "Nenhum presente", 0, null, null, false);
        }

        TipoEmbarcacao tipo = tipoParaQuantidade(presentes);
        String descricao = descricaoTipo(tipo, capacidadeTipo(tipo));
        Embarcacao escolhida = buscarEmbarcacaoDisponivel(tipo, data, inicio, fim);

        return new SugestaoCanoaDTO(
                presentes,
                tipo,
                descricao,
                capacidadeTipo(tipo),
                escolhida != null ? escolhida.getId() : null,
                escolhida != null ? escolhida.getNome() : null,
                escolhida != null
        );
    }

    private TipoEmbarcacao tipoParaQuantidade(int presentes) {
        if (presentes >= 13) return TipoEmbarcacao.TRIMARA;
        if (presentes >= 7) return TipoEmbarcacao.KATAMARA;
        if (presentes >= 5) return TipoEmbarcacao.OC6;
        if (presentes == 4) return TipoEmbarcacao.OC4;
        if (presentes == 3) return TipoEmbarcacao.OC3;
        if (presentes == 2) return TipoEmbarcacao.OC2;
        return TipoEmbarcacao.OC1;
    }

    private int capacidadeTipo(TipoEmbarcacao tipo) {
        return switch (tipo) {
            case TRIMARA -> 18;
            case KATAMARA -> 12;
            case OC6 -> 6;
            case OC4 -> 4;
            case OC3 -> 3;
            case OC2 -> 2;
            case OC1 -> 1;
        };
    }

    private String descricaoTipo(TipoEmbarcacao tipo, int cap) {
        return switch (tipo) {
            case TRIMARA -> "Trimarã (até " + cap + ")";
            case KATAMARA -> "Katamarã (até " + cap + ")";
            default -> tipo.name() + " (até " + cap + ")";
        };
    }

    private Embarcacao buscarEmbarcacaoDisponivel(
            TipoEmbarcacao tipo, LocalDate data, LocalTime inicio, LocalTime fim
    ) {
        List<Embarcacao> candidatas = embarcacaoRepository.findAll().stream()
                .filter(e -> e.getTipo() == tipo)
                .sorted(Comparator.comparing(Embarcacao::getNome))
                .toList();

        for (Embarcacao e : candidatas) {
            var status = disponibilidadeService.calcularStatusEfetivo(e, data, inicio, fim);
            if (status == com.bpc.escola.domain.enums.StatusEmbarcacao.DISPONIVEL) {
                return e;
            }
        }
        return candidatas.isEmpty() ? null : candidatas.getFirst();
    }
}
