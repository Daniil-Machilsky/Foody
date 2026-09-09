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
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.stack
import javax.inject.Inject

class RecommendationManagerImpl @Inject constructor(
    private val database: DatabaseFacade,
    private val mapper: RecipeMapper,
    private val matcher: IngredientMatcher
): RecommendationManager {

    override suspend fun getRecommendation(
        scores: List<Score>,
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): List<Recipe> {
        val recipeIds = getFilteredRecipeIds(ingredients, maxAbsentIngredients, tags)
        if (recipeIds.isEmpty())
            return emptyList()
        val (vectorIds, recipeVectors) = readRecipeVectors(recipeIds)
        val scoreValues = applyRecommendModel(vectorIds, recipeVectors, scores)
        val recipes = assembleRecommendation(vectorIds, scoreValues)
        return recipes
    }

    private suspend fun readRecipeVectors(recipeIds: List<Int>): Pair<List<Int>, D2Array<Float>> {
        val (vectorIds, recipeVectors) = database.getRecipeVectors(recipeIds)
        val n = recipeVectors.size
        val m = recipeVectors.first().vector.size

        val flatArray = FloatArray(n * m)
        for (i in 0 until n) {
            val vector = recipeVectors[i].vector
            for (j in 0 until m) {
                flatArray[i * m + j] = vector[j]
            }
        }
        val recipeMatrix = mk.ndarray(flatArray, n, m)
        return vectorIds to recipeMatrix
    }

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

    private suspend fun getFilteredRecipeIds(
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): List<Int> {
        val recipes = mapper.toEntities(database.getRecipeSample(1000))
        return filter(recipes, ingredients, maxAbsentIngredients, tags).map {
            it.id
        }
    }

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

    private fun getLearnData(
        recipeIds: List<Int>,
        recipes: D2Array<Float>,
        scores: List<Score>
    ): Pair<D2Array<Float>, D1Array<Float>> {
        val scoredVectors = mutableListOf<MultiArray<Float, D1>>()
        val validScores = mutableListOf<Float>()
        val recipeIdToIndex = recipeIds.withIndex().associate { it.value to it.index }

        for (score in scores) {
            val index = recipeIdToIndex[score.recipeId]
            if (index != null) {
                scoredVectors.add(recipes[index])
                validScores.add(score.value.toFloat())
            }
        }

        // Если scoredVectors пустой, то будет исключение.
        // Это позволяет уведомить, что рекомендация невозможна.
        val recipeMatrix = mk.stack(scoredVectors, axis = 0)
        val scoreValues = mk.ndarray(validScores.toFloatArray())
        return recipeMatrix to scoreValues
    }
}