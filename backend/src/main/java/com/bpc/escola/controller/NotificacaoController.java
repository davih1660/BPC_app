package com.bpc.escola.controller;

import com.bpc.escola.dto.NotificacaoDTO;
import com.bpc.escola.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService service;

    @GetMapping
    public List<NotificacaoDTO> listar(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) Boolean somenteNaoLidas) {
        return service.listar(usuarioId, somenteNaoLidas);
    }

    @GetMapping("/contagem")
    public Map<String, Long> contagem(@RequestParam Long usuarioId) {
        return Map.of("naoLidas", service.contarNaoLidas(usuarioId));
    }

    @PatchMapping("/{id}/lida")
    public NotificacaoDTO marcarLida(@PathVariable Long id) {
        return service.marcarLida(id);
    }
}
