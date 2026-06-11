package com.bpc.escola.service;

import com.bpc.escola.domain.Ocorrencia;
import com.bpc.escola.domain.TipoOcorrencia;
import com.bpc.escola.domain.enums.StatusOcorrencia;
import com.bpc.escola.dto.CriarOcorrenciaRequest;
import com.bpc.escola.dto.OcorrenciaDTO;
import com.bpc.escola.dto.OcorrenciaImagemDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.OcorrenciaImagemRepository;
import com.bpc.escola.repository.OcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OcorrenciaService {

    private static final int DESCRICAO_MAX = 10_000;

    private final OcorrenciaRepository ocorrenciaRepository;
    private final OcorrenciaImagemRepository imagemRepository;
    private final EmbarcacaoService embarcacaoService;
    private final UsuarioService usuarioService;
    private final TipoOcorrenciaService tipoOcorrenciaService;

    public List<OcorrenciaDTO> listar(StatusOcorrencia status) {
        List<Ocorrencia> lista = status != null
                ? ocorrenciaRepository.findByStatus(status)
                : ocorrenciaRepository.findAll();
        if (lista.isEmpty()) {
            return List.of();
        }
        List<Long> ids = lista.stream().map(Ocorrencia::getId).toList();
        Map<Long, List<OcorrenciaImagemDTO>> imagensPorOcorrencia = imagemRepository
                .findByOcorrencia_IdIn(ids).stream()
                .collect(Collectors.groupingBy(
                        img -> img.getOcorrencia().getId(),
                        Collectors.mapping(OcorrenciaImagemDTO::from, Collectors.toList())
                ));
        return lista.stream()
                .map(o -> OcorrenciaDTO.from(o, imagensPorOcorrencia.getOrDefault(o.getId(), List.of())))
                .toList();
    }

    @Transactional
    public OcorrenciaDTO criar(CriarOcorrenciaRequest dto, Long usuarioId) {
        validarCriacao(dto);
        TipoOcorrencia tipo = tipoOcorrenciaService.getAtivo(dto.tipoOcorrenciaId());
        Ocorrencia o = Ocorrencia.builder()
                .embarcacao(embarcacaoService.get(dto.embarcacaoId()))
                .usuario(usuarioService.get(usuarioId))
                .tipoOcorrencia(tipo)
                .titulo(tipo.getNome())
                .descricao(dto.descricao())
                .gravidade(tipo.getGravidade())
                .status(StatusOcorrencia.ABERTA)
                .dataAbertura(RelogioSaoPaulo.dataHora())
                .build();
        return toDto(ocorrenciaRepository.save(o));
    }

    private OcorrenciaDTO toDto(Ocorrencia o) {
        List<OcorrenciaImagemDTO> imagens = imagemRepository.findByOcorrenciaIdOrderByCriadoEmAsc(o.getId())
                .stream()
                .map(OcorrenciaImagemDTO::from)
                .toList();
        return OcorrenciaDTO.from(o, imagens);
    }

    private void validarCriacao(CriarOcorrenciaRequest dto) {
        if (dto.tipoOcorrenciaId() == null) {
            throw new BusinessException("Selecione o tipo da ocorrência.", "TIPO_OCORRENCIA_OBRIGATORIO");
        }
        if (dto.embarcacaoId() == null) {
            throw new BusinessException("Selecione a embarcação.", "EMBARCACAO_OBRIGATORIA");
        }
        if (dto.descricao() != null && dto.descricao().length() > DESCRICAO_MAX) {
            throw new BusinessException(
                    "Descrição deve ter no máximo " + DESCRICAO_MAX + " caracteres.",
                    "DESCRICAO_MUITO_LONGA");
        }
    }

    @Transactional
    public OcorrenciaDTO atualizarStatus(Long id, StatusOcorrencia status) {
        Ocorrencia o = ocorrenciaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ocorrência não encontrada.", "OCORRENCIA_NAO_ENCONTRADA"));
        o.setStatus(status);
        return toDto(ocorrenciaRepository.save(o));
    }
}
