package com.southerngoods.southgooddis.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "manual_expenses")
public class ManualExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private float amount;
    private LocalDate expenseDate;
    private String category;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}