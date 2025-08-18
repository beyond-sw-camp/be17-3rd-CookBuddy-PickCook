package org.example.be17pickcook.domain.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.review.model.Review;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.review.repository.ReviewRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // 리뷰 등록
    @Transactional
    public void register(UserDto.AuthUser authUser, ReviewDto.ReviewRequestDto dto, List<MultipartFile> files) {
        User user = userRepository.findById(authUser.getIdx())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Product product = productRepository.findById(dto.getProduct_id())
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        Review review = dto.toEntity(user, product);
        reviewRepository.save(review);

        // 파일 저장 로직 추가 가능 추후 개발
    }

    // 특정 상품의 리뷰 전체 조회
    public List<ReviewDto.ReviewResponseDto> getAllReviewByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findAllByProductId(productId);

        return reviews.stream().map(review -> {
            ReviewDto.ReviewResponseDto dto = ReviewDto.ReviewResponseDto.from(review);
            return dto;
        }).toList();
    }
}
