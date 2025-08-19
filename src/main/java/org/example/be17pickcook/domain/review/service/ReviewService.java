package org.example.be17pickcook.domain.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.review.model.Review;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.review.repository.ReviewRepository;
import org.example.be17pickcook.domain.user.model.User;
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

    /** 리뷰 등록 */
    @Transactional
    public void register(Long userId, ReviewDto.ReviewRequestDto dto, List<MultipartFile> files) {
        // ------- 널가드 --------
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("review payload must not be null");
        }
        if (dto.getProductId() == null) {
            throw new IllegalArgumentException("productId must not be null");
        }

        // ------- 연관 엔티티 조회 --------
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userId));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + dto.getProductId()));

        // ------- 엔티티 생성/저장 --------
        Review review = dto.toEntity(user, product); // toEntity가 없다면 아래 예시 참고
        reviewRepository.save(review);

        // ------- (선택) 파일 처리 --------
        // if (files != null && !files.isEmpty()) {
        //     // 이미지 업로드/연결 로직
        // }
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
