package com.southerngoods.southgooddis.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "income")
public class Income {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float amount;
    private String incomeType;
    private LocalDate incomeDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
    public String getIncomeType() { return incomeType; }
    public void setIncomeType(String incomeType) { this.incomeType = incomeType; }
    public LocalDate getIncomeDate() { return incomeDate; }
    public void setIncomeDate(LocalDate incomeDate) { this.incomeDate = incomeDate; }
}