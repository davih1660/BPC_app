package com.bpc.escola.service;

import com.bpc.escola.domain.Aula;
import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.DiaSemana;
import com.bpc.escola.dto.AulaDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.AulaRepository;
import com.bpc.escola.repository.EmbarcacaoRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AulaService {

    private final AulaRepository aulaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmbarcacaoRepository embarcacaoRepository;

    public List<AulaDTO> listar(DiaSemana dia) {
        List<Aula> aulas = dia != null ? aulaRepository.findByDiaSemana(dia) : aulaRepository.findAll();
        return aulas.stream().map(AulaDTO::from).toList();
    }

    public AulaDTO buscar(Long id) {
        return AulaDTO.from(get(id));
    }

    @Transactional
    public AulaDTO criar(AulaDTO dto) {
        Usuario professor = usuarioRepository.findById(dto.professorId())
                .orElseThrow(() -> new BusinessException("Professor não encontrado.", "PROFESSOR_NAO_ENCONTRADO"));
        Embarcacao embarcacao = embarcacaoRepository.findById(dto.embarcacaoPrincipalId())
                .orElseThrow(() -> new BusinessException("Embarcação não encontrada.", "EMBARCACAO_NAO_ENCONTRADA"));

        Aula aula = Aula.builder()
                .titulo(dto.titulo())
                .diaSemana(dto.diaSemana())
                .horarioInicio(dto.horarioInicio())
                .horarioFim(dto.horarioFim())
                .capacidadeMaxima(dto.capacidadeMaxima())
                .professor(professor)
                .embarcacaoPrincipal(embarcacao)
                .build();
        return AulaDTO.from(aulaRepository.save(aula));
    }

    @Transactional
    public AulaDTO atualizar(Long id, AulaDTO dto) {
        Aula aula = get(id);
        if (dto.professorId() != null) {
            aula.setProfessor(usuarioRepository.findById(dto.professorId()).orElseThrow());
        }
        if (dto.embarcacaoPrincipalId() != null) {
            aula.setEmbarcacaoPrincipal(embarcacaoRepository.findById(dto.embarcacaoPrincipalId()).orElseThrow());
        }
        aula.setTitulo(dto.titulo());
        aula.setDiaSemana(dto.diaSemana());
        aula.setHorarioInicio(dto.horarioInicio());
        aula.setHorarioFim(dto.horarioFim());
        aula.setCapacidadeMaxima(dto.capacidadeMaxima());
        return AulaDTO.from(aulaRepository.save(aula));
    }

    public Aula get(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Aula não encontrada.", "AULA_NAO_ENCONTRADA"));
    }
}
