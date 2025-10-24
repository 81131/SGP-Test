package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    // Corrected Query: Join through StockBatch -> PurchaseOrder -> Product
    @Query("SELECT sm FROM StockMovement sm " +
            "JOIN sm.stockBatch sb " +
            "JOIN sb.purchaseOrder po " + // Assumes StockBatch has 'purchaseOrder' field
            "JOIN po.product p " +        // Assumes PurchaseOrder has 'product' field
            "WHERE sm.movementType = 'SALE' AND CAST(sm.movementDate AS date) BETWEEN :startDate AND :endDate")
    List<StockMovement> findSalesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}