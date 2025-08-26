package org.example.be17pickcook.domain.recipe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RecipeNutrition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private Integer calories;
    private Integer carbs;
    private Integer protein;
    private Integer fat;
    private Integer sodium;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
}
