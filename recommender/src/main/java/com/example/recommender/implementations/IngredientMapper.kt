package com.example.recommender.implementations

import com.softcat.database.models.IngredientDbModel
import com.softcat.domain.entities.Ingredient
import com.softcat.domain.entities.IngredientCategory

fun IngredientDbModel.toEntity() = Ingredient(
    id = id,
    name = name,
    category = category.toIngredientCategory(),
)

fun Int.toIngredientCategory() = when (this) {
    1 -> IngredientCategory.Crops
    2 -> IngredientCategory.Dairy
    3 -> IngredientCategory.MeatAndFish
    4 -> IngredientCategory.Sweet
    5 -> IngredientCategory.SpiceAndSauce
    6 -> IngredientCategory.FruitAndVegetables
    else -> IngredientCategory.Other
}