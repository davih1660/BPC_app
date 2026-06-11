package com.bpc.escola.service;

import com.bpc.escola.domain.*;
import com.bpc.escola.domain.enums.EstadoSessao;
import com.bpc.escola.domain.enums.SituacaoAluno;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.*;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.*;
import com.bpc.escola.util.OrdemAluno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessaoOperacaoService {

    private final HorarioColetivoService horarioService;
    private final SessaoHorarioRepository sessaoRepository;
    private final ReservaColetivaRepository reservaRepository;
    private final GrupoCanoaRepository grupoRepository;
    private final GrupoCanoaReservaRepository grupoReservaRepository;
    private final EscalacaoProfessorRepository escalacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmbarcacaoRepository embarcacaoRepository;
    private final PlanoService planoService;
    private final SugestaoCanoaService sugestaoCanoaService;
    private final ReservaColetivaService reservaColetivaService;

    @Transactional
    public SessaoOperacaoDTO obter(Long horarioId, LocalDate data) {
        HorarioColetivo horario = horarioService.get(horarioId);
        SessaoHorario sessao = obterOuCriarSessao(horario, data);
        return montarSessaoDto(sessao, horario, data);
    }

    @Transactional
    public SessaoOperacaoDTO iniciarChamada(Long horarioId, LocalDate data) {
        SessaoHorario sessao = obterOuCriarSessao(horarioService.get(horarioId), data);
        sessao.setEstado(EstadoSessao.CHAMADA);
        sessaoRepository.save(sessao);
        return montarSessaoDto(sessao, sessao.getHorario(), data);
    }

    @Transactional
    public SessaoOperacaoDTO atualizarEstado(Long horarioId, LocalDate data, EstadoSessao estado) {
        SessaoHorario sessao = obterOuCriarSessao(horarioService.get(horarioId), data);
        sessao.setEstado(estado);
        sessaoRepository.save(sessao);
        return montarSessaoDto(sessao, sessao.getHorario(), data);
    }

    @Transactional
    public GrupoCanoaDTO criarGrupo(Long horarioId, LocalDate data, Long professorId, List<Long> reservaIds) {
        SessaoHorario sessao = obterOuCriarSessao(horarioService.get(horarioId), data);
        Usuario professor = usuarioRepository.findById(professorId)
                .orElseThrow(() -> new BusinessException("Professor não encontrado.", "PROFESSOR_NAO_ENCONTRADO"));

        GrupoCanoa grupo = GrupoCanoa.builder()
                .sessao(sessao)
                .professor(professor)
                .confirmado(false)
                .build();
        grupo = grupoRepository.save(grupo);

        for (Long reservaId : reservaIds) {
            ReservaColetiva reserva = reservaColetivaService.get(reservaId);
            grupoReservaRepository.save(GrupoCanoaReserva.builder().grupo(grupo).reserva(reserva).build());
        }

        return montarGrupoDto(grupo, data);
    }

    @Transactional
    public GrupoCanoaDTO confirmarGrupo(Long grupoId, Long embarcacaoId) {
        GrupoCanoa grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new BusinessException("Grupo não encontrado.", "GRUPO_NAO_ENCONTRADO"));
        Embarcacao emb = embarcacaoRepository.findById(embarcacaoId)
                .orElseThrow(() -> new BusinessException("Embarcação não encontrada.", "EMBARCACAO_NAO_ENCONTRADA"));
        grupo.setEmbarcacao(emb);
        grupo.setConfirmado(true);
        grupoRepository.save(grupo);
        return montarGrupoDto(grupo, grupo.getSessao().getData());
    }

    @Transactional
    public EscalacaoProfessor escalarProfessor(Long horarioId, LocalDate data, Long professorId) {
        HorarioColetivo horario = horarioService.get(horarioId);
        Usuario professor = usuarioRepository.findById(professorId)
                .orElseThrow(() -> new BusinessException("Professor não encontrado.", "PROFESSOR_NAO_ENCONTRADO"));
        return escalacaoRepository.save(EscalacaoProfessor.builder()
                .horario(horario)
                .data(data)
                .professor(professor)
                .build());
    }

    @Transactional
    public SessaoHorario obterOuCriarSessao(HorarioColetivo horario, LocalDate data) {
        return sessaoRepository.findByHorarioAndData(horario, data)
                .orElseGet(() -> sessaoRepository.save(SessaoHorario.builder()
                        .horario(horario)
                        .data(data)
                        .estado(EstadoSessao.AGUARDANDO)
                        .build()));
    }

    private SessaoOperacaoDTO montarSessaoDto(SessaoHorario sessao, HorarioColetivo horario, LocalDate data) {
        Map<Long, SituacaoAluno> situacoes = planoService.mapaSituacoesAlunos();
        List<ReservaColetiva> reservas = reservaRepository.findByHorarioAndDataReservaAndStatus(
                horario, data, StatusReserva.CONFIRMADA);
        List<ReservaColetivaDTO> reservaDtos = OrdemAluno.ordenarReservas(reservas.stream()
                .map(r -> ReservaColetivaDTO.from(r, situacoes.getOrDefault(r.getAluno().getId(), SituacaoAluno.SEM_PLANO)))
                .toList());
        List<ReservaColetivaDTO> presentes = OrdemAluno.ordenarReservas(
                reservaDtos.stream().filter(r -> Boolean.TRUE.equals(r.presente())).toList());

        List<Long> escalados = escalacaoRepository.findByHorarioAndData(horario, data).stream()
                .map(e -> e.getProfessor().getId()).toList();

        List<GrupoCanoaDTO> grupos = grupoRepository.findBySessao(sessao).stream()
                .map(g -> montarGrupoDto(g, data))
                .toList();

        int total = reservaDtos.size();
        int cap = horario.getCapacidadeSlot();
        int qtdPresentes = presentes.size();

        return new SessaoOperacaoDTO(
                sessao.getId(),
                HorarioColetivoDTO.from(horario),
                data,
                sessao.getEstado(),
                reservaDtos,
                presentes,
                escalados,
                grupos,
                total,
                cap,
                total >= cap,
                sugestaoCanoaService.sugerir(
                        qtdPresentes, data, horario.getHorarioInicio(), horario.getHorarioFim())
        );
    }

    private GrupoCanoaDTO montarGrupoDto(GrupoCanoa grupo, LocalDate data) {
        Map<Long, SituacaoAluno> situacoes = planoService.mapaSituacoesAlunos();
        List<ReservaColetivaDTO> membros = OrdemAluno.ordenarReservas(grupoReservaRepository.findByGrupo(grupo).stream()
                .map(gr -> ReservaColetivaDTO.from(
                        gr.getReserva(),
                        situacoes.getOrDefault(gr.getReserva().getAluno().getId(), SituacaoAluno.SEM_PLANO)))
                .toList());
        int presentesGrupo = (int) membros.stream().filter(m -> Boolean.TRUE.equals(m.presente())).count();
        var horario = grupo.getSessao().getHorario();
        SugestaoCanoaDTO sugestao = sugestaoCanoaService.sugerir(
                presentesGrupo, data, horario.getHorarioInicio(), horario.getHorarioFim());
        return GrupoCanoaDTO.from(grupo, sugestao, membros);
    }
}
