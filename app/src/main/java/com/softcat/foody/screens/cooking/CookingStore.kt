package com.softcat.foody.screens.cooking

import com.arkivanov.mvikotlin.core.store.Store

interface CookingStore: Store<CookingStore.Intent, CookingStore.State, Nothing> {

    data class State(
        val content: StepContent,
        val stepNumber: Int,
        val stepCount: Int,
        val imageUrl: String,
        val isFavourite: Boolean,
        val isFavouriteVisible: Boolean
    ) {
        data class IngredientDescription(
            val id: Int,
            val name: String,
            val quantity: String,
            val unitsResId: Int
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

    sealed interface Intent {
        data class SelectStep(val step: Int) : Intent
        data object IncreasePortions : Intent
        data object DecreasePortions : Intent
        data object ChangeIsFavourite: Intent
    }
}