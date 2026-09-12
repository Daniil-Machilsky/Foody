package com.softcat.foody.screens.cooking

import kotlinx.coroutines.flow.StateFlow

interface CookingComponent {

    val model: StateFlow<CookingStore.State>

    fun selectStep(step: Int)

    fun increasePortions()

    fun decreasePortions()

    fun back()
}