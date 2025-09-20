package org.example.be17pickcook.domain.recipe.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.service.S3UploadService;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.repository.LikeRepository;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.example.be17pickcook.domain.recipe.model.Recipe;
import org.example.be17pickcook.domain.recipe.model.RecipeComment;
import org.example.be17pickcook.domain.recipe.model.RecipeCommentDto;
import org.example.be17pickcook.domain.recipe.model.RecipeCommentImage;
import org.example.be17pickcook.domain.recipe.repository.RecipeCommentRepository;
import org.example.be17pickcook.domain.recipe.repository.RecipeRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeCommentService {
    private final RecipeCommentRepository commentRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final LikeService likeService;

    @Transactional
    public void addComment(RecipeCommentDto.Request request, int userId)
            throws SQLException, IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        RecipeComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        }

        // 댓글 엔티티 생성
        RecipeComment comment = request.toEntity(user, recipe, parentComment);

        // 이미지 URL이 있으면 연결
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            RecipeCommentImage image = RecipeCommentImage.builder()
                    .imageUrl(request.getImageUrl())
                    .build();
            comment.setImage(image);
        }

        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<RecipeCommentDto.Response> getComments(Long recipeId, Integer userIdx) {
        List<RecipeComment> comments = commentRepository.findByRecipeIdxAndParentCommentIsNullOrderByCreatedAtAsc(recipeId);

        return comments.stream()
                .map(comment -> mapCommentWithLike(comment, userIdx))
                .collect(Collectors.toList());
    }

    private RecipeCommentDto.Response mapCommentWithLike(RecipeComment comment, Integer userIdx) {
        RecipeCommentDto.Response dto = RecipeCommentDto.Response.fromEntity(comment);

        // 최상위 댓글 hasLiked 세팅
        dto.setHasLiked(likeService.hasUserLiked(userIdx, LikeTargetType.RECIPE_COMMENT, comment.getId()));

        // 자식 댓글도 재귀적으로 hasLiked 세팅
        if (dto.getChildren() != null) {
            dto.getChildren().forEach(childDto -> setChildHasLiked(childDto, userIdx));
        }

        return dto;
    }

    private void setChildHasLiked(RecipeCommentDto.Response dto, Integer userIdx) {
        dto.setHasLiked(likeService.hasUserLiked(userIdx, LikeTargetType.RECIPE_COMMENT, dto.getId()));
        if (dto.getChildren() != null) {
            dto.getChildren().forEach(child -> setChildHasLiked(child, userIdx));
        }
    }
}
