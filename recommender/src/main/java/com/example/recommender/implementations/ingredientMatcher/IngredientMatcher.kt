package com.example.recommender.implementations.ingredientMatcher

import com.softcat.domain.entities.Ingredient

interface IngredientMatcher {
    fun equal(ingredient1: Ingredient, ingredient2: Ingredient): Boolean
}