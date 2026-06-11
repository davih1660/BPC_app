package com.bpc.escola.seed;

import com.bpc.escola.domain.Plano;
import com.bpc.escola.repository.AlunoPlanoRepository;
import com.bpc.escola.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(0)
@RequiredArgsConstructor
public class PlanoCatalogoSeeder implements CommandLineRunner {

    private static final Set<String> PLANOS_LEGADO = Set.of(
            "1x Semana",
            "2x Semana",
            "3x Semana",
            "Livre",
            "Ilimitado"
    );

    private final PlanoRepository planoRepository;
    private final AlunoPlanoRepository alunoPlanoRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        sincronizar();
    }

    public void sincronizar() {
        var nomesCatalogo = PlanoCatalogo.todos().stream()
                .map(PlanoCatalogo.Definicao::nome)
                .collect(Collectors.toSet());

        for (PlanoCatalogo.Definicao def : PlanoCatalogo.todos()) {
            Plano plano = planoRepository.findByNome(def.nome())
                    .orElseGet(() -> Plano.builder().nome(def.nome()).build());
            plano.setTipoPlano(def.tipoPlano());
            plano.setCategoriaPlano(def.categoriaPlano());
            plano.setPeriodicidade(def.periodicidade());
            plano.setQuantidadeAulasSemana(def.quantidadeAulasSemana());
            plano.setQuantidadeAulasMes(def.quantidadeAulasMes());
            plano.setQuantidadeRemadas(def.quantidadeRemadas());
            plano.setValidadeMeses(def.validadeMeses());
            plano.setValor(def.valor());
            plano.setIlimitado(def.ilimitado());
            planoRepository.save(plano);
        }

        planoRepository.findAll().stream()
                .filter(p -> PLANOS_LEGADO.contains(p.getNome()))
                .filter(p -> alunoPlanoRepository.countByPlano(p) == 0)
                .forEach(planoRepository::delete);

        planoRepository.findAll().stream()
                .filter(p -> !nomesCatalogo.contains(p.getNome()))
                .filter(p -> !PLANOS_LEGADO.contains(p.getNome()))
                .filter(p -> alunoPlanoRepository.countByPlano(p) == 0)
                .forEach(planoRepository::delete);
    }
}
