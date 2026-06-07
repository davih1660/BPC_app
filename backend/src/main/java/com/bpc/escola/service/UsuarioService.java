package com.bpc.escola.service;

import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.TipoUsuario;
import com.bpc.escola.dto.PageResponse;
import com.bpc.escola.dto.UsuarioDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public PageResponse<UsuarioDTO> listar(TipoUsuario tipo, String q, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Usuario> result;
        if (q != null && !q.isBlank() && tipo != null) {
            result = usuarioRepository.findByTipoUsuarioAndNomeContainingIgnoreCase(tipo, q, pr);
        } else if (q != null && !q.isBlank()) {
            result = usuarioRepository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pr);
        } else if (tipo != null) {
            result = usuarioRepository.findByTipoUsuario(tipo, pr);
        } else {
            result = usuarioRepository.findAll(pr);
        }
        return toPage(result);
    }

    public UsuarioDTO buscar(Long id) {
        return UsuarioDTO.from(get(id));
    }

    @Transactional
    public UsuarioDTO criar(UsuarioDTO dto) {
        Usuario u = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .telefone(dto.telefone())
                .tipoUsuario(dto.tipoUsuario())
                .build();
        return UsuarioDTO.from(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioDTO atualizar(Long id, UsuarioDTO dto) {
        Usuario u = get(id);
        u.setNome(dto.nome());
        u.setEmail(dto.email());
        u.setTelefone(dto.telefone());
        u.setTipoUsuario(dto.tipoUsuario());
        return UsuarioDTO.from(usuarioRepository.save(u));
    }

    public Usuario get(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado.", "USUARIO_NAO_ENCONTRADO"));
    }

    private PageResponse<UsuarioDTO> toPage(Page<Usuario> page) {
        return new PageResponse<>(
                page.getContent().stream().map(UsuarioDTO::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
