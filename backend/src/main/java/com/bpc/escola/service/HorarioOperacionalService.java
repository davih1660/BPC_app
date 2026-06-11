package com.bpc.escola.service;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.SessaoHorario;
import com.bpc.escola.domain.enums.SituacaoAluno;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.BlocoHorarioOperacaoDTO;
import com.bpc.escola.dto.HorarioColetivoDTO;
import com.bpc.escola.dto.OperacaoDiaHorarioDTO;
import com.bpc.escola.dto.OperacaoHorarioResumoDTO;
import com.bpc.escola.dto.OperacaoHorarioSlotDTO;
import com.bpc.escola.dto.ProximasAulasOperacionaisDTO;
import com.bpc.escola.dto.ReservaColetivaDTO;
import com.bpc.escola.repository.HorarioColetivoRepository;
import com.bpc.escola.repository.ReservaColetivaRepository;
import com.bpc.escola.repository.SessaoHorarioRepository;
import com.bpc.escola.util.OrdemAluno;
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
public class HorarioOperacionalService {

    public static final String EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String PROXIMA = "PROXIMA";
    public static final String ENCERRADA = "ENCERRADA";
    public static final String AGENDADA = "AGENDADA";

    private final HorarioColetivoRepository horarioRepository;
    private final ReservaColetivaRepository reservaRepository;
    private final SessaoHorarioRepository sessaoRepository;
    private final PlanoService planoService;
    private final SugestaoCanoaService sugestaoCanoaService;
    public ProximasAulasOperacionaisDTO obterProximosBlocos(LocalDate data) {
        LocalTime agora = RelogioSaoPaulo.isHoje(data) ? RelogioSaoPaulo.hora() : null;
        List<OperacaoHorarioSlotDTO> slots = montarSlotsDoDia(data, agora);
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

    public OperacaoHorarioSlotDTO obterOperacaoAtual() {
        LocalDate hoje = RelogioSaoPaulo.hoje();
        LocalTime agora = RelogioSaoPaulo.hora();
        List<OperacaoHorarioSlotDTO> slots = montarSlotsDoDia(hoje, agora);

        OperacaoHorarioSlotDTO emAndamento = slots.stream()
                .filter(s -> EM_ANDAMENTO.equals(s.statusSlot()))
                .findFirst()
                .orElse(null);
        if (emAndamento != null) return emAndamento;

        return slots.stream()
                .filter(s -> PROXIMA.equals(s.statusSlot()))
                .findFirst()
                .orElse(null);
    }

    public OperacaoDiaHorarioDTO obterDia(LocalDate data) {
        LocalTime agora = RelogioSaoPaulo.isHoje(data) ? RelogioSaoPaulo.hora() : null;
        List<OperacaoHorarioSlotDTO> slots = montarSlotsDoDia(data, agora);

        OperacaoHorarioSlotDTO destaque = slots.stream()
                .filter(s -> EM_ANDAMENTO.equals(s.statusSlot()))
                .findFirst()
                .orElse(slots.stream()
                        .filter(s -> PROXIMA.equals(s.statusSlot()))
                        .findFirst()
                        .orElse(null));

        long alunosNoDia = reservaRepository.findByDataReserva(data).stream()
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA)
                .map(r -> r.getAluno().getId())
                .distinct()
                .count();

        return new OperacaoDiaHorarioDTO(destaque, slots, alunosNoDia);
    }

    private List<OperacaoHorarioSlotDTO> montarSlotsDoDia(LocalDate data, LocalTime agora) {
        var dia = DiaSemanaUtil.fromLocalDate(data);
        List<HorarioColetivo> horarios = horarioRepository.findByDiaSemana(dia).stream()
                .sorted(Comparator.comparing(HorarioColetivo::getHorarioInicio))
                .toList();

        List<OperacaoHorarioSlotDTO> resultado = new ArrayList<>();
        boolean proximaDefinida = false;
        for (HorarioColetivo horario : horarios) {
            String status = calcularStatusSlot(horario, agora, proximaDefinida);
            if (PROXIMA.equals(status)) proximaDefinida = true;
            resultado.add(montarSlot(horario, data, status));
        }
        return resultado;
    }

