package com.southerngoods.southgooddis.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "compartments")
public class Compartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_id", nullable = false)
    private Shelf shelf;

    @Column(name = "level", length = 50, nullable = false)
    private String level;

    @Column(name = "capacity_volume", precision = 10, scale = 2)
    private BigDecimal capacityVolume;

    @OneToMany(mappedBy = "compartment", fetch = FetchType.LAZY)
    private List<StockBatch> stockBatches;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Shelf getShelf() {
        return shelf;
    }

    public void setShelf(Shelf shelf) {
        this.shelf = shelf;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public BigDecimal getCapacityVolume() {
        return capacityVolume;
    }

    public void setCapacityVolume(BigDecimal capacityVolume) {
        this.capacityVolume = capacityVolume;
    }

    public List<StockBatch> getStockBatches() {
        return stockBatches;
    }

    public void setStockBatches(List<StockBatch> stockBatches) {
        this.stockBatches = stockBatches;
    }
}