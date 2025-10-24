package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementType = 'SALE' AND FUNCTION('CAST', sm.movementDate AS date) BETWEEN :startDate AND :endDate")
    List<StockMovement> findSalesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}