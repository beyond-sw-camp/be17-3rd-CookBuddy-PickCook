package org.example.be17pickcook.domain.order.repository;

import org.example.be17pickcook.domain.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
