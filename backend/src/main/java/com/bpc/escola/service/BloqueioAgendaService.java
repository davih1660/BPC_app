package com.bpc.escola.service;

import com.bpc.escola.domain.BloqueioAgenda;
import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.domain.enums.TipoBloqueioAgenda;
import com.bpc.escola.domain.enums.TipoNotificacao;
import com.bpc.escola.dto.BloqueioAgendaDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.BloqueioAgendaRepository;
import com.bpc.escola.repository.HorarioColetivoRepository;
import com.bpc.escola.repository.ReservaColetivaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BloqueioAgendaService {

    private final BloqueioAgendaRepository bloqueioRepository;
    private final HorarioColetivoRepository horarioRepository;
    private final ReservaColetivaRepository reservaRepository;
    private final NotificacaoService notificacaoService;

    public List<BloqueioAgendaDTO> listar(LocalDate de, LocalDate ate) {
        return bloqueioRepository.findByDataBetweenOrderByDataAsc(de, ate).stream()
                .map(BloqueioAgendaDTO::from).toList();
    }

    public boolean estaBloqueado(LocalDate data, HorarioColetivo horario) {
        if (bloqueioRepository.existsByDataAndHorarioIsNull(data)) {
            return true;
        }
        return bloqueioRepository.existsByDataAndHorario(data, horario);
    }

    public void validarNaoBloqueado(LocalDate data, HorarioColetivo horario) {
        if (estaBloqueado(data, horario)) {
            throw new BusinessException("Horário ou dia bloqueado na agenda.", "AGENDA_BLOQUEADA");
        }
    }

    public List<BloqueioAgendaDTO> listarPorData(LocalDate data) {
        return bloqueioRepository.findByData(data).stream().map(BloqueioAgendaDTO::from).toList();
    }

    @Transactional
    public BloqueioAgendaDTO criar(LocalDate data, Long horarioId, TipoBloqueioAgenda tipo, String motivo, boolean cancelarInscritos) {
        HorarioColetivo horario = horarioId != null
                ? horarioRepository.findById(horarioId)
                .orElseThrow(() -> new BusinessException("Horário não encontrado.", "HORARIO_NAO_ENCONTRADO"))
                : null;

        BloqueioAgenda bloqueio = bloqueioRepository.save(BloqueioAgenda.builder()
                .data(data)
                .horario(horario)
                .tipo(tipo)
                .motivo(motivo)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build());

        if (cancelarInscritos) {
            cancelarReservasAfetadas(data, horario, motivo);
        }

        return BloqueioAgendaDTO.from(bloqueio);
    }

    @Transactional
    public void remover(Long id) {
        bloqueioRepository.deleteById(id);
    }

    private void cancelarReservasAfetadas(LocalDate data, HorarioColetivo horario, String motivo) {
        List<ReservaColetiva> reservas = reservaRepository.findByDataReserva(data).stream()
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA)
                .filter(r -> horario == null || r.getHorario().getId().equals(horario.getId()))
                .toList();

        for (ReservaColetiva r : reservas) {
            r.setStatus(StatusReserva.CANCELADA);
            reservaRepository.save(r);
            notificacaoService.criar(
                    r.getAluno().getId(),
                    TipoNotificacao.AGENDA_BLOQUEADA,
                    "Aula cancelada",
                    "Sua aula de " + data + " foi cancelada: " + motivo,
                    "RESERVA",
                    r.getId());
        }
    }
}
