package com.bpc.escola.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(-3)
@RequiredArgsConstructor
public class NotificacaoTipoSchemaFix implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE notificacoes DROP CONSTRAINT IF EXISTS notificacoes_tipo_check");
            jdbcTemplate.execute("""
                    ALTER TABLE notificacoes ADD CONSTRAINT notificacoes_tipo_check CHECK (tipo IN (
                        'RESERVA_CONFIRMADA',
                        'LISTA_ESPERA_PROMOVIDO',
                        'RESERVA_CANCELADA',
                        'COBRANCA_VENCIDA',
                        'AGENDA_BLOQUEADA',
                        'USO_LIVRE_APROVADO',
                        'USO_LIVRE_RECUSADO'
                    ))
                    """);
        } catch (Exception ignored) {
            // Constraint pode já estar atualizado ou tabela ainda não existir no primeiro boot
        }
    }
}
