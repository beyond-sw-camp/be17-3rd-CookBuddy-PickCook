package org.example.be17pickcook.domain.recipe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.recipe.model.RecipeDto;
import org.example.be17pickcook.domain.recipe.service.RecipeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipe")
@Tag(name = "레시피 기능", description = "레시피 등록, 조회, 목록 조회 기능을 제공합니다.")
public class RecipeController {
    private final RecipeService recipeService;

    @Operation(
            summary = "레시피 등록",
            description = "사용자가 새로운 레시피를 등록합니다.\n" +
                    "- 레시피 정보는 RecipeRequestDto로 전달\n" +
                    "- 이미지 파일은 optional로 MultipartFile 리스트 형태로 전달 가능"
    )
    @PostMapping(value="/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity register(@AuthenticationPrincipal UserDto.AuthUser authUser,
                                   @RequestPart RecipeDto.RecipeRequestDto dto,
                                   @RequestPart(value = "files", required = false)List<MultipartFile> files) throws SQLException, IOException {
        recipeService.register(authUser, dto, files);

        return ResponseEntity.status(200).body("레시피 작성 성공");
    }

    // 특정 레시피 조회
    @Operation(
            summary = "특정 레시피 조회",
            description = "레시피 ID를 기반으로 레시피 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto.RecipeResponseDto> getRecipe(@AuthenticationPrincipal UserDto.AuthUser authUser, @PathVariable Long id) {
        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;
        return ResponseEntity.ok(recipeService.getRecipe(id, userIdx));
    }

    // 레시피 목록 조회
    @Operation(
            summary = "레시피 목록 조회",
            description = "등록된 모든 레시피 목록을 조회합니다."
    )
    @GetMapping
    public BaseResponse<List<RecipeDto.RecipeResponseDto>> getRecipeList(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;

        return BaseResponse.success(recipeService.getRecipeList(userIdx));
    }
}
