package org.example.be17pickcook.domain.recipe.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.likes.model.LikeCountable;
import org.example.be17pickcook.domain.scrap.model.ScrapCountable;
import org.example.be17pickcook.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Recipe extends BaseEntity implements LikeCountable, ScrapCountable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Setter
    private String title;
    @Setter
    private String description;
    @Setter
    private String cooking_method;
    @Setter
    private String category;
    @Setter
    private String time_taken;
    @Setter
    private String difficulty_level;
    @Setter
    private String serving_size;
    @Setter
    private String hashtags;
    @Setter
    private String image_small_url;
    @Setter
    private String image_large_url;
    @Setter
    private String tip;

    // 반정규화 적용 (기본값 0 보장)
    private Long likeCount = 0L;
    private Long scrapCount = 0L;
    private Long commentCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeStep> steps = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private RecipeNutrition nutrition;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeComment> comments = new ArrayList<>();




    // ================== 연관관계 메서드 ==================
    public void addSteps(RecipeStep step) {
        if (this.steps == null) this.steps = new ArrayList<>();

        this.steps.add(step);
        step.setRecipe(this);
    }


    public void addNutrition(RecipeNutrition nutrition) {
        this.nutrition =  nutrition;
        nutrition.setRecipe(this);
    }

    public void addIngredient(RecipeIngredient ingredient) {
        if (this.ingredients == null) this.ingredients = new ArrayList<>();

        this.ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void clearIngredients() {
        ingredients.clear(); // 기존 데이터 삭제
    }

    public void clearSteps() {
        steps.clear();
    }


    // ================== 유틸 메서드 ==================
    public void setImage_small_url(String url) { this.image_small_url = url; }
    public void setImage_large_url(String url) { this.image_large_url = url; }

    // 반정규화 필드 제어 메서드
    @Override
    public Long getIdxLike() { return this.idx; }
    @Override
    public Long getLikeCount() { return this.likeCount; }
    @Override
    public void increaseLike() {
        if (likeCount == null) {
            likeCount = 0L;
        }
        likeCount++;
    }
    @Override
    public void decreaseLike() {
        if (likeCount == null || likeCount <= 0) {
            likeCount = 0L;
        } else {
            likeCount--;
        }
    }


    @Override
    public Long getIdxScrap() { return this.idx; }
    @Override
    public Long getScrapCount() { return this.scrapCount; }
    @Override
    public void increaseScrap() {
        if (scrapCount == null) {
            scrapCount = 0L;
        }
        scrapCount++;
    }
    @Override
    public void decreaseScrap() {
        if (scrapCount == null || scrapCount <= 0) {
            scrapCount = 0L;
        } else {
            scrapCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
