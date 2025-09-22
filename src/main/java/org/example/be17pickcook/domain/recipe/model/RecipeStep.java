package org.example.be17pickcook.domain.recipe.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Setter
    private Integer step_order;
    @Setter
    private String description;
    @Setter
    private String image_url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public void setImage_url(String url) { this.image_url = url; }
}
