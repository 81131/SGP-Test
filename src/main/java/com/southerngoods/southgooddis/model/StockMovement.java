package com.southerngoods.southgooddis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // link to StockBatch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_batch_id", nullable = false)
    private StockBatch stockBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String movementType; // "SALE", "ADJUSTMENT_OUT", etc.

    @Column(nullable = false)
    private int quantity; // Negative for "SALE"

    @Column(nullable = false)
    private LocalDateTime movementDate;

    private String notes;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StockBatch getStockBatch() { return stockBatch; }
    public void setStockBatch(StockBatch stockBatch) { this.stockBatch = stockBatch; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDateTime movementDate) { this.movementDate = movementDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}