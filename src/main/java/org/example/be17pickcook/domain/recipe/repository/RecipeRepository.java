package org.example.be17pickcook.domain.recipe.repository;

import org.example.be17pickcook.domain.recipe.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {}

