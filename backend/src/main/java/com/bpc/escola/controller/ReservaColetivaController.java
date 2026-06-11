package com.bpc.escola.controller;

import com.bpc.escola.dto.CreateReservaColetivaRequest;
import com.bpc.escola.dto.ReservaColetivaDTO;
import com.bpc.escola.service.ReservaColetivaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas-coletivas")
@RequiredArgsConstructor
public class ReservaColetivaController {

    private final ReservaColetivaService service;

    @GetMapping
    public List<ReservaColetivaDTO> listar(
            @RequestParam(required = false) Long horarioId,
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        if (alunoId != null) {
            return service.listarPorAluno(alunoId);
        }
        if (horarioId != null && data != null) {
            return service.listarPorHorario(horarioId, data);
        }
        throw new IllegalArgumentException("Informe horarioId+data ou alunoId");
    }

    @PostMapping
    public ReservaColetivaDTO criar(@RequestBody CreateReservaColetivaRequest request) {
        return service.criar(request);
    }

    @DeleteMapping("/{id}")
    public void cancelar(@PathVariable Long id) {
        service.cancelar(id);
    }

    @PatchMapping("/{id}/presenca")
    public ReservaColetivaDTO presenca(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return service.atualizarPresenca(id, Boolean.TRUE.equals(body.get("presente")));
    }
}
