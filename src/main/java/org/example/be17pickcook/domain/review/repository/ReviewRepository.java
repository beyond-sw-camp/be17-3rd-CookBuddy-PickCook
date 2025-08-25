package org.example.be17pickcook.domain.review.repository;

import org.example.be17pickcook.domain.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
