package com.bpc.escola.seed;

import com.bpc.escola.domain.TipoOcorrencia;
import com.bpc.escola.repository.TipoOcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(0)
@RequiredArgsConstructor
public class TipoOcorrenciaSeeder implements CommandLineRunner {

    private final TipoOcorrenciaRepository repository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        for (var item : TipoOcorrenciaCatalogo.itens()) {
            repository.findByNome(item.nome()).ifPresentOrElse(
                    existente -> {
                        existente.setGravidade(item.gravidade());
                        existente.setOrdem(item.ordem());
                        if (existente.getAtivo() == null) {
                            existente.setAtivo(true);
                        }
                        repository.save(existente);
                    },
                    () -> repository.save(TipoOcorrencia.builder()
                            .nome(item.nome())
                            .gravidade(item.gravidade())
                            .ordem(item.ordem())
                            .ativo(true)
                            .build())
            );
        }
    }
}
