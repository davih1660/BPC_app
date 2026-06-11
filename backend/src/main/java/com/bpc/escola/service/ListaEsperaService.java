package com.bpc.escola.service;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ListaEspera;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.OrigemReserva;
import com.bpc.escola.domain.enums.StatusListaEspera;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.domain.enums.TipoNotificacao;
import com.bpc.escola.dto.ListaEsperaDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.ListaEsperaRepository;
import com.bpc.escola.repository.ReservaColetivaRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListaEsperaService {

    private final ListaEsperaRepository listaEsperaRepository;
    private final HorarioColetivoService horarioService;
    private final UsuarioRepository usuarioRepository;
    private final ReservaColetivaRepository reservaRepository;
    private final PlanoValidacaoService planoValidacaoService;
    private final CobrancaService cobrancaService;
    private final BloqueioAgendaService bloqueioAgendaService;
    private final NotificacaoService notificacaoService;

    public List<ListaEsperaDTO> listarPorAluno(Long alunoId) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));
        return listaEsperaRepository.findByAlunoAndStatus(aluno, StatusListaEspera.AGUARDANDO).stream()
                .map(le -> {
                    int pos = calcularPosicao(le);
                    return ListaEsperaDTO.from(le, pos);
                })
                .toList();
    }

    @Transactional
    public ListaEsperaDTO entrar(Long horarioId, Long alunoId, LocalDate data) {
        HorarioColetivo horario = horarioService.get(horarioId);
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

        cobrancaService.validarAlunoNaoInadimplente(aluno);
        bloqueioAgendaService.validarNaoBloqueado(data, horario);
        planoValidacaoService.validarReservaColetiva(aluno, data);

        long inscritos = reservaRepository.countByHorarioAndDataReservaAndStatus(horario, data, StatusReserva.CONFIRMADA);
        if (inscritos < horario.getCapacidadeSlot()) {
            throw new BusinessException("Horário ainda tem vagas. Reserve diretamente.", "HORARIO_COM_VAGAS");
        }

        if (reservaRepository.existsByHorarioAndAlunoAndDataReservaAndStatus(horario, aluno, data, StatusReserva.CONFIRMADA)) {
            throw new BusinessException("Você já está inscrito neste horário.", "RESERVA_DUPLICADA");
        }

        if (listaEsperaRepository.existsByHorarioAndDataReservaAndAlunoAndStatusIn(
                horario, data, aluno, List.of(StatusListaEspera.AGUARDANDO))) {
            throw new BusinessException("Você já está na lista de espera.", "LISTA_ESPERA_DUPLICADA");
        }

        ListaEspera le = listaEsperaRepository.save(ListaEspera.builder()
                .horario(horario)
                .aluno(aluno)
                .dataReserva(data)
                .status(StatusListaEspera.AGUARDANDO)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build());

        int pos = calcularPosicao(le);
        return ListaEsperaDTO.from(le, pos);
    }

    @Transactional
    public void sair(Long id, Long alunoId) {
        ListaEspera le = listaEsperaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Entrada não encontrada.", "LISTA_ESPERA_NAO_ENCONTRADA"));
        if (!le.getAluno().getId().equals(alunoId)) {
            throw new BusinessException("Não autorizado.", "NAO_AUTORIZADO");
        }
        le.setStatus(StatusListaEspera.CANCELADO);
        listaEsperaRepository.save(le);
    }

    @Transactional
    public void promoverProximo(HorarioColetivo horario, LocalDate data) {
        long inscritos = reservaRepository.countByHorarioAndDataReservaAndStatus(horario, data, StatusReserva.CONFIRMADA);
        if (inscritos >= horario.getCapacidadeSlot()) {
            return;
        }

        listaEsperaRepository.findFirstByHorarioAndDataReservaAndStatusOrderByCriadoEmAsc(
                horario, data, StatusListaEspera.AGUARDANDO).ifPresent(le -> {
            le.setStatus(StatusListaEspera.PROMOVIDO);
            listaEsperaRepository.save(le);

            ReservaColetiva reserva = ReservaColetiva.builder()
                    .horario(horario)
                    .aluno(le.getAluno())
                    .status(StatusReserva.CONFIRMADA)
                    .origem(OrigemReserva.APP)
                    .dataReserva(data)
                    .presente(false)
                    .criadoEm(RelogioSaoPaulo.dataHora())
                    .build();
            ReservaColetiva salva = reservaRepository.save(reserva);

            notificacaoService.criar(
                    le.getAluno().getId(),
                    TipoNotificacao.LISTA_ESPERA_PROMOVIDO,
                    "Vaga liberada!",
                    "Uma vaga abriu em " + horario.getTitulo() + " (" + data + "). Sua reserva foi confirmada.",
                    "RESERVA",
                    salva.getId());
        });
    }

    private int calcularPosicao(ListaEspera le) {
        List<ListaEspera> fila = listaEsperaRepository.findByHorarioAndDataReservaAndStatusOrderByCriadoEmAsc(
                le.getHorario(), le.getDataReserva(), StatusListaEspera.AGUARDANDO);
        for (int i = 0; i < fila.size(); i++) {
            if (fila.get(i).getId().equals(le.getId())) {
                return i + 1;
            }
        }
        return 0;
    }
}
