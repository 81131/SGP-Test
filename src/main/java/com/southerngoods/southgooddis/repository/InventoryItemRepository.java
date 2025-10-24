package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
}