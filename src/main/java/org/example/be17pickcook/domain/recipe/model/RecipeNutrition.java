package org.example.be17pickcook.domain.recipe.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RecipeNutrition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Setter
    private Integer calories;
    @Setter
    private Integer carbs;
    @Setter
    private Integer protein;
    @Setter
    private Integer fat;
    @Setter
    private Integer sodium;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
}
