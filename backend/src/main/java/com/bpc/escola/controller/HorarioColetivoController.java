package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.DiaSemana;
import com.bpc.escola.dto.HorarioColetivoDTO;
import com.bpc.escola.service.HorarioColetivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios-coletivos")
@RequiredArgsConstructor
public class HorarioColetivoController {

    private final HorarioColetivoService service;

    @GetMapping
    public List<HorarioColetivoDTO> listar(@RequestParam(required = false) DiaSemana dia) {
        return service.listar(dia);
    }

    @GetMapping("/{id}")
    public HorarioColetivoDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public HorarioColetivoDTO criar(@RequestBody HorarioColetivoDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public HorarioColetivoDTO atualizar(@PathVariable Long id, @RequestBody HorarioColetivoDTO dto) {
        return service.atualizar(id, dto);
    }
}
