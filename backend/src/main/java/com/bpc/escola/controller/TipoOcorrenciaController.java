package com.bpc.escola.controller;

import com.bpc.escola.dto.TipoOcorrenciaDTO;
import com.bpc.escola.service.TipoOcorrenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-ocorrencia")
@RequiredArgsConstructor
public class TipoOcorrenciaController {

    private final TipoOcorrenciaService service;

    @GetMapping
    public List<TipoOcorrenciaDTO> listar(
            @RequestParam(required = false, defaultValue = "false") boolean somenteAtivos) {
        return service.listar(somenteAtivos);
    }

    @GetMapping("/{id}")
    public TipoOcorrenciaDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public TipoOcorrenciaDTO criar(@RequestBody TipoOcorrenciaDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public TipoOcorrenciaDTO atualizar(@PathVariable Long id, @RequestBody TipoOcorrenciaDTO dto) {
        return service.atualizar(id, dto);
    }
}
