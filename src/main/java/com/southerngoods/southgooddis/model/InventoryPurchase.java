package com.southerngoods.southgooddis.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_purchases")
public class InventoryPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private float quantityPurchased;
    private float unitPrice;
    private LocalDate purchaseDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public float getQuantityPurchased() { return quantityPurchased; }
    public void setQuantityPurchased(float quantityPurchased) { this.quantityPurchased = quantityPurchased; }
    public float getUnitPrice() { return unitPrice; }
    public void setUnitPrice(float unitPrice) { this.unitPrice = unitPrice; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
}