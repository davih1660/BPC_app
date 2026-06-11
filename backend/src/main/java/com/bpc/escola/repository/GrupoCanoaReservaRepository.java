package com.bpc.escola.repository;

import com.bpc.escola.domain.GrupoCanoa;
import com.bpc.escola.domain.GrupoCanoaReserva;
import com.bpc.escola.domain.ReservaColetiva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoCanoaReservaRepository extends JpaRepository<GrupoCanoaReserva, Long> {

    List<GrupoCanoaReserva> findByGrupo(GrupoCanoa grupo);

    void deleteByReserva(ReservaColetiva reserva);
}
