package com.verygoodbank.tes.db;

import com.verygoodbank.tes.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END FROM Order o WHERE o.identifier = :identifier AND o.status = 'FINISHED'")
    boolean isReady(String identifier);
}
