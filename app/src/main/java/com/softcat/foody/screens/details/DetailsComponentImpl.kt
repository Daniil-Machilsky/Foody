package com.softcat.foody.screens.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.softcat.domain.entities.Recipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class DetailsComponentImpl @AssistedInject constructor(
    private val storeFactory: DetailsStoreFactory,
    @Assisted("context") componentContext: ComponentContext,
    @Assisted("recipe") recipe: Recipe,
    @Assisted("context") private val onBackClicked: () -> Unit,
    @Assisted("openCookingRecipe") private val openCookingRecipeCallback: (Recipe) -> Unit
): DetailsComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(recipe, componentContext.lifecycle) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<DetailsStore.State> = store.stateFlow

    override fun changeFavouriteStatus() {
        Timber.i("${this::class.simpleName}: changeFavouriteStatus()")
        store.accept(DetailsStore.Intent.ChangeFavouriteStatus)
    }

    override fun updateScore(newValue: Int) {
        Timber.i("${this::class.simpleName}: updateScore()")
        store.accept(DetailsStore.Intent.UpdateScore(newValue))
    }

    override fun deleteScore() {
        Timber.i("${this::class.simpleName}: deleteScore()")
        store.accept(DetailsStore.Intent.RemoveScore)
    }

    override fun changeIsCooked() {
        Timber.i("${this::class.simpleName}: changeIsCooked()")
        store.accept(DetailsStore.Intent.ChangeIsCooked)
    }

    override fun nextStep() {
        Timber.i("${this::class.simpleName}: nextStep()")
        store.accept(DetailsStore.Intent.NextStep)
    }

    override fun previousStep() {
        Timber.i("${this::class.simpleName}: previousStep()")
        store.accept(DetailsStore.Intent.PreviousStep)
    }

    override fun back() {
        Timber.i("${this::class.simpleName}: back()")
        onBackClicked()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("context") componentContext: ComponentContext,
            @Assisted("context") onBackClicked: () -> Unit,
            @Assisted("recipe") recipe: Recipe,
            @Assisted("openCookingRecipe") openCookingRecipeCallback: (Recipe) -> Unit
        ): DetailsComponentImpl
    }
}