package com.southerngoods.southgooddis.dto;

// This is a simple class to hold the results of our sales query.
public class ItemSaleDTO {
    private String itemName;
    private int totalQuantity;

    public ItemSaleDTO(String itemName, int totalQuantity) {
        this.itemName = itemName;
        this.totalQuantity = totalQuantity;
    }

    // Getters and Setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}