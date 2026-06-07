package com.bpc.escola.service;

import com.bpc.escola.domain.Aula;
import com.bpc.escola.domain.ReservaAula;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.AulaDTO;
import com.bpc.escola.dto.BlocoHorarioOperacaoDTO;
import com.bpc.escola.dto.OperacaoAulaDTO;
import com.bpc.escola.dto.OperacaoAulaResumoDTO;
import com.bpc.escola.dto.OperacaoDiaDTO;
import com.bpc.escola.dto.ProximasAulasOperacionaisDTO;
import com.bpc.escola.dto.ReservaAulaDTO;
import com.bpc.escola.repository.AulaRepository;
import com.bpc.escola.repository.ReservaAulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AulaOperacionalService {

    public static final String EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String PROXIMA = "PROXIMA";
    public static final String ENCERRADA = "ENCERRADA";
    public static final String AGENDADA = "AGENDADA";

    private final AulaRepository aulaRepository;
    private final ReservaAulaRepository reservaAulaRepository;

    public OperacaoAulaDTO obterOperacaoAtual() {
        LocalDate hoje = RelogioSaoPaulo.hoje();
        LocalTime agora = RelogioSaoPaulo.hora();
        List<OperacaoAulaDTO> slots = montarSlotsDoDia(hoje, agora);

        OperacaoAulaDTO emAndamento = slots.stream()
                .filter(s -> EM_ANDAMENTO.equals(s.statusSlot()))
                .findFirst()
                .orElse(null);

        if (emAndamento != null) {
            return emAndamento;
        }

        return slots.stream()
                .filter(s -> PROXIMA.equals(s.statusSlot()))
                .findFirst()
                .orElse(null);
    }

    public OperacaoDiaDTO obterDia(LocalDate data) {
        LocalTime agora = RelogioSaoPaulo.isHoje(data) ? RelogioSaoPaulo.hora() : null;
        List<OperacaoAulaDTO> slots = montarSlotsDoDia(data, agora);

        OperacaoAulaDTO destaque = slots.stream()
                .filter(s -> EM_ANDAMENTO.equals(s.statusSlot()))
                .findFirst()
                .orElse(slots.stream()
                        .filter(s -> PROXIMA.equals(s.statusSlot()))
                        .findFirst()
                        .orElse(null));

        long alunosNoDia = reservaAulaRepository.findByDataReserva(data).stream()
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA)
                .map(r -> r.getAluno().getId())
                .distinct()
                .count();

        return new OperacaoDiaDTO(destaque, slots, alunosNoDia);
    }

    public ProximasAulasOperacionaisDTO obterProximosBlocos(LocalDate data) {
        LocalTime agora = RelogioSaoPaulo.isHoje(data) ? RelogioSaoPaulo.hora() : null;
        List<OperacaoAulaDTO> slots = montarSlotsDoDia(data, agora);
        List<BlocoHorarioOperacaoDTO> blocos = agruparPorHorario(slots, agora);

        if (blocos.isEmpty()) {
            return new ProximasAulasOperacionaisDTO(null, null);
        }

        int indiceImediato = -1;
        if (agora != null) {
            for (int i = 0; i < blocos.size(); i++) {
                String status = blocos.get(i).statusBloco();
                if (EM_ANDAMENTO.equals(status) || PROXIMA.equals(status)) {
                    indiceImediato = i;
                    break;
                }
            }
        } else {
            indiceImediato = 0;
        }

        BlocoHorarioOperacaoDTO imediato = indiceImediato >= 0 ? blocos.get(indiceImediato) : null;
        BlocoHorarioOperacaoDTO seguinte = null;
        if (indiceImediato >= 0 && indiceImediato + 1 < blocos.size()) {
            BlocoHorarioOperacaoDTO prox = blocos.get(indiceImediato + 1);
            if (!ENCERRADA.equals(prox.statusBloco())) {
                seguinte = comStatus(prox, AGENDADA);
            }
        }

        if (imediato != null && !EM_ANDAMENTO.equals(imediato.statusBloco())) {
            imediato = comStatus(imediato, PROXIMA);
        }

        return new ProximasAulasOperacionaisDTO(imediato, seguinte);
    }

    private BlocoHorarioOperacaoDTO comStatus(BlocoHorarioOperacaoDTO bloco, String status) {
        return new BlocoHorarioOperacaoDTO(
                bloco.horarioInicio(), bloco.horarioFim(), status, bloco.aulas());
    }

    private List<BlocoHorarioOperacaoDTO> agruparPorHorario(List<OperacaoAulaDTO> slots, LocalTime agora) {
        Map<String, List<OperacaoAulaDTO>> grupos = new LinkedHashMap<>();
        for (OperacaoAulaDTO slot : slots) {
            var aula = slot.aula();
            String chave = aula.horarioInicio() + "-" + aula.horarioFim();
            grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(slot);
        }

        List<BlocoHorarioOperacaoDTO> blocos = new ArrayList<>();
        for (List<OperacaoAulaDTO> grupo : grupos.values()) {
            var primeira = grupo.getFirst().aula();
            LocalTime inicio = primeira.horarioInicio();
            LocalTime fim = primeira.horarioFim();
            String statusBloco = calcularStatusBloco(inicio, fim, agora);
            List<OperacaoAulaResumoDTO> resumos = grupo.stream()
                    .map(OperacaoAulaResumoDTO::from)
                    .toList();
            blocos.add(new BlocoHorarioOperacaoDTO(inicio, fim, statusBloco, resumos));
        }
        return blocos;
    }

    private String calcularStatusBloco(LocalTime inicio, LocalTime fim, LocalTime agora) {
        if (agora == null) {
            return AGENDADA;
        }
        if (!agora.isBefore(inicio) && agora.isBefore(fim)) {
            return EM_ANDAMENTO;
        }
        if (agora.isBefore(inicio)) {
            return PROXIMA;
        }
        return ENCERRADA;
    }

    private List<OperacaoAulaDTO> montarSlotsDoDia(LocalDate data, LocalTime agora) {
        var dia = DiaSemanaUtil.fromLocalDate(data);
        List<Aula> aulas = aulaRepository.findByDiaSemana(dia).stream()
                .sorted(Comparator.comparing(Aula::getHorarioInicio))
                .toList();

        List<OperacaoAulaDTO> resultado = new ArrayList<>();
        boolean proximaDefinida = false;
        for (Aula aula : aulas) {
            String status = calcularStatusSlot(aula, agora, proximaDefinida);
            if (PROXIMA.equals(status)) {
                proximaDefinida = true;
            }
            resultado.add(montarSlot(aula, data, status));
        }
        return resultado;
    }

    private String calcularStatusSlot(Aula aula, LocalTime agora, boolean proximaDefinida) {
        if (agora == null) {
            return AGENDADA;
        }
        LocalTime inicio = aula.getHorarioInicio();
        LocalTime fim = aula.getHorarioFim();
        if (!agora.isBefore(inicio) && agora.isBefore(fim)) {
            return EM_ANDAMENTO;
        }
        if (agora.isBefore(inicio)) {
            return proximaDefinida ? AGENDADA : PROXIMA;
        }
        return ENCERRADA;
    }

    private OperacaoAulaDTO montarSlot(Aula aula, LocalDate data, String statusSlot) {
        List<ReservaAula> reservas = reservaAulaRepository.findByAulaAndDataReservaAndStatus(
                aula, data, StatusReserva.CONFIRMADA);
        List<ReservaAulaDTO> inscritos = reservas.stream().map(ReservaAulaDTO::from).toList();
        int cap = aula.getCapacidadeMaxima();
        int total = inscritos.size();
        return new OperacaoAulaDTO(
                AulaDTO.from(aula),
                statusSlot,
                data,
                inscritos,
                total,
                cap,
                total >= cap
        );
    }
}
