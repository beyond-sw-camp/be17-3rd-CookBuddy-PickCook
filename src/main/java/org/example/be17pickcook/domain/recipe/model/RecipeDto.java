package org.example.be17pickcook.domain.recipe.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.be17pickcook.domain.user.model.User;

import java.time.LocalDateTime;  // ✅ Date → LocalDateTime으로 변경
import java.util.List;

public class RecipeDto {

    @Getter
    @Builder
    @Schema(description = "레시피 등록 요청 DTO")
    public static class RecipeRequestDto { // 등록
        @Schema(description = "레시피 제목", example = "김치찌개")
        private String title;
        @Schema(description = "레시피에 대한 간단한 설명", example = "귀찮을 때 간단하게 만들어 먹기 좋은 김치찌개 레시피 알려드릴게요!")
        private String description;
        @Schema(description = "조리 방법", example = "끓이기")
        private String cooking_method;
        @Schema(description = "레시피 카테고리", example = "한식")
        private String category;
        @Schema(description = "소요시간", example = "20분")
        private String time_taken;
        @Schema(description = "난이도", example = "어려움/보통/쉬움")
        private String difficulty_level;
        @Schema(description = "인분/양", example = "2인분")
        private String serving_size;
        @Schema(description = "해시태그", example = "#매운 #한식")
        private String hashtags;
        @Schema(description = "작은 이미지 URL")
        private String image_small_url;
        @Schema(description = "큰 이미지 URL")
        private String image_large_url;
        @Schema(description = "팁/노하우")
        private String tip;

        @ArraySchema(schema = @Schema(implementation = RecipeStepDto.class), arraySchema = @Schema(description = "조리 단계 리스트"))
        private List<RecipeStepDto> steps;
        @ArraySchema(schema = @Schema(implementation = RecipeIngredientDto.class), arraySchema = @Schema(description = "재료 리스트"))
        private List<RecipeIngredientDto> ingredients;
        @Schema(description = "영양 정보")
        private RecipeNutritionDto nutrition;

        // DTO → Entity 변환 메서드
        public Recipe toEntity(User authUser) {
            Recipe recipe = Recipe.builder()
                    .title(this.title)
                    .description(this.description)
                    .cooking_method(this.cooking_method)
                    .category(this.category)
                    .time_taken(this.time_taken)
                    .difficulty_level(this.difficulty_level)
                    .serving_size(this.serving_size)
                    .hashtags(this.hashtags)
                    .tip(this.tip)
                    .image_small_url(this.image_small_url)
                    .image_large_url(this.image_large_url)
                    .user(authUser)
                    .build();

            // Step 엔티티 변환
            if (steps != null) {
                for (RecipeDto.RecipeStepDto stepDto : steps) {
                    RecipeStep stepEntity = stepDto.toEntity(recipe);
                    recipe.addSteps(stepEntity); // 하나씩 추가
                }
            }

            // Ingredient 엔티티 변환
            if (ingredients != null) {
                for (RecipeDto.RecipeIngredientDto ingDto : ingredients) {
                    RecipeIngredient ingEntity = ingDto.toEntity(recipe);
                    recipe.addIngredient(ingEntity); // 하나씩 추가
                }
            }

            // Nutrition 엔티티 변환
            if (nutrition != null) {
                RecipeNutrition nutritionEntity = nutrition.toEntity(recipe);
                recipe.addNutrition(nutritionEntity);
            }

            return recipe;
        }
    }

    @Getter
    @Builder
    @Schema(description = "레시피 조리 단계 DTO")
    public static class RecipeStepDto {
        @Schema(description = "단계 순서", example = "1")
        private Integer step_order;
        @Schema(description = "조리 단계 설명", example = "재료를 썰어줍니다.")
        private String description;
        @Schema(description = "단계 이미지 URL")
        private String image_url;

        public RecipeStep toEntity(Recipe recipe) {
            return RecipeStep.builder()
                    .step_order(step_order)
                    .description(description)
                    .image_url(image_url)
                    .recipe(recipe)
                    .build();
        }

        public static RecipeStepDto fromEntity(RecipeStep step) {
            return RecipeStepDto.builder()
                    .step_order(step.getStep_order())
                    .description(step.getDescription())
                    .image_url(step.getImage_url())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "레시피 재료 DTO")
    public static class RecipeIngredientDto {
        @Schema(description = "재료 이름", example = "김치")
        private String ingredient_name;
        @Schema(description = "재료 양", example = "200g")
        private String quantity;

        public RecipeIngredient toEntity(Recipe recipe) {
            return RecipeIngredient.builder()
                    .ingredient_name(this.ingredient_name)
                    .quantity(this.quantity)
                    .recipe(recipe)
                    .build();
        }

        public static RecipeIngredientDto fromEntity(RecipeIngredient ingredient) {
            return RecipeIngredientDto.builder()
                    .ingredient_name(ingredient.getIngredient_name())
                    .quantity(ingredient.getQuantity())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "레시피 영양 정보 DTO")
    public static class RecipeNutritionDto {
        @Schema(description = "칼로리(kcal)", example = "300")
        private Integer calories;
        @Schema(description = "탄수화물(g)", example = "40")
        private Integer carbs;
        @Schema(description = "단백질(g)", example = "20")
        private Integer protein;
        @Schema(description = "지방(g)", example = "10")
        private Integer fat;
        @Schema(description = "나트륨(mg)", example = "500")
        private Integer sodium;

        public RecipeNutrition toEntity(Recipe recipe) {
            return RecipeNutrition.builder()
                    .calories(this.calories)
                    .carbs(this.carbs)
                    .protein(this.protein)
                    .fat(this.fat)
                    .sodium(this.sodium)
                    .recipe(recipe)
                    .build();
        }

        public static RecipeNutritionDto fromEntity(RecipeNutrition nutrition) {
            return RecipeNutritionDto.builder()
                    .calories(nutrition.getCalories())
                    .carbs(nutrition.getCarbs())
                    .protein(nutrition.getProtein())
                    .fat(nutrition.getFat())
                    .sodium(nutrition.getSodium())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "레시피 응답 DTO")
    public static class RecipeResponseDto {
        @Schema(description = "레시피 ID", example = "1")
        private Long idx;
        @Schema(description = "레시피 제목", example = "김치찌개")
        private String title;
        @Schema(description = "레시피에 대한 간단한 설명", example = "귀찮을 때 간단하게 만들어 먹기 좋은 김치찌개 레시피 알려드릴게요!")
        private String description;
        @Schema(description = "조리 방법", example = "끓이기")
        private String cooking_method;
        @Schema(description = "레시피 카테고리", example = "한식")
        private String category;
        @Schema(description = "소요시간", example = "20분")
        private String time_taken;
        @Schema(description = "난이도", example = "어려움/보통/쉬움")
        private String difficulty_level;
        @Schema(description = "인분/양", example = "2인분")
        private String serving_size;
        @Schema(description = "해시태그", example = "#매운 #한식")
        private String hashtags;
        @Schema(description = "작은 이미지 URL")
        private String image_small_url;
        @Schema(description = "큰 이미지 URL")
        private String image_large_url;
        @Schema(description = "팁/노하우")
        private String tip;
        @Schema(description = "작성자 ID", example = "5")
        private Integer user_idx;
        @ArraySchema(schema = @Schema(implementation = RecipeStepDto.class), arraySchema = @Schema(description = "조리 단계 리스트"))
        private List<RecipeStepDto> steps;
        @ArraySchema(schema = @Schema(implementation = RecipeIngredientDto.class), arraySchema = @Schema(description = "재료 리스트"))
        private List<RecipeIngredientDto> ingredients;
        @Schema(description = "영양 정보")
        private RecipeNutritionDto nutrition;
        @Schema(description = "생성일")
        private LocalDateTime createdAt;        // ✅ LocalDateTime으로 변경
        @Schema(description = "수정일")
        private LocalDateTime updatedAt;        // ✅ LocalDateTime으로 변경
        @Schema(description = "좋아요 수", example = "12")
        private Integer likeCount;
        @Schema(description = "로그인 사용자가 좋아요를 눌렀는지 여부", example = "true")
        private Boolean likedByUser;

        // 좋아요 관련 값 세팅 메서드
        public void setLikeInfo(Integer likeCount, Boolean likedByUser) {
            this.likeCount = likeCount;
            this.likedByUser = likedByUser;
        }

        public static RecipeResponseDto fromEntity(Recipe recipe) {
            return RecipeResponseDto.builder()
                    .idx(recipe.getIdx())
                    .title(recipe.getTitle())
                    .description(recipe.getDescription())
                    .cooking_method(recipe.getCooking_method())
                    .category(recipe.getCategory())
                    .time_taken(recipe.getTime_taken())
                    .difficulty_level(recipe.getDifficulty_level())
                    .serving_size(recipe.getServing_size())
                    .hashtags(recipe.getHashtags())
                    .image_small_url(recipe.getImage_small_url())
                    .image_large_url(recipe.getImage_large_url())
                    .tip(recipe.getTip())
                    .user_idx(recipe.getUser() != null ? recipe.getUser().getIdx() : null)
                    .steps(recipe.getSteps() != null ? recipe.getSteps().stream()
                            .map(RecipeStepDto::fromEntity).toList() : null)
                    .ingredients(recipe.getIngredients() != null ? recipe.getIngredients().stream()
                            .map(RecipeIngredientDto::fromEntity).toList() : null)
                    .nutrition(recipe.getNutrition() != null ? RecipeNutritionDto.fromEntity(recipe.getNutrition()) : null)
                    .createdAt(recipe.getCreatedAt())    // ✅ 이제 타입이 일치함
                    .updatedAt(recipe.getUpdatedAt())    // ✅ 이제 타입이 일치함
                    .build();
        }
    }
}