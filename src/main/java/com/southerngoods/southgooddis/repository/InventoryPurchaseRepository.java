package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.InventoryPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface InventoryPurchaseRepository extends JpaRepository<InventoryPurchase, Long> {
    List<InventoryPurchase> findByPurchaseDateBetweenOrderByPurchaseDateDesc(LocalDate startDate, LocalDate endDate);
}