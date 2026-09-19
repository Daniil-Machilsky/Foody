package com.softcat.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class NutritionData(
    val calories: Float,
    val fat: Float,
    val protein: Float,
    val carbohydrates: Float,
)