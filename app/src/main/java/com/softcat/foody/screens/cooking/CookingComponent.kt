package com.softcat.foody.screens.cooking

import com.arkivanov.decompose.value.Value

interface CookingComponent {

    val model: Value<CookingStore.State>

    fun selectStep(step: Int)

    fun increasePortions()

    fun decreasePortions()

    fun back()

    fun changeFavouriteStatus()

    fun changeIsCookedStatus()
}