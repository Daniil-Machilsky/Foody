package com.example.recommender.interfaces

import com.softcat.domain.entities.Ingredient
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.RecipeTag
import com.softcat.domain.entities.Score

interface RecommendationManager {

    suspend fun setUserScores(scores: List<Score>)

    suspend fun getRecommendation(
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): List<Recipe>
}