package org.example.be17pickcook.domain.product.repository;

import org.example.be17pickcook.domain.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 필요하면 커스텀 쿼리 메서드 추가
}
