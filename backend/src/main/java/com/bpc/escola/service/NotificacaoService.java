package com.bpc.escola.service;

import com.bpc.escola.domain.Notificacao;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.TipoNotificacao;
import com.bpc.escola.dto.NotificacaoDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.NotificacaoRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public NotificacaoDTO criar(Long usuarioId, TipoNotificacao tipo, String titulo, String mensagem, String refTipo, Long refId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado.", "USUARIO_NAO_ENCONTRADO"));
        Notificacao n = notificacaoRepository.save(Notificacao.builder()
                .usuario(usuario)
                .tipo(tipo)
                .titulo(titulo)
                .mensagem(mensagem)
                .lida(false)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .refTipo(refTipo)
                .refId(refId)
                .build());
        return NotificacaoDTO.from(n);
    }

    public List<NotificacaoDTO> listar(Long usuarioId, Boolean somenteNaoLidas) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado.", "USUARIO_NAO_ENCONTRADO"));
        List<Notificacao> lista = Boolean.TRUE.equals(somenteNaoLidas)
                ? notificacaoRepository.findByUsuarioAndLidaFalseOrderByCriadoEmDesc(usuario)
                : notificacaoRepository.findByUsuarioOrderByCriadoEmDesc(usuario);
        return lista.stream().map(NotificacaoDTO::from).toList();
    }

    public long contarNaoLidas(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado.", "USUARIO_NAO_ENCONTRADO"));
        return notificacaoRepository.countByUsuarioAndLidaFalse(usuario);
    }

    @Transactional
    public NotificacaoDTO marcarLida(Long id) {
        Notificacao n = notificacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Notificação não encontrada.", "NOTIFICACAO_NAO_ENCONTRADA"));
        n.setLida(true);
        return NotificacaoDTO.from(notificacaoRepository.save(n));
    }
}
