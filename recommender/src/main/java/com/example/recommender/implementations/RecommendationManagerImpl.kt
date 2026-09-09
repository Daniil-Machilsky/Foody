package com.example.recommender.implementations

import com.example.recommender.implementations.ingredientMatcher.IngredientMatcher
import com.example.recommender.interfaces.RecommendationManager
import com.example.recommender.mlModels.RecommendModel
import com.softcat.database.facade.DatabaseFacade
import com.softcat.domain.entities.Ingredient
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.RecipeTag
import com.softcat.domain.entities.Score
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.stack
import javax.inject.Inject

class RecommendationManagerImpl @Inject constructor(
    private val database: DatabaseFacade,
    private val mapper: RecipeMapper,
    private val matcher: IngredientMatcher
): RecommendationManager {

    // IDEF уровня 0
    override suspend fun getRecommendation(
        scores: List<Score>,
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): List<Recipe> {
        val (recipeIds, recipeVectors) = readRecipes()
        val scoreValues = applyRecommendModel(recipeIds, recipeVectors, scores)
        val recipes = assembleRecommendation(recipeIds, scoreValues)
        return filter(recipes, ingredients, maxAbsentIngredients, tags)
    }

    // IDEF уровня 1 блок 1
    private suspend fun readRecipes(): Pair<List<Int>, D2Array<Float>> {
        val recipeVectors = database.getRecipeVectors()
        val n = recipeVectors.size
        val m = recipeVectors.first().vector.size
        val numbers = List(n * m) {
            recipeVectors[it / m].vector[it % m]
        }
        val recipeMatrix = mk.ndarray(numbers, n, m)
        val recipeIds = recipeVectors.map { it.id }
        return recipeIds to recipeMatrix
    }

    // IDEF уровня 1, блок 2
    private fun applyRecommendModel(
        recipeIds: List<Int>,
        recipes: D2Array<Float>,
        scores: List<Score>
    ): List<Float> {
        val (scoredRecipes, scoreValues) = getLearnData(recipeIds, recipes, scores)
        val model = RecommendModel.learn(scoredRecipes, scoreValues)
        val otherScores = model.predict(recipes)
        return otherScores.data.toList()
    }

    // IDEF уровня 1, блок 3
    private suspend fun assembleRecommendation(
        recipeIds: List<Int>,
        scores: List<Float>
    ): List<Recipe> {
        val pairs = scores
            .zip(recipeIds)
            .filter { it.first >= 4f }
            .sortedByDescending { it.first }

        val recipeModels = database.getRecipes(recipeIds)
        val recipeMap = mapper.toEntities(recipeModels)
            .associateBy({ it.id }, { it })

        return pairs.mapNotNull {
            recipeMap[it.second]
        }
    }

    // IDEF уровня 1, блок 4
    private fun filter(
        recipes: List<Recipe>,
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): List<Recipe> {
        return recipes.filter { recipe ->
            tags.forEach {
                if (!recipe.tags.contains(it))
                    return@filter false
            }
            var missIngredient = 0
            var i = 0
            while (i < recipe.ingredients.size && missIngredient <= maxAbsentIngredients) {
                val ingredientName = recipe.ingredients[i].name
                val match = ingredients.any {
                    matcher.equal(ingredientName, it.name)
                }
                if (!match)
                    ++missIngredient
                ++i
            }
            missIngredient <= maxAbsentIngredients
        }
    }

    // IDEF уровня 2, блок 2.1
    private fun getLearnData(
        recipeIds: List<Int>,
        recipes: D2Array<Float>,
        scores: List<Score>
    ): Pair<D2Array<Float>, D1Array<Float>> {
        val numbers = scores.map { it.value.toFloat() }
        val scoreValues = mk.ndarray(numbers)
        val scoredVectors = scores.mapNotNull { score ->
            val index = recipeIds.indexOfFirst { it == score.recipeId }
            if (index == -1)
                null
            else
                recipes[index]
        }
        val recipeMatrix = mk.stack(scoredVectors, axis = 0)
        return recipeMatrix to scoreValues
    }
}