package org.example.be17pickcook.domain.recipe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String title;
    private String description;
    private String cooking_method;
    private String category;
    private String time_taken;
    private String difficulty_level;
    private String serving_size;
    private String hashtags;
    private String image_small_url;
    private String image_large_url;
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
