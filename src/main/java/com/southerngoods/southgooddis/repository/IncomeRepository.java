package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByIncomeDateBetweenOrderByIncomeDateDesc(LocalDate startDate, LocalDate endDate);
}