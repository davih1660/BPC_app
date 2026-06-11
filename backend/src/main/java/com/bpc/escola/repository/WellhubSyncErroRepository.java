package com.bpc.escola.repository;

import com.bpc.escola.domain.WellhubSyncErro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WellhubSyncErroRepository extends JpaRepository<WellhubSyncErro, Long> {

    List<WellhubSyncErro> findByResolvidoFalseOrderByCriadoEmDesc();
}
