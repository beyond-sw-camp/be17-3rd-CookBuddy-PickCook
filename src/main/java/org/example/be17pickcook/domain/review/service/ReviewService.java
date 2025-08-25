package org.example.be17pickcook.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ProductRepository productRepository;


}
