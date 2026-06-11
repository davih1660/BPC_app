package com.bpc.escola.repository;

import com.bpc.escola.domain.GrupoCanoa;
import com.bpc.escola.domain.SessaoHorario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoCanoaRepository extends JpaRepository<GrupoCanoa, Long> {

    List<GrupoCanoa> findBySessao(SessaoHorario sessao);
}
