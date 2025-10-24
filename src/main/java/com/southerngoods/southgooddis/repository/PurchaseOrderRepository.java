package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByPurchaseDateBetweenOrderByPurchaseDateDesc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.purchaseDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findPurchasesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}