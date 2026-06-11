package com.bpc.escola.seed;

import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.enums.CategoriaPlano;
import com.bpc.escola.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlanoBackfillRunner implements CommandLineRunner {

    private final PlanoRepository planoRepository;

    @Override
    @Transactional
    public void run(String... args) {
        planoRepository.findAll().stream()
                .filter(p -> p.getCategoriaPlano() == null)
                .forEach(p -> {
                    p.setCategoriaPlano(inferirCategoria(p));
                    planoRepository.save(p);
                });
    }

    private CategoriaPlano inferirCategoria(Plano plano) {
        return switch (plano.getTipoPlano()) {
            case AVULSO_REMADAS -> CategoriaPlano.AVULSO;
            case WELLHUB -> CategoriaPlano.WELLHUB;
            default -> CategoriaPlano.RECORRENTE;
        };
    }
}
