package com.softcat.foody.screens.details

import com.arkivanov.mvikotlin.core.store.Store
import com.softcat.domain.entities.Recipe

interface DetailsStore: Store<DetailsStore.Intent, DetailsStore.State, Nothing> {

    sealed interface Intent {
        data object ChangeFavouriteStatus: Intent
        data class UpdateScore(val newValue: Int): Intent
        data object RemoveScore: Intent
        data object NextStep: Intent
        data object PreviousStep: Intent
    }

    data class State(
        val recipe: Recipe,
        val stepNumber: Int,
        val isScoreVisible: Boolean,
        val score: Int,
        val isFavourite: Boolean,
        val isFavouriteVisible: Boolean
    )
}