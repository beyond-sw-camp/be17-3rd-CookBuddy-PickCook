package org.example.be17pickcook.domain.review.repository;

import org.example.be17pickcook.domain.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct_Id(Long productId);   // 특정 상품 리뷰 조회
    List<Review> findByUser_Idx(Long userIdx);       // [변경] 특정 유저 리뷰 조회 (User의 PK는 idx)
}
