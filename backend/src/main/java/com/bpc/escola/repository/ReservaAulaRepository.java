package com.bpc.escola.repository;

import com.bpc.escola.domain.Aula;
import com.bpc.escola.domain.ReservaAula;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaAulaRepository extends JpaRepository<ReservaAula, Long> {

    List<ReservaAula> findByStatus(StatusReserva status);

    List<ReservaAula> findByAulaAndDataReservaAndStatus(Aula aula, LocalDate data, StatusReserva status);

    @Query("SELECT COUNT(r) FROM ReservaAula r WHERE r.aluno = :aluno AND r.status = :status " +
           "AND r.dataReserva BETWEEN :inicio AND :fim")
    long countByAlunoAndStatusAndDataReservaBetween(
            @Param("aluno") Usuario aluno,
            @Param("status") StatusReserva status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    List<ReservaAula> findByDataReserva(LocalDate data);

    List<ReservaAula> findByAlunoAndStatus(Usuario aluno, StatusReserva status);

    @Query("SELECT r FROM ReservaAula r WHERE r.dataReserva >= :data ORDER BY r.dataReserva, r.aula.horarioInicio")
    List<ReservaAula> findProximas(@Param("data") LocalDate data);
}
