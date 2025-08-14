package org.example.be17pickcook.domain.recipe.model;

import lombok.Builder;
import lombok.Getter;
import org.example.be17pickcook.domain.user.model.User;

import java.util.Date;
import java.util.List;

public class RecipeDto {

    @Getter
    @Builder
    public static class RecipeRequestDto { // 등록
        private String title;
        private String cooking_method;
        private String category;
        private String serving_size;
        private String hashtags;
        private String image_small_url;
        private String image_large_url;
        private String tip;

        private List<RecipeStepDto> steps;
        private List<RecipeIngredientDto> ingredients;
        private RecipeNutritionDto nutrition;


        // DTO → Entity 변환 메서드
        public Recipe toEntity(User authUser) {
            Recipe recipe = Recipe.builder()
                    .title(this.title)
                    .cooking_method(this.cooking_method)
                    .category(this.category)
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
    public static class RecipeStepDto {
        private Integer step_order;
        private String description;
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
    public static class RecipeIngredientDto {
        private String ingredient_name;
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
    public static class RecipeNutritionDto {
        private Integer calories;
        private Integer carbs;
        private Integer protein;
        private Integer fat;
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
    public static class RecipeResponseDto {
        private Long idx;
        private String title;
        private String cooking_method;
        private String category;
        private String serving_size;
        private String hashtags;
        private String image_small_url;
        private String image_large_url;
        private String tip;
        private Integer user_idx;
        private List<RecipeStepDto> steps;
        private List<RecipeIngredientDto> ingredients;
        private RecipeNutritionDto nutrition;
        private Date createdAt;
        private Date updatedAt;

        public static RecipeResponseDto fromEntity(Recipe recipe) {
            return RecipeResponseDto.builder()
                    .idx(recipe.getIdx())
                    .title(recipe.getTitle())
                    .cooking_method(recipe.getCooking_method())
                    .category(recipe.getCategory())
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
                    .createdAt(recipe.getCreatedAt())
                    .updatedAt(recipe.getUpdatedAt())
                    .build();
        }
    }
}
