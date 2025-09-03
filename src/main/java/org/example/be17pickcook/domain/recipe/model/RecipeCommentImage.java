package org.example.be17pickcook.domain.recipe.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.common.BaseEntity;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recipe_comment_images")
public class RecipeCommentImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_comment_id")
    private RecipeComment recipeComment;
}
