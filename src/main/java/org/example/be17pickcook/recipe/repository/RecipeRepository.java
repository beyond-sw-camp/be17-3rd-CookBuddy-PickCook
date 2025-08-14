package org.example.be17pickcook.recipe.repository;

import org.example.be17pickcook.recipe.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {}

