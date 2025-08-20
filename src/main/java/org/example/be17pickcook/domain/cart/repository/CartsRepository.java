package org.example.be17pickcook.domain.cart.repository;

import org.example.be17pickcook.domain.cart.model.Carts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartsRepository extends JpaRepository<Carts, Long> {
    // 유저별 장바구니 전체 조회
    List<Carts> findByUserIdx(Integer userIdx);

    // 유저 + 상품 조합으로 장바구니 존재 여부 확인 (토글용)
    Optional<Carts> findByUserIdxAndProductId(Integer userIdx, Long productId);
}
