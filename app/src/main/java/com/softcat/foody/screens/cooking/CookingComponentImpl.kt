package com.softcat.foody.screens.cooking

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.softcat.domain.entities.Recipe
import com.softcat.foody.common.asValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import timber.log.Timber

class CookingComponentImpl @AssistedInject constructor(
    @Assisted("context") private val componentContext: ComponentContext,
    @Assisted("back") private val backClickCallback: () -> Unit,
    @Assisted("recipe") private val recipe: Recipe,
    private val storeFactory: CookingStoreFactory
): CookingComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(recipe) }

    override val model = store.asValue(lifecycle)

    override fun selectStep(step: Int) {
        Timber.i("${this::class.simpleName}.selectStep($step)")
        store.accept(CookingStore.Intent.SelectStep(step))
    }

    override fun increasePortions() {
        Timber.i("${this::class.simpleName}.increasePortions()")
        store.accept(CookingStore.Intent.IncreasePortions)
    }

    override fun decreasePortions() {
        Timber.i("${this::class.simpleName}.decreasePortions()")
        store.accept(CookingStore.Intent.DecreasePortions)
    }

    override fun back() {
        Timber.i("${this::class.simpleName}.back()")
        backClickCallback()
    }

    override fun changeFavouriteStatus() {
        Timber.i("${this::class.simpleName}.changeFavouriteStatus()")
        store.accept(CookingStore.Intent.ChangeIsFavourite)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("context") componentContext: ComponentContext,
            @Assisted("back") backClickCallback: () -> Unit,
            @Assisted("recipe") recipe: Recipe
        ): CookingComponentImpl
    }
}