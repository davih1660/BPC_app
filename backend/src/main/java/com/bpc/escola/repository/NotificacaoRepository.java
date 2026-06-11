package com.bpc.escola.repository;

import com.bpc.escola.domain.Notificacao;
import com.bpc.escola.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioOrderByCriadoEmDesc(Usuario usuario);

    List<Notificacao> findByUsuarioAndLidaFalseOrderByCriadoEmDesc(Usuario usuario);

    long countByUsuarioAndLidaFalse(Usuario usuario);
}
