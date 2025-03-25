package com.example.trading.repository;

import com.example.trading.model.StockGainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockGainerRepository extends JpaRepository<StockGainer, Long> {
}
