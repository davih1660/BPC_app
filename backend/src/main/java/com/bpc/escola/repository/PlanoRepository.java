package com.bpc.escola.repository;

import com.bpc.escola.domain.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanoRepository extends JpaRepository<Plano, Long> {

    Optional<Plano> findByNome(String nome);
}
