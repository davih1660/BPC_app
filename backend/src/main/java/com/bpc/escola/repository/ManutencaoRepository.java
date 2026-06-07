package com.bpc.escola.repository;

import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.Manutencao;
import com.bpc.escola.domain.enums.StatusManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    List<Manutencao> findByEmbarcacao(Embarcacao embarcacao);

    @Query("SELECT m FROM Manutencao m WHERE m.embarcacao = :embarcacao AND m.status IN :statuses " +
           "AND m.dataInicio <= :data AND (m.dataFim IS NULL OR m.dataFim >= :data)")
    Optional<Manutencao> findAtivaNaData(
            @Param("embarcacao") Embarcacao embarcacao,
            @Param("data") LocalDate data,
            @Param("statuses") List<StatusManutencao> statuses);
}
