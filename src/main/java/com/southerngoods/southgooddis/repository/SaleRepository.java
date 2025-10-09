package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);
}