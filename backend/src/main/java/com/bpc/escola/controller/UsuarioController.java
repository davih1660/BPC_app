package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.TipoUsuario;
import com.bpc.escola.dto.PageResponse;
import com.bpc.escola.dto.UsuarioDTO;
import com.bpc.escola.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public PageResponse<UsuarioDTO> listar(
            @RequestParam(required = false) TipoUsuario tipo,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return usuarioService.listar(tipo, q, page, size);
    }

    @GetMapping("/{id}")
    public UsuarioDTO buscar(@PathVariable Long id) {
        return usuarioService.buscar(id);
    }

    @PostMapping
    public UsuarioDTO criar(@Valid @RequestBody UsuarioDTO dto) {
        return usuarioService.criar(dto);
    }

    @PutMapping("/{id}")
    public UsuarioDTO atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDTO dto) {
        return usuarioService.atualizar(id, dto);
    }
}
