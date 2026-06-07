package com.bpc.escola.service;

import com.bpc.escola.domain.Aula;
import com.bpc.escola.domain.ReservaAula;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.CreateReservaAulaRequest;
import com.bpc.escola.dto.ReservaAulaDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.AulaRepository;
import com.bpc.escola.repository.ReservaAulaRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservaAulaService {

    private final ReservaAulaRepository reservaAulaRepository;
    private final AulaRepository aulaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanoValidacaoService planoValidacaoService;

    @Transactional
    public ReservaAulaDTO criar(CreateReservaAulaRequest request) {
        Aula aula = aulaRepository.findById(request.aulaId())
                .orElseThrow(() -> new BusinessException("Aula não encontrada.", "AULA_NAO_ENCONTRADA"));
        Usuario aluno = usuarioRepository.findById(request.alunoId())
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

        validarDiaSemana(aula, request.dataReserva());
        planoValidacaoService.validarReservaAula(aluno, request.dataReserva());
        validarCapacidade(aula, request.dataReserva());
        validarDuplicata(aula, aluno, request.dataReserva());

        ReservaAula reserva = ReservaAula.builder()
                .aula(aula)
                .aluno(aluno)
                .status(StatusReserva.CONFIRMADA)
                .dataReserva(request.dataReserva())
                .presente(false)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build();

        return ReservaAulaDTO.from(reservaAulaRepository.save(reserva));
    }

    @Transactional
    public void cancelar(Long id) {
        ReservaAula reserva = reservaAulaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reserva não encontrada.", "RESERVA_NAO_ENCONTRADA"));
        validarCancelamento(reserva);
        reserva.setStatus(StatusReserva.CANCELADA);
        reservaAulaRepository.save(reserva);
    }

    @Transactional
    public ReservaAulaDTO atualizarPresenca(Long id, boolean presente) {
        ReservaAula reserva = reservaAulaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reserva não encontrada.", "RESERVA_NAO_ENCONTRADA"));
        reserva.setPresente(presente);
        return ReservaAulaDTO.from(reservaAulaRepository.save(reserva));
    }

    public List<ReservaAulaDTO> listar(Long alunoId, StatusReserva status) {
        List<ReservaAula> lista;
        if (alunoId != null && status != null) {
            Usuario aluno = usuarioRepository.findById(alunoId).orElseThrow();
            lista = reservaAulaRepository.findByAlunoAndStatus(aluno, status);
        } else if (status != null) {
            lista = reservaAulaRepository.findByStatus(status);
        } else {
            lista = reservaAulaRepository.findAll();
        }
        return lista.stream().map(ReservaAulaDTO::from).toList();
    }

    public List<ReservaAulaDTO> inscritos(Long aulaId, LocalDate data) {
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new BusinessException("Aula não encontrada.", "AULA_NAO_ENCONTRADA"));
        return reservaAulaRepository.findByAulaAndDataReservaAndStatus(aula, data, StatusReserva.CONFIRMADA)
                .stream().map(ReservaAulaDTO::from).toList();
    }

    private void validarDiaSemana(Aula aula, LocalDate data) {
        if (DiaSemanaUtil.fromLocalDate(data) != aula.getDiaSemana()) {
            throw new BusinessException("Data não corresponde ao dia da aula.", "AULA_DIA_INVALIDO");
        }
    }

    private void validarCapacidade(Aula aula, LocalDate data) {
        long inscritos = reservaAulaRepository.findByAulaAndDataReservaAndStatus(aula, data, StatusReserva.CONFIRMADA).size();
        if (inscritos >= aula.getCapacidadeMaxima()) {
            throw new BusinessException("Aula lotada.", "AULA_LOTADA");
        }
    }

    private void validarDuplicata(Aula aula, Usuario aluno, LocalDate data) {
        boolean existe = reservaAulaRepository.findByAulaAndDataReservaAndStatus(aula, data, StatusReserva.CONFIRMADA)
                .stream().anyMatch(r -> r.getAluno().getId().equals(aluno.getId()));
        if (existe) {
            throw new BusinessException("Aluno já possui reserva nesta aula.", "RESERVA_DUPLICADA");
        }
    }

    private void validarCancelamento(ReservaAula reserva) {
        LocalDateTime inicio = LocalDateTime.of(reserva.getDataReserva(), reserva.getAula().getHorarioInicio());
        if (RelogioSaoPaulo.dataHora().isAfter(inicio.minusHours(1))) {
            throw new BusinessException("Cancelamento permitido apenas até 1h antes.", "CANCELAMENTO_TARDE");
        }
    }
}
