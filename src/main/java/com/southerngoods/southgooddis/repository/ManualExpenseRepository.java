package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.ManualExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ManualExpenseRepository extends JpaRepository<ManualExpense, Long> {
    List<ManualExpense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate startDate, LocalDate endDate);
}