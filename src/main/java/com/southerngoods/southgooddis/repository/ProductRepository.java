package com.southerngoods.southgooddis.repository;

import com.southerngoods.southgooddis.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}