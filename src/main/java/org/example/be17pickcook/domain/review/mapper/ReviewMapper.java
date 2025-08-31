package org.example.be17pickcook.domain.review.mapper;

import org.example.be17pickcook.domain.review.model.Review;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.review.model.ReviewImage;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    // =================================================================
    // Request DTO → Entity 매핑
    // =================================================================

    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    Review writeRequestToEntity(ReviewDto.WriteRequest dto);

    // =================================================================
    // Entity → Response DTO 매핑
    // =================================================================

    @Mapping(source = "product.id", target = "productId")
    // 🚨 제거: productName 필드가 ReviewDto.Response에 없음
    @Mapping(target = "isMyReview", expression = "java(currentUserId != null && review.getUser().getIdx().equals(currentUserId))")
    @Mapping(target = "canModify", expression = "java(review.isModifiable() && (currentUserId != null && review.getUser().getIdx().equals(currentUserId)))")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "imageCount", expression = "java(review.getImages() != null ? review.getImages().size() : 0)")
    @Mapping(target = "author", expression = "java(mapAuthorInfo(review.getUser()))")
    @Mapping(target = "images", expression = "java(mapImageInfos(review.getImages()))")
        // 🚨 제거: imageUrls 필드가 ReviewDto.Response에 없음
    ReviewDto.Response entityToResponse(Review review, @Context Integer currentUserId);

    // =================================================================
    // 매핑 유틸리티 메서드
    // =================================================================

    /**
     * 닉네임 마스킹 처리
     * 예: "김철수" → "김**"
     */
    @Named("maskNickname")
    default String maskNickname(String nickname) {
        if (nickname == null || nickname.length() <= 1) {
            return nickname;
        }

        if (nickname.length() == 2) {
            return nickname.charAt(0) + "*";
        }

        return nickname.charAt(0) + "*".repeat(nickname.length() - 1);
    }

    /**
     * LocalDateTime을 문자열로 포맷팅
     */
    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    /**
     * User를 AuthorInfo로 변환
     */
    default ReviewDto.AuthorInfo mapAuthorInfo(org.example.be17pickcook.domain.user.model.User user) {
        return ReviewDto.AuthorInfo.fromUser(user);
    }

    /**
     * ReviewImage 리스트를 ImageInfo 리스트로 변환
     */
    default List<ReviewDto.ImageInfo> mapImageInfos(List<ReviewImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .sorted((a, b) -> Integer.compare(a.getImageOrder(), b.getImageOrder()))
                .map(ReviewDto.ImageInfo::fromEntity)
                .toList();
    }
}