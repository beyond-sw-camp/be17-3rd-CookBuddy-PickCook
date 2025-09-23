package org.example.be17pickcook.domain.order.repository;

import org.example.be17pickcook.domain.order.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {
}
