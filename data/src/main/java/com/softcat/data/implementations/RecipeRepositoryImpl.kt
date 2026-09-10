package com.softcat.data.implementations

import com.example.recommender.implementations.RecipeMapper
import com.example.recommender.interfaces.RecommendationManager
import com.softcat.database.facade.DatabaseFacade
import com.softcat.domain.entities.Ingredient
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.RecipeTag
import com.softcat.domain.entities.Score
import com.softcat.domain.interfaces.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val database: DatabaseFacade,
    private val recipeMapper: RecipeMapper,
    private val recommender: RecommendationManager
): RecipeRepository {

    private val isCookedFlow = MutableStateFlow(false)
    private var selectedRecipe: Int? = null

    override suspend fun search(query: String): List<Recipe> {
        return recipeMapper.toEntities(database.searchRecipe(query, RECIPE_LIMIT))
    }

    override suspend fun getRecipeSample(): List<Recipe> {
        return recipeMapper.toEntities(database.getRecipeSample(RECIPE_LIMIT))
    }

    override suspend fun recommend(
        scores: List<Score>,
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): Result<List<Recipe>> {
        return try {
            recommender.setUserScores(scores)
            val recipes = recommender.getRecommendation(
                ingredients,
                maxAbsentIngredients,
                tags
            )
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun get(recipeIds: List<Int>): List<Recipe> {
        return recipeMapper.toEntities(database.getRecipes(recipeIds))
    }

    override suspend fun setIsCooked(recipeId: Int, value: Boolean): Result<Unit> {
        val result = database.setRecipeIsCooked(recipeId, value)
        // Если наблюдаем состояние isCooked данного рецепта, то уведомляем об обновлении.
        selectedRecipe?.let {
            isCookedFlow.value = database.isRecipeCooked(it)
        }
        return result
    }

    override suspend fun observeIsCooked(recipeId: Int): StateFlow<Boolean> {
        selectedRecipe = recipeId
        isCookedFlow.value = database.isRecipeCooked(recipeId)
        return isCookedFlow
    }

    companion object {
        private const val RECIPE_LIMIT = 300
    }
}