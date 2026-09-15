package com.example.recommender.implementations

import com.softcat.database.facade.DatabaseFacade
import com.softcat.database.models.RecipeDbModel
import com.softcat.domain.entities.Ingredient
import com.softcat.domain.entities.NutritionData
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.RecipeTag
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

class RecipeMapper @Inject constructor(
    private val database: DatabaseFacade
) {
    private var ingredientMap: Map<Int, Ingredient> = emptyMap()
    private var tagMap: Map<Int, RecipeTag> = emptyMap()

    @Volatile
    private var initialized: Boolean = false
    private val mutex = Mutex()

    suspend fun toEntities(recipeModels: List<RecipeDbModel>): List<Recipe> {
        if (!initialized)
            initialize()

        return recipeModels.map { model ->
            val ingredients = model.ingredients
                .split(",")
                .mapNotNull { id -> ingredientMap[id.toInt()] }

            val tags = model.tags
                .split(",")
                .mapNotNull { id -> tagMap[id.toInt()] }

            val steps = model.steps.split("|")
            Recipe(
                id = model.id,
                name = model.name,
                description = model.description,
                isCooked = model.isCooked,
                ingredients = ingredients,
                tags = tags,
                steps = steps,
                minutes = model.minutes,
                nutrition = NutritionData(
                    calories = model.calories,
                    fat = model.fat,
                    sugar = model.sugar,
                    sodium = model.sodium,
                    protein = model.protein,
                    saturatedFat = model.saturatedFat,
                    carbohydrates = model.carbohydrates
                ),
                imageUrl = "",
                stepImages = List(steps.size) { "" },
                ingredientQuantity = List(ingredients.size) { 1f },
                ingredientUnits = List(ingredients.size) { Recipe.IngredientUnit.Piece },
            )
        }
    }

    private suspend fun initialize() {
        mutex.lock()
        if (initialized) {
            mutex.unlock()
            return
        }
        ingredientMap = database
            .getIngredients(limit = 10000)
            .map { it.toEntity() }
            .associateBy { ingredient -> ingredient.id }
        tagMap = database
            .getTags(limit = 10000)
            .associate({it.id to RecipeTag(it.name) })
        initialized = true
        mutex.unlock()
    }
}