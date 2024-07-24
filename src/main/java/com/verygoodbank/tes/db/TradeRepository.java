package com.verygoodbank.tes.db;

import com.verygoodbank.tes.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Product, Long> {
  List<Product> findAllByIdentifier(String identifier);
}
