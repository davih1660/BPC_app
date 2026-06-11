package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.TipoBloqueioAgenda;
import com.bpc.escola.dto.BloqueioAgendaDTO;
import com.bpc.escola.service.BloqueioAgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bloqueios-agenda")
@RequiredArgsConstructor
public class BloqueioAgendaController {

    private final BloqueioAgendaService service;

    @GetMapping
    public List<BloqueioAgendaDTO> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return service.listar(de, ate);
    }

    @GetMapping("/dia")
    public List<BloqueioAgendaDTO> porData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.listarPorData(data);
    }

    @PostMapping
    public BloqueioAgendaDTO criar(@RequestBody Map<String, Object> body) {
        LocalDate data = LocalDate.parse(body.get("data").toString());
        Long horarioId = body.get("horarioId") != null ? Long.valueOf(body.get("horarioId").toString()) : null;
        TipoBloqueioAgenda tipo = TipoBloqueioAgenda.valueOf(body.get("tipo").toString());
        String motivo = body.get("motivo").toString();
        boolean cancelar = body.get("cancelarInscritos") == null || Boolean.parseBoolean(body.get("cancelarInscritos").toString());
        return service.criar(data, horarioId, tipo, motivo, cancelar);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
