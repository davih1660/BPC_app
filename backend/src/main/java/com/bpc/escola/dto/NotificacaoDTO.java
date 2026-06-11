package com.bpc.escola.dto;

import com.bpc.escola.domain.Notificacao;
import com.bpc.escola.domain.enums.TipoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoDTO(
        Long id,
        String titulo,
        String mensagem,
        TipoNotificacao tipo,
        Boolean lida,
        LocalDateTime criadoEm,
        String refTipo,
        Long refId
) {
    public static NotificacaoDTO from(Notificacao n) {
        return new NotificacaoDTO(
                n.getId(),
                n.getTitulo(),
                n.getMensagem(),
                n.getTipo(),
                n.getLida(),
                n.getCriadoEm(),
                n.getRefTipo(),
                n.getRefId()
        );
    }
}
