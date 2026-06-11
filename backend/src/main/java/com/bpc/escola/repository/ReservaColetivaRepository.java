package com.bpc.escola.repository;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaColetivaRepository extends JpaRepository<ReservaColetiva, Long> {

    List<ReservaColetiva> findByHorarioAndDataReservaAndStatus(
            HorarioColetivo horario, LocalDate data, StatusReserva status);

    long countByHorarioAndDataReservaAndStatus(
            HorarioColetivo horario, LocalDate data, StatusReserva status);

    boolean existsByHorarioAndAlunoAndDataReservaAndStatus(
            HorarioColetivo horario, Usuario aluno, LocalDate data, StatusReserva status);

    List<ReservaColetiva> findByDataReserva(LocalDate data);

    List<ReservaColetiva> findByAlunoAndStatus(Usuario aluno, StatusReserva status);

    Optional<ReservaColetiva> findByWellhubReservaId(String wellhubReservaId);

    @Query("SELECT COUNT(r) FROM ReservaColetiva r WHERE r.aluno = :aluno AND r.status = :status "
            + "AND r.dataReserva BETWEEN :inicio AND :fim")
    long countByAlunoAndStatusAndDataReservaBetween(
            @Param("aluno") Usuario aluno,
            @Param("status") StatusReserva status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);
}
