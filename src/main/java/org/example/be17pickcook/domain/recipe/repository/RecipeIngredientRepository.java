package org.example.be17pickcook.domain.recipe.repository;

import org.example.be17pickcook.domain.recipe.model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    @Query(value = "SELECT ri.recipe_id, ri.ingredient_name FROM recipe_ingredient ri", nativeQuery = true)
    List<Object[]> findAllRecipeIngredients();
}
