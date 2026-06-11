package com.bpc.escola.service;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.OrigemReserva;
import com.bpc.escola.domain.enums.SituacaoAluno;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.domain.enums.TipoNotificacao;
import com.bpc.escola.domain.enums.TipoUsuario;
import com.bpc.escola.dto.CreateReservaColetivaRequest;
import com.bpc.escola.dto.ReservaColetivaDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.ReservaColetivaRepository;
import com.bpc.escola.repository.UsuarioRepository;
import com.bpc.escola.util.OrdemAluno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservaColetivaService {

    private final ReservaColetivaRepository reservaRepository;
    private final HorarioColetivoService horarioService;
    private final UsuarioRepository usuarioRepository;
    private final PlanoValidacaoService planoValidacaoService;
    private final PlanoService planoService;
    private final CobrancaService cobrancaService;
    private final BloqueioAgendaService bloqueioAgendaService;
    private final NotificacaoService notificacaoService;
    private final ListaEsperaService listaEsperaService;

    @Transactional
    public ReservaColetivaDTO criar(CreateReservaColetivaRequest request) {
        HorarioColetivo horario = horarioService.get(request.horarioId());
        Usuario aluno = usuarioRepository.findById(request.alunoId())
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

        validarDiaSemana(horario, request.dataReserva());
        cobrancaService.validarAlunoNaoInadimplente(aluno);
        bloqueioAgendaService.validarNaoBloqueado(request.dataReserva(), horario);
        planoValidacaoService.validarReservaColetiva(aluno, request.dataReserva());
        validarCapacidade(horario, request.dataReserva());
        validarDuplicata(horario, aluno, request.dataReserva());

        OrigemReserva origem = request.origem() != null ? request.origem() : OrigemReserva.MANUAL;

        ReservaColetiva reserva = ReservaColetiva.builder()
                .horario(horario)
                .aluno(aluno)
                .status(StatusReserva.CONFIRMADA)
                .origem(origem)
                .dataReserva(request.dataReserva())
                .presente(false)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build();

        ReservaColetiva salva = reservaRepository.save(reserva);
        notificacaoService.criar(
                aluno.getId(),
                TipoNotificacao.RESERVA_CONFIRMADA,
                "Reserva confirmada",
                horario.getTitulo() + " em " + request.dataReserva() + " — inscrição confirmada.",
                "RESERVA",
                salva.getId());
        return toDto(salva);
    }

    @Transactional
    public ReservaColetivaDTO criarWellhub(Long horarioId, Long alunoId, LocalDate data, String wellhubId) {
        return criar(new CreateReservaColetivaRequest(horarioId, alunoId, data, OrigemReserva.WELLHUB));
    }

    @Transactional
    public void cancelar(Long id) {
        ReservaColetiva reserva = get(id);
        validarCancelamento(reserva);
        reserva.setStatus(StatusReserva.CANCELADA);
        reservaRepository.save(reserva);
        notificacaoService.criar(
                reserva.getAluno().getId(),
                TipoNotificacao.RESERVA_CANCELADA,
                "Reserva cancelada",
                "Sua reserva em " + reserva.getHorario().getTitulo() + " (" + reserva.getDataReserva() + ") foi cancelada.",
                "RESERVA",
                reserva.getId());
        listaEsperaService.promoverProximo(reserva.getHorario(), reserva.getDataReserva());
    }

    @Transactional
    public ReservaColetivaDTO atualizarPresenca(Long id, boolean presente) {
        ReservaColetiva reserva = get(id);
        reserva.setPresente(presente);
        return toDto(reservaRepository.save(reserva));
    }

    public List<ReservaColetivaDTO> listarPorHorario(Long horarioId, LocalDate data) {
        HorarioColetivo horario = horarioService.get(horarioId);
        Map<Long, SituacaoAluno> situacoes = planoService.mapaSituacoesAlunos();
        return OrdemAluno.ordenarReservas(reservaRepository.findByHorarioAndDataReservaAndStatus(horario, data, StatusReserva.CONFIRMADA)
                .stream()
                .map(r -> ReservaColetivaDTO.from(r, situacoes.getOrDefault(r.getAluno().getId(), SituacaoAluno.SEM_PLANO)))
                .toList());
    }

    public List<ReservaColetivaDTO> listarPorAluno(Long alunoId) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));
        Map<Long, SituacaoAluno> situacoes = planoService.mapaSituacoesAlunos();
        return reservaRepository.findByAlunoAndStatus(aluno, StatusReserva.CONFIRMADA).stream()
                .map(r -> ReservaColetivaDTO.from(r, situacoes.get(r.getAluno().getId())))
                .toList();
    }

    public ReservaColetiva get(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reserva não encontrada.", "RESERVA_NAO_ENCONTRADA"));
    }

    private ReservaColetivaDTO toDto(ReservaColetiva r) {
        Map<Long, SituacaoAluno> situacoes = planoService.mapaSituacoesAlunos();
        return ReservaColetivaDTO.from(r, situacoes.getOrDefault(r.getAluno().getId(), SituacaoAluno.SEM_PLANO));
    }

    private void validarDiaSemana(HorarioColetivo horario, LocalDate data) {
        if (horario.getDiaSemana() != DiaSemanaUtil.fromLocalDate(data)) {
            throw new BusinessException("Horário não corresponde ao dia da semana.", "HORARIO_DIA_INVALIDO");
        }
    }

    private void validarCapacidade(HorarioColetivo horario, LocalDate data) {
        long inscritos = reservaRepository.findByHorarioAndDataReservaAndStatus(
                horario, data, StatusReserva.CONFIRMADA).size();
        if (inscritos >= horario.getCapacidadeSlot()) {
            throw new BusinessException("Horário lotado.", "HORARIO_LOTADO");
        }
    }

    private void validarDuplicata(HorarioColetivo horario, Usuario aluno, LocalDate data) {
        boolean existe = reservaRepository.findByHorarioAndDataReservaAndStatus(horario, data, StatusReserva.CONFIRMADA)
                .stream().anyMatch(r -> r.getAluno().getId().equals(aluno.getId()));
        if (existe) {
            throw new BusinessException("Aluno já possui reserva neste horário.", "RESERVA_DUPLICADA");
        }
    }

    private void validarCancelamento(ReservaColetiva reserva) {
        LocalDateTime inicio = LocalDateTime.of(
                reserva.getDataReserva(),
                reserva.getHorario().getHorarioInicio());
        if (RelogioSaoPaulo.dataHora().isAfter(inicio.minusHours(1))) {
            throw new BusinessException("Cancelamento permitido apenas até 1h antes.", "CANCELAMENTO_TARDE");
        }
    }

    public void validarAlunoPodeReservarEmbarcacao(Usuario aluno) {
        var situacao = planoService.mapaSituacoesAlunos().get(aluno.getId());
        if (situacao == SituacaoAluno.WELLHUB) {
            throw new BusinessException("Alunos Wellhub não podem reservar embarcações avulsas.", "WELLHUB_SEM_EMBARCACAO");
        }
        if (aluno.getTipoUsuario() != TipoUsuario.ALUNO) {
            throw new BusinessException("Apenas alunos podem reservar.", "NAO_ALUNO");
        }
    }
}
