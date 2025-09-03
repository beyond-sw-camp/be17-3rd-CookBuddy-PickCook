package org.example.be17pickcook.domain.product.repository;

import org.example.be17pickcook.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p.id, p.title, p.main_image_url, p.discount_rate, p.original_price, " +
            "p.review_count FROM Product p")
    Page<Object[]> findAllOnlyProductList(Pageable pageable);


}
