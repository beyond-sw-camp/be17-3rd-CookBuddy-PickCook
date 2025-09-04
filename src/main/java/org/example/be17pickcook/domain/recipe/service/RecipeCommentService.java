package org.example.be17pickcook.domain.recipe.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.service.S3UploadService;
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
    private final S3UploadService s3UploadService;

    @Transactional
    public RecipeCommentDto.Response addComment(RecipeCommentDto.Request request, MultipartFile image, int userId) throws SQLException, IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        RecipeComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        }

        RecipeComment comment = request.toEntity(user, recipe, parentComment);
        if (image != null && !image.isEmpty()){
            String imageUrl = s3UploadService.upload(image);
            comment.setImage(RecipeCommentImage.builder().recipeComment(comment).imageUrl(imageUrl).build());
        }
        RecipeComment savedComment = commentRepository.save(comment);

        return RecipeCommentDto.Response.fromEntity(savedComment);
    }

    @Transactional(readOnly = true)
    public List<RecipeCommentDto.Response> getComments(Long recipeId) {
        List<RecipeComment> comments = commentRepository.findByRecipeIdxAndParentCommentIsNullOrderByCreatedAtAsc(recipeId);
        return comments.stream()
                .map(RecipeCommentDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
}
