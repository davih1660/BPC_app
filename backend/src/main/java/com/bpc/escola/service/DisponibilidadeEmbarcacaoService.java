package com.bpc.escola.service;

import com.bpc.escola.domain.*;
import com.bpc.escola.domain.enums.*;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadeEmbarcacaoService {

    private final EmbarcacaoRepository embarcacaoRepository;
    private final ComposicaoEmbarcacaoRepository composicaoRepository;
    private final ReservaEmbarcacaoRepository reservaEmbarcacaoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final AulaRepository aulaRepository;
    private final ReservaAulaRepository reservaAulaRepository;

    public StatusEmbarcacao calcularStatusEfetivo(Embarcacao embarcacao, LocalDate data, LocalTime inicio, LocalTime fim) {
        if (embarcacao.getStatus() == StatusEmbarcacao.INTERDITADA) {
            return StatusEmbarcacao.INTERDITADA;
        }
        if (estaEmManutencao(embarcacao, data)) {
            return StatusEmbarcacao.MANUTENCAO;
        }
        if (embarcacao.getTipo() == TipoEmbarcacao.TRIMARA) {
            StatusEmbarcacao statusFilhas = statusTrimaraPorFilhas(embarcacao, data);
            if (statusFilhas == StatusEmbarcacao.INTERDITADA || statusFilhas == StatusEmbarcacao.MANUTENCAO) {
                return statusFilhas;
            }
        }
        if (embarcacao.getTipo() == TipoEmbarcacao.OC6) {
            if (oc6EmTrimaraIndisponivel(embarcacao, data, inicio, fim)) {
                return StatusEmbarcacao.RESERVADA;
            }
        }
        if (temReservaNoHorario(embarcacao, data, inicio, fim)) {
            return StatusEmbarcacao.RESERVADA;
        }
        if (emAulaNoHorario(embarcacao, data, inicio, fim)) {
            return StatusEmbarcacao.EM_AULA;
        }
        return StatusEmbarcacao.DISPONIVEL;
    }

    public void validarPodeReservar(Embarcacao embarcacao, LocalDate data, LocalTime inicio, LocalTime fim) {
        StatusEmbarcacao status = calcularStatusEfetivo(embarcacao, data, inicio, fim);
        if (status == StatusEmbarcacao.INTERDITADA) {
            throw new BusinessException("Embarcação interditada não pode ser reservada.", "EMBARCACAO_INTERDITADA");
        }
        if (status == StatusEmbarcacao.MANUTENCAO) {
            throw new BusinessException("Embarcação em manutenção não está disponível.", "EMBARCACAO_MANUTENCAO");
        }
        if (embarcacao.getTipo() == TipoEmbarcacao.OC6 && oc6EmTrimaraIndisponivel(embarcacao, data, inicio, fim)) {
            throw new BusinessException("OC6 em uso por trimarã não pode ser reservada individualmente.", "OC6_TRIMARA_EM_USO");
        }
        if (status == StatusEmbarcacao.RESERVADA || status == StatusEmbarcacao.EM_AULA) {
            throw new BusinessException("Embarcação indisponível neste horário.", "EMBARCACAO_INDISPONIVEL");
        }
    }

    private boolean estaEmManutencao(Embarcacao embarcacao, LocalDate data) {
        if (embarcacao.getStatus() == StatusEmbarcacao.MANUTENCAO) {
            return true;
        }
        return manutencaoRepository.findAtivaNaData(embarcacao, data,
                List.of(StatusManutencao.AGENDADA, StatusManutencao.EM_ANDAMENTO)).isPresent();
    }

    private StatusEmbarcacao statusTrimaraPorFilhas(Embarcacao trimara, LocalDate data) {
        List<ComposicaoEmbarcacao> composicoes = composicaoRepository.findByEmbarcacaoPrincipal(trimara);
        for (ComposicaoEmbarcacao comp : composicoes) {
            Embarcacao filha = comp.getEmbarcacaoFilha();
            if (filha.getStatus() == StatusEmbarcacao.INTERDITADA) {
                return StatusEmbarcacao.INTERDITADA;
            }
            if (estaEmManutencao(filha, data)) {
                return StatusEmbarcacao.MANUTENCAO;
            }
        }
        return StatusEmbarcacao.DISPONIVEL;
    }

    private boolean oc6EmTrimaraIndisponivel(Embarcacao oc6, LocalDate data, LocalTime inicio, LocalTime fim) {
        List<ComposicaoEmbarcacao> composicoes = composicaoRepository.findByEmbarcacaoFilha(oc6);
        for (ComposicaoEmbarcacao comp : composicoes) {
            Embarcacao trimara = comp.getEmbarcacaoPrincipal();
            if (trimara.getTipo() != TipoEmbarcacao.TRIMARA) {
                continue;
            }
            if (temReservaNoHorario(trimara, data, inicio, fim) || emAulaNoHorario(trimara, data, inicio, fim)) {
                return true;
            }
            StatusEmbarcacao statusTrimara = calcularStatusEfetivo(trimara, data, inicio, fim);
            if (statusTrimara == StatusEmbarcacao.RESERVADA || statusTrimara == StatusEmbarcacao.EM_AULA) {
                return true;
            }
        }
        return false;
    }

    private boolean temReservaNoHorario(Embarcacao embarcacao, LocalDate data, LocalTime inicio, LocalTime fim) {
        return !reservaEmbarcacaoRepository.findOverlapping(
                embarcacao, data, inicio, fim, StatusReserva.CONFIRMADA).isEmpty();
    }

    private boolean emAulaNoHorario(Embarcacao embarcacao, LocalDate data, LocalTime inicio, LocalTime fim) {
        DiaSemana dia = DiaSemanaUtil.fromLocalDate(data);
        return aulaRepository.findAll().stream()
                .filter(a -> a.getDiaSemana() == dia)
                .filter(a -> a.getEmbarcacaoPrincipal().getId().equals(embarcacao.getId()))
                .anyMatch(a -> a.getHorarioInicio().isBefore(fim) && a.getHorarioFim().isAfter(inicio)
                        && !reservaAulaRepository.findByAulaAndDataReservaAndStatus(a, data, StatusReserva.CONFIRMADA).isEmpty());
    }

    public List<Embarcacao> listarDisponiveis(LocalDate data, LocalTime inicio, LocalTime fim) {
        List<Embarcacao> disponiveis = new ArrayList<>();
        for (Embarcacao e : embarcacaoRepository.findAll()) {
            StatusEmbarcacao status = calcularStatusEfetivo(e, data, inicio, fim);
            if (status == StatusEmbarcacao.DISPONIVEL) {
                disponiveis.add(e);
            }
        }
        return disponiveis;
    }
}
