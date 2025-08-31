package org.example.be17pickcook.domain.order.repository;

import org.example.be17pickcook.domain.order.model.OrderItem;
import org.example.be17pickcook.domain.order.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByPaymentId(String paymentId);

    /**
     * 사용자가 특정 상품을 구매한 완료된 주문 아이템 조회
     * 리뷰 작성 권한 검증용
     */
    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.user.idx = :userId " +
            "AND oi.product.id = :productId " +
            "AND o.status = 'COMPLETED' " +
            "ORDER BY oi.createdAt DESC")
    Optional<OrderItem> findCompletedOrderItemByUserAndProduct(
            @Param("userId") Integer userId,
            @Param("productId") Long productId);

    /**
     * 사용자의 완료된 주문 목록 조회 (리뷰 작성 가능한 상품 목록용)
     */
    @Query("SELECT o FROM Orders o " +
            "WHERE o.user.idx = :userId " +
            "AND o.status = 'COMPLETED' " +
            "ORDER BY o.createdAt DESC")
    List<Orders> findCompletedOrdersByUser(@Param("userId") Integer userId);
}