package org.example.be17pickcook.domain.recipe.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.recipe.model.RecipeCommentDto;
import org.example.be17pickcook.domain.recipe.repository.RecipeRepository;
import org.example.be17pickcook.domain.recipe.service.RecipeCommentRepository;
import org.example.be17pickcook.domain.recipe.service.RecipeCommentService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipe/comment")
@Tag(name = "레시피 기능", description = "레시피 등록, 조회, 목록 조회 기능을 제공합니다.")
public class RecipeCommentController {
    private final RecipeCommentService recipeCommentService;

    @PostMapping()
    public ResponseEntity register(@AuthenticationPrincipal UserDto.AuthUser authUser, RecipeCommentDto.Request dto){
        return null;
    }
}