    private String calcularStatusSlot(HorarioColetivo horario, LocalTime agora, boolean proximaDefinida) {
        if (agora == null) return AGENDADA;
        LocalTime inicio = horario.getHorarioInicio();
        LocalTime fim = horario.getHorarioFim();
        if (!agora.isBefore(inicio) && agora.isBefore(fim)) return EM_ANDAMENTO;
        if (agora.isBefore(inicio)) return proximaDefinida ? AGENDADA : PROXIMA;
        return ENCERRADA;
    }

    private OperacaoHorarioSlotDTO montarSlot(HorarioColetivo horario, LocalDate data, String statusSlot) {
        Map<Long, SituacaoAluno> situacoes = planoService.mapaSituacoesAlunos();
        List<ReservaColetiva> reservas = reservaRepository.findByHorarioAndDataReservaAndStatus(
                horario, data, StatusReserva.CONFIRMADA);
        List<ReservaColetivaDTO> inscritos = OrdemAluno.ordenarReservas(reservas.stream()
                .map(r -> ReservaColetivaDTO.from(r, situacoes.getOrDefault(r.getAluno().getId(), SituacaoAluno.SEM_PLANO)))
                .toList());
        int total = inscritos.size();
        int cap = horario.getCapacidadeSlot();
        int presentes = (int) inscritos.stream().filter(i -> Boolean.TRUE.equals(i.presente())).count();

        SessaoHorario sessao = sessaoRepository.findByHorarioAndData(horario, data).orElse(null);

        return new OperacaoHorarioSlotDTO(
                HorarioColetivoDTO.from(horario),
                statusSlot,
                data,
                inscritos,
                total,
                cap,
                total >= cap,
                sugestaoCanoaService.sugerir(presentes, data, horario.getHorarioInicio(), horario.getHorarioFim()),
                sessao != null ? sessao.getId() : null
        );
    }

    private BlocoHorarioOperacaoDTO comStatus(BlocoHorarioOperacaoDTO bloco, String status) {
        return new BlocoHorarioOperacaoDTO(
                bloco.horarioInicio(), bloco.horarioFim(), status, bloco.horarios());
    }

    private List<BlocoHorarioOperacaoDTO> agruparPorHorario(List<OperacaoHorarioSlotDTO> slots, LocalTime agora) {
        Map<String, List<OperacaoHorarioSlotDTO>> grupos = new LinkedHashMap<>();
        for (OperacaoHorarioSlotDTO slot : slots) {
            var horario = slot.horario();
            String chave = horario.horarioInicio() + "-" + horario.horarioFim();
            grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(slot);
        }

        List<BlocoHorarioOperacaoDTO> blocos = new ArrayList<>();
        for (List<OperacaoHorarioSlotDTO> grupo : grupos.values()) {
            var primeira = grupo.getFirst().horario();
            LocalTime inicio = primeira.horarioInicio();
            LocalTime fim = primeira.horarioFim();
            String statusBloco = calcularStatusBloco(inicio, fim, agora);
            List<OperacaoHorarioResumoDTO> resumos = grupo.stream()
                    .map(OperacaoHorarioResumoDTO::from)
                    .toList();
            blocos.add(new BlocoHorarioOperacaoDTO(inicio, fim, statusBloco, resumos));
        }
        return blocos;
    }

    private String calcularStatusBloco(LocalTime inicio, LocalTime fim, LocalTime agora) {
        if (agora == null) return AGENDADA;
        if (!agora.isBefore(inicio) && agora.isBefore(fim)) return EM_ANDAMENTO;
        if (agora.isBefore(inicio)) return PROXIMA;
        return ENCERRADA;
    }
}
