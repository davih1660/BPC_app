package com.bpc.escola.service;

import com.bpc.escola.domain.WellhubSyncErro;
import com.bpc.escola.dto.CreateReservaColetivaRequest;
import com.bpc.escola.dto.ReservaColetivaDTO;
import com.bpc.escola.dto.WellhubSyncErroDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.ReservaColetivaRepository;
import com.bpc.escola.repository.UsuarioRepository;
import com.bpc.escola.repository.WellhubSyncErroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WellhubIntegracaoService {

    private final ReservaColetivaService reservaColetivaService;
    private final ReservaColetivaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final WellhubSyncErroRepository erroRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ReservaColetivaDTO importarReserva(Map<String, Object> payload) {
        try {
            String wellhubId = String.valueOf(payload.get("wellhubReservaId"));
            if (reservaRepository.findByWellhubReservaId(wellhubId).isPresent()) {
                throw new BusinessException("Reserva Wellhub já importada.", "WELLHUB_DUPLICADA");
            }
            Long horarioId = Long.valueOf(payload.get("horarioId").toString());
            Long alunoId = Long.valueOf(payload.get("alunoId").toString());
            LocalDate data = LocalDate.parse(payload.get("data").toString());

            usuarioRepository.findById(alunoId)
                    .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

            var dto = reservaColetivaService.criar(
                    new CreateReservaColetivaRequest(horarioId, alunoId, data, com.bpc.escola.domain.enums.OrigemReserva.WELLHUB));

            reservaRepository.findById(dto.id()).ifPresent(r -> {
                r.setWellhubReservaId(wellhubId);
                reservaRepository.save(r);
            });
            return dto;
        } catch (BusinessException e) {
            registrarErro(payload, e.getMessage());
            throw e;
        } catch (Exception e) {
            registrarErro(payload, e.getMessage());
            throw new BusinessException("Falha ao importar reserva Wellhub: " + e.getMessage(), "WELLHUB_ERRO");
        }
    }

    public List<WellhubSyncErroDTO> listarErros() {
        return erroRepository.findByResolvidoFalseOrderByCriadoEmDesc().stream()
                .map(WellhubSyncErroDTO::from)
                .toList();
    }

    @Transactional
    public void resolverErro(Long id) {
        WellhubSyncErro erro = erroRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Erro não encontrado.", "ERRO_NAO_ENCONTRADO"));
        erro.setResolvido(true);
        erroRepository.save(erro);
    }

    private void registrarErro(Map<String, Object> payload, String mensagem) {
        try {
            erroRepository.save(WellhubSyncErro.builder()
                    .payload(objectMapper.writeValueAsString(payload))
                    .mensagem(mensagem)
                    .resolvido(false)
                    .criadoEm(RelogioSaoPaulo.dataHora())
                    .build());
        } catch (Exception ignored) {
            erroRepository.save(WellhubSyncErro.builder()
                    .payload(payload.toString())
                    .mensagem(mensagem)
                    .resolvido(false)
                    .criadoEm(RelogioSaoPaulo.dataHora())
                    .build());
        }
    }
}
