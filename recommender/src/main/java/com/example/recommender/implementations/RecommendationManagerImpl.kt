package com.example.recommender.implementations

import com.example.recommender.implementations.ingredientMatcher.IngredientMatcher
import com.example.recommender.interfaces.RecommendationManager
import com.example.recommender.mlModels.RecommendModel
import com.softcat.database.facade.DatabaseFacade
import com.softcat.domain.entities.Ingredient
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.RecipeTag
import com.softcat.domain.entities.Score
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.toNDArray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import javax.inject.Inject

class RecommendationManagerImpl @Inject constructor(
    private val database: DatabaseFacade,
    private val mapper: RecipeMapper,
    private val matcher: IngredientMatcher
): RecommendationManager {

    @Volatile
    private var model: RecommendModel? = null

    private val mutex = Mutex()

    override suspend fun setUserScores(scores: List<Score>) {
        val newModel = buildRecommendModel(scores)
        mutex.withLock {
            model = newModel
        }
    }


    override suspend fun getRecommendation(
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): List<Recipe> {
        val recipeIds = getFilteredRecipeIds(ingredients, maxAbsentIngredients, tags)
        val (vectorIds, vectors) = readRecipeVectors(recipeIds)
        val model = mutex.withLock {
            model ?: return emptyList()
        }

        val scoreValues = model.predict(vectors).toList()
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

    private suspend fun buildRecommendModel(scores: List<Score>): RecommendModel {
        val scoredRecipeIds = scores.map { it.recipeId }
        val idToScore = scores.associate { it.recipeId to it.value }

        val (vectorIds, recipeVectors) = readRecipeVectors(scoredRecipeIds)
        val scoreValues = vectorIds.map {
            id -> (idToScore[id] ?: 3).toFloat()
        }.toNDArray()

        val model = RecommendModel.learn(recipeVectors, scoreValues)
        return model
    }

    private suspend fun assembleRecommendation(
        recipeIds: List<Int>,
        scores: List<Float>
    ): List<Recipe> {
        val recommendedIds = scores
            .zip(recipeIds)
            .filter { it.first >= 4f }
            .sortedByDescending { it.first }
            .map { it.second }

        val recipeModels = database.getRecipes(recommendedIds)
        val recipeMap = mapper.toEntities(recipeModels).associateBy { it.id }
        return recommendedIds.mapNotNull {
            recipeMap[it]
        }
    }

    private suspend fun getFilteredRecipeIds(
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): List<Int> {
        val recipes = mapper.toEntities(database.getRecipeSample(1000))
        return recipes.filter(ingredients, maxAbsentIngredients, tags).map {
            recipe -> recipe.id
        }
    }

    private fun List<Recipe>.filter(
        ingredients: List<Ingredient>,
        maxAbsentIngredients: Int,
        tags: List<RecipeTag>
    ): List<Recipe> {
        return filter { recipe ->
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
}