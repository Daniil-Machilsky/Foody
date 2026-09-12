package com.softcat.foody.screens.cooking

import com.arkivanov.mvikotlin.core.store.Store

interface CookingStore: Store<CookingStore.Intent, CookingStore.State, CookingStore.Label> {

    data class State(
        val content: StepContent,
        val stepNumber: Int,
        val stepCount: Int,
        val imageUrl: String
    ) {
        data class IngredientDescription(
            val name: String,
            val quantity: String,
            val units: String
        )

        sealed interface StepContent {
            data class Prepare(
                val ingredients: List<IngredientDescription>,
                val portions: Int
            ): StepContent

            data class Instruction(
                val text: String
            ): StepContent
        }
    }

    sealed interface Intent {}

    sealed interface Label {}
}