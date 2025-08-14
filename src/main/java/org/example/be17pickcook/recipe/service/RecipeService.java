package org.example.be17pickcook.recipe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.recipe.model.*;
import org.example.be17pickcook.recipe.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final S3UploadService s3UploadService;

    // 기본 이미지
    private static final String DEFAULT_SMALL_IMAGE = "https://example.com/default-small.jpg";
    private static final String DEFAULT_LARGE_IMAGE = "https://example.com/default-large.jpg";
    private static final String DEFAULT_STEP_IMAGE  = "https://example.com/default-step.jpg";


    // 레시피 등록
    @Transactional
    public void register(UserDto.AuthUser authUser,
                         RecipeDto.RecipeRequestDto dto,
                         List<MultipartFile> files) throws SQLException, IOException {

        // 대표 이미지 업로드 (첫 2장은 대표 이미지 small, large)
        String imageSmallUrl = (files.size() > 0 && !files.get(0).isEmpty()) ?
                s3UploadService.upload(files.get(0)) : DEFAULT_SMALL_IMAGE;

        String imageLargeUrl = (files.size() > 1 && !files.get(1).isEmpty()) ?
                s3UploadService.upload(files.get(1)) : DEFAULT_LARGE_IMAGE;


        // 기본 Recipe 엔티티 생성
        Recipe recipe = dto.toEntity(User.builder().idx(authUser.getIdx()).build());

        // 대표 이미지 적용
        recipe.setImage_small_url(imageSmallUrl);
        recipe.setImage_large_url(imageLargeUrl);

        // Recipe 먼저 저장
        recipe = recipeRepository.save(recipe);

        // Steps 매핑 및 이미지 업로드
        if (dto.getSteps() != null) {
            for (int i = 0; i < dto.getSteps().size(); i++) {
                RecipeDto.RecipeStepDto stepDto = dto.getSteps().get(i);
                String stepImageUrl = (files.size() > i + 2 && !files.get(i + 2).isEmpty()) ?
                        s3UploadService.upload(files.get(i + 2)) : DEFAULT_STEP_IMAGE;

                RecipeStep step = stepDto.toEntity(recipe);
                step.setImage_url(stepImageUrl);
                recipe.addSteps(step); // 편의 메서드로 연관관계 설정
            }
        }

        // 식재료 매핑
        if (dto.getIngredients() != null) {
            for (RecipeDto.RecipeIngredientDto ingDto : dto.getIngredients()) {
                RecipeIngredient ingredient = ingDto.toEntity(recipe);
                recipe.addIngredient(ingredient);
            }
        }

        recipeRepository.save(recipe);
    }



    // 특정 레시피 조회
    public RecipeDto.RecipeResponseDto getRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다. id=" + recipeId));

        return RecipeDto.RecipeResponseDto.fromEntity(recipe);
    }

    // 레시피 전체 목록 조회
    public List<RecipeDto.RecipeResponseDto> getRecipeList() {
        List<Recipe> recipes = recipeRepository.findAll();
        return recipes.stream()
                .map(RecipeDto.RecipeResponseDto::fromEntity)
                .toList();
    }

}
