package com.bpc.escola.repository;

import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.TipoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByTipoUsuarioOrderByNomeAsc(TipoUsuario tipo);

    Page<Usuario> findByTipoUsuario(TipoUsuario tipo, Pageable pageable);

    Page<Usuario> findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nome, String email, Pageable pageable);

    Page<Usuario> findByTipoUsuarioAndNomeContainingIgnoreCase(
            TipoUsuario tipo, String nome, Pageable pageable);
}
