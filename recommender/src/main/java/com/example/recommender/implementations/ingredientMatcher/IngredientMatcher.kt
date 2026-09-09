package com.example.recommender.implementations.ingredientMatcher

interface IngredientMatcher {
    fun equal(ingredient1: String,ingredient2: String): Boolean
}