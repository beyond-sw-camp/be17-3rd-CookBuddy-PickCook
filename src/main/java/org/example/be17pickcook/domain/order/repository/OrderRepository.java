package org.example.be17pickcook.domain.order.repository;

import org.example.be17pickcook.domain.order.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByPaymentId(String paymentId);
}
