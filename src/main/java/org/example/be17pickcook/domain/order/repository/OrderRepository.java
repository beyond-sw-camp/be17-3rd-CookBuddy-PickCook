package org.example.be17pickcook.domain.order.repository;

import org.example.be17pickcook.domain.order.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByPaymentId(String paymentId);
}
