package com.bpc.escola.seed;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(-1)
@RequiredArgsConstructor
public class PlanoSchemaMigration implements CommandLineRunner {

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        entityManager.createNativeQuery(
                "ALTER TABLE planos DROP CONSTRAINT IF EXISTS planos_tipo_plano_check"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "ALTER TABLE planos DROP CONSTRAINT IF EXISTS planos_categoria_plano_check"
        ).executeUpdate();
    }
}
