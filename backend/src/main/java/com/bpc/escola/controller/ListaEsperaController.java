package com.bpc.escola.controller;

import com.bpc.escola.dto.ListaEsperaDTO;
import com.bpc.escola.service.ListaEsperaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lista-espera")
@RequiredArgsConstructor
public class ListaEsperaController {

    private final ListaEsperaService service;

    @GetMapping
    public List<ListaEsperaDTO> listar(@RequestParam Long alunoId) {
        return service.listarPorAluno(alunoId);
    }

    @PostMapping
    public ListaEsperaDTO entrar(@RequestBody Map<String, Object> body) {
        Long horarioId = Long.valueOf(body.get("horarioId").toString());
        Long alunoId = Long.valueOf(body.get("alunoId").toString());
        LocalDate data = LocalDate.parse(body.get("dataReserva").toString());
        return service.entrar(horarioId, alunoId, data);
    }

    @DeleteMapping("/{id}")
    public void sair(@PathVariable Long id, @RequestParam Long alunoId) {
        service.sair(id, alunoId);
    }
}
