package org.example.be17pickcook.domain.recipe.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    @Setter
    private String ingredient_name;
    @Setter
    private String quantity;
    @Setter
    private Boolean isMainIngredient;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
}
