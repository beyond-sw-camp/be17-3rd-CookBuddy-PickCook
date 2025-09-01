package org.example.be17pickcook.domain.order.repository;

import org.example.be17pickcook.domain.order.model.OrderItem;
import org.example.be17pickcook.domain.order.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product p JOIN FETCH oi.order o " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "ORDER BY o.createdAt ASC")
    List<OrderItem> findAllByPeriod(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);


}
