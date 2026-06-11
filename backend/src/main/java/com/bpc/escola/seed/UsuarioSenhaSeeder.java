package com.bpc.escola.seed;

import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(-2)
@RequiredArgsConstructor
public class UsuarioSenhaSeeder implements CommandLineRunner {

    public static final String SENHA_DEMO = "123456";

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void run(String... args) {
        usuarioRepository.findAll().forEach(u -> {
            if (u.getSenha() == null || u.getSenha().isBlank()) {
                u.setSenha(SENHA_DEMO);
                usuarioRepository.save(u);
            }
        });
    }
}
