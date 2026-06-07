package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.DiaSemana;
import com.bpc.escola.dto.AulaDTO;
import com.bpc.escola.dto.ReservaAulaDTO;
import com.bpc.escola.service.AgendaService;
import com.bpc.escola.service.AulaService;
import com.bpc.escola.service.ReservaAulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/aulas")
@RequiredArgsConstructor
public class AulaController {

    private final AulaService aulaService;
    private final ReservaAulaService reservaAulaService;
    private final AgendaService agendaService;

    @GetMapping
    public List<AulaDTO> listar(@RequestParam(required = false) DiaSemana dia) {
        return aulaService.listar(dia);
    }

    @GetMapping("/agenda")
    public Object agenda(
            @RequestParam LocalDate de,
            @RequestParam LocalDate ate) {
        return agendaService.obter(de, ate);
    }

    @GetMapping("/{id}")
    public AulaDTO buscar(@PathVariable Long id) {
        return aulaService.buscar(id);
    }

    @GetMapping("/{id}/inscritos")
    public List<ReservaAulaDTO> inscritos(@PathVariable Long id, @RequestParam LocalDate data) {
        return reservaAulaService.inscritos(id, data);
    }

    @PostMapping
    public AulaDTO criar(@RequestBody AulaDTO dto) {
        return aulaService.criar(dto);
    }

    @PutMapping("/{id}")
    public AulaDTO atualizar(@PathVariable Long id, @RequestBody AulaDTO dto) {
        return aulaService.atualizar(id, dto);
    }
}
