package com.bpc.escola.seed;

import com.bpc.escola.domain.*;
import com.bpc.escola.domain.enums.StatusCobranca;
import com.bpc.escola.domain.enums.TipoBloqueioAgenda;
import com.bpc.escola.domain.enums.TipoUsuario;
import com.bpc.escola.repository.*;
import com.bpc.escola.service.RelogioSaoPaulo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(4)
@RequiredArgsConstructor
public class ProtoFeaturesSeeder implements CommandLineRunner {

    private final CobrancaRepository cobrancaRepository;
    private final BloqueioAgendaRepository bloqueioAgendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (cobrancaRepository.count() == 0) {
            List<Usuario> alunos = usuarioRepository.findAll().stream()
                    .filter(u -> u.getTipoUsuario() == TipoUsuario.ALUNO)
                    .limit(3)
                    .toList();
            Plano plano = planoRepository.findAll().stream().findFirst().orElse(null);
            for (int i = 0; i < alunos.size(); i++) {
                cobrancaRepository.save(Cobranca.builder()
                        .aluno(alunos.get(i))
                        .plano(plano)
                        .valor(new BigDecimal("189.90"))
                        .vencimento(RelogioSaoPaulo.hoje().plusDays(5 - i))
                        .status(i == 2 ? StatusCobranca.INADIMPLENTE : StatusCobranca.PENDENTE)
                        .criadoEm(RelogioSaoPaulo.dataHora())
                        .build());
            }
        }

        if (bloqueioAgendaRepository.count() == 0) {
            bloqueioAgendaRepository.save(BloqueioAgenda.builder()
                    .data(RelogioSaoPaulo.hoje().plusDays(14))
                    .horario(null)
                    .tipo(TipoBloqueioAgenda.FERIADO)
                    .motivo("Feriado municipal — base fechada")
                    .criadoEm(RelogioSaoPaulo.dataHora())
                    .build());
        }
    }
}
