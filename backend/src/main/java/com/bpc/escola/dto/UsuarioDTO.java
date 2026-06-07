package com.bpc.escola.dto;

import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioDTO(
        Long id,
        @NotBlank String nome,
        @NotBlank @Email String email,
        String telefone,
        @NotNull TipoUsuario tipoUsuario
) {
    public static UsuarioDTO from(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getNome(), u.getEmail(), u.getTelefone(), u.getTipoUsuario());
    }
}
