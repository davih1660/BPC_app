package com.bpc.escola.repository;

import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.TipoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Page<Usuario> findByTipoUsuario(TipoUsuario tipo, Pageable pageable);

    Page<Usuario> findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nome, String email, Pageable pageable);

    Page<Usuario> findByTipoUsuarioAndNomeContainingIgnoreCase(
            TipoUsuario tipo, String nome, Pageable pageable);
}
