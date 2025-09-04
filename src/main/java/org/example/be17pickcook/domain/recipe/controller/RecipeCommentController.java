package org.example.be17pickcook.domain.recipe.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.recipe.model.RecipeCommentDto;
import org.example.be17pickcook.domain.recipe.service.RecipeCommentService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipe/comment")
@Tag(name = "레시피 댓글 기능", description = "레시피 댓글 등록, 댓글 목록 조회 기능을 제공합니다.")
public class RecipeCommentController {
    private final RecipeCommentService commentService;

    @PostMapping
    public BaseResponse<RecipeCommentDto.Response> addComment(
            @RequestPart("data") RecipeCommentDto.Request request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDto.AuthUser authUser) throws SQLException, IOException {
        request.setRecipeId(request.getRecipeId());
        return BaseResponse.success(commentService.addComment(request, image, authUser.getIdx()));
    }

    @GetMapping
    public BaseResponse<List<RecipeCommentDto.Response>> getComments(@RequestParam Long recipeId) {
        return BaseResponse.success(commentService.getComments(recipeId));
    }
}
