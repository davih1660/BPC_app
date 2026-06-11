package com.bpc.escola.dto;

import com.bpc.escola.domain.WellhubSyncErro;

import java.time.LocalDateTime;

public record WellhubSyncErroDTO(
        Long id,
        String payload,
        String mensagem,
        Boolean resolvido,
        LocalDateTime criadoEm
) {
    public static WellhubSyncErroDTO from(WellhubSyncErro e) {
        return new WellhubSyncErroDTO(
                e.getId(),
                e.getPayload(),
                e.getMensagem(),
                e.getResolvido(),
                e.getCriadoEm()
        );
    }
}
