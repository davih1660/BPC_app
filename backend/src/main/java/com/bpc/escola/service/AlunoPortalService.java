package com.bpc.escola.service;

import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.OrigemReserva;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.ProximaReservaDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.ReservaColetivaRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlunoPortalService {

    private final UsuarioRepository usuarioRepository;
    private final ReservaColetivaRepository reservaRepository;

    public ProximaReservaDTO obterProximaReserva(Long alunoId) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

        List<ReservaColetiva> vigentes = reservaRepository.findByAlunoAndStatus(aluno, StatusReserva.CONFIRMADA).stream()
                .filter(this::aindaVigente)
                .sorted(Comparator
                        .comparing(ReservaColetiva::getDataReserva)
                        .thenComparing(r -> r.getHorario().getHorarioInicio()))
                .toList();

        if (vigentes.isEmpty()) {
            return null;
        }

        ReservaColetiva r = vigentes.get(0);
        boolean podeCancelar = podeCancelar(r);

        return new ProximaReservaDTO(
                r.getId(),
                r.getHorario().getId(),
                r.getHorario().getTitulo(),
                r.getDataReserva(),
                r.getHorario().getHorarioInicio().toString().substring(0, 5),
                r.getHorario().getHorarioFim().toString().substring(0, 5),
                r.getOrigem(),
                podeCancelar
        );
    }

    private boolean aindaVigente(ReservaColetiva r) {
        LocalDate hoje = RelogioSaoPaulo.hoje();
        LocalDate data = r.getDataReserva();
        if (data.isBefore(hoje)) return false;
        if (data.isAfter(hoje)) return true;
        LocalTime agora = RelogioSaoPaulo.hora();
        LocalTime fim = r.getHorario().getHorarioFim();
        return agora.isBefore(fim);
    }

    private boolean podeCancelar(ReservaColetiva r) {
        LocalDateTime inicio = LocalDateTime.of(r.getDataReserva(), r.getHorario().getHorarioInicio());
        return RelogioSaoPaulo.dataHora().isBefore(inicio.minusHours(1));
    }
}
