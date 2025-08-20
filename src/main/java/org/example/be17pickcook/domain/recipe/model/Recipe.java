package org.example.be17pickcook.domain.recipe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Recipe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private String title;
    private String cooking_method;
    private String category;
    private String time_taken;
    private String difficulty_level;
    private String serving_size;
    private String hashtags;
    private String image_small_url;
    private String image_large_url;
    private String tip;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private RecipeNutrition nutrition;


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

    public void setImage_small_url(String url) { this.image_small_url = url; }
    public void setImage_large_url(String url) { this.image_large_url = url; }
}
