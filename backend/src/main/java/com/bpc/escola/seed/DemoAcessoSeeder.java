package com.bpc.escola.seed;

import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.TipoUsuario;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Garante usuários demo para login mesmo se o DataSeeder foi pulado (banco parcial).
 */
@Component
@Order(-1)
@RequiredArgsConstructor
public class DemoAcessoSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        garantir("Admin Recepção", "admin@bpc.com", "11999990001", TipoUsuario.ADMIN);
        garantir("Prof. Ricardo", "ricardo@bpc.com", "11999990002", TipoUsuario.PROFESSOR);
        garantir("Prof. Marina", "marina@bpc.com", "11999990003", TipoUsuario.PROFESSOR);
        garantir("Equipe Manutenção", "manutencao@bpc.com", "11999990099", TipoUsuario.MANUTENCAO);
        garantir("Ana Silva", "aluno1@bpc.com", "11988880001", TipoUsuario.ALUNO);
    }

    private void garantir(String nome, String email, String telefone, TipoUsuario tipo) {
        usuarioRepository.findByEmail(email).ifPresentOrElse(
                usuario -> {
                    if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                        usuario.setSenha(UsuarioSenhaSeeder.SENHA_DEMO);
                        usuarioRepository.save(usuario);
                    }
                },
                () -> usuarioRepository.save(Usuario.builder()
                        .nome(nome)
                        .email(email)
                        .telefone(telefone)
                        .tipoUsuario(tipo)
                        .senha(UsuarioSenhaSeeder.SENHA_DEMO)
                        .build())
        );
    }
}
