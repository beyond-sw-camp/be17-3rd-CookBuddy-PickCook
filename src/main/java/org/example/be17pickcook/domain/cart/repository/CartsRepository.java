package org.example.be17pickcook.domain.cart.repository;

import org.example.be17pickcook.domain.cart.model.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartsRepository extends JpaRepository<Carts, Long> {
    List<Carts> findByUserIdx(Integer userIdx); // 유저별 장바구니 전체 조회
    Optional<Carts> findByUserIdxAndProductId(Integer userIdx, Long productId); // 유저 + 상품 조합으로 장바구니 존재 여부 확인 (토글용)

    // User + Product 기준으로 장바구니 삭제
    @Modifying
    @Query("DELETE FROM Carts c WHERE c.user.idx = :userId AND c.product.id = :productId")
    void deleteByUserAndProduct(@Param("userId") Integer userId, @Param("productId") Long productId);

    // 사용자별 장바구니 담았는지 여부 (하나)
    boolean existsByUserIdxAndProductId(Integer userId, Long productId);

    // 사용자별 장바구니 담았는지 여부 (여러개)
    @Query("SELECT c.product.id FROM Carts c WHERE c.user.idx = :userId AND c.product.id IN :productIds")
    List<Long> findCartsProductIdsByUser(@Param("userId") Integer userId,
                                         @Param("productIds") List<Long> productIds);

}
