package com.southerngoods.southgooddis.model;

import jakarta.persistence.*;
import java.math.BigDecimal; // <-- 1. IMPORT
import java.time.LocalDate;

@Entity
@Table(name = "manual_expenses")
public class ManualExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // 2. CHANGE float to BigDecimal and add column definition
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    private LocalDate expenseDate;
    private String category;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // 3. UPDATE Getters and Setters for BigDecimal
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}