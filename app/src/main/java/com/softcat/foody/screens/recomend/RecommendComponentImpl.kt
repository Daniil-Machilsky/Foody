package com.softcat.foody.screens.recomend

import android.app.Application
import android.widget.Toast
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.RecipeTag
import com.softcat.domain.exceptions.NoScoresException
import com.softcat.foody.R
import com.softcat.foody.common.asValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber

class RecommendComponentImpl @AssistedInject constructor(
    private val storeFactory: RecommendStoreFactory,
    private val application: Application,
    @Assisted("context") componentContext: ComponentContext,
    @Assisted("open_recipe") private val openRecipeDetailsCallback: (Recipe) -> Unit,
    @Assisted("open_fridge") private val openFridgeCallback: () -> Unit,
): RecommendComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }
    private val scope = CoroutineScope(Dispatchers.Main)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: Value<RecommendStore.State> = store.asValue(lifecycle)

    init {
        scope.launch {
            store.labels.collect(::labelCollector)
        }
    }

    private fun labelCollector(label: RecommendStore.Label) {
        when (label) {
            is RecommendStore.Label.OpenRecipeDetails -> openRecipeDetailsCallback(label.recipe)
            is RecommendStore.Label.Error -> {
                val msg = when (label.error) {
                    is NoScoresException -> application.getString(R.string.need_two_scores_error)
                    is NoUserException -> application.getString(R.string.no_user_error)
                    else -> "Error: " + label.error.message.orEmpty()
                }
                Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun changeMaxAbsentIngredients(newValue: Int) {
        Timber.i("${this::class.simpleName}: changeMaxAbsentIngredients()")
        store.accept(RecommendStore.Intent.ChangeMaxAbsentIngredients(newValue))
    }

    override fun removeIngredient(name: String) {
        Timber.i("${this::class.simpleName}: removeIngredient()")
        store.accept(RecommendStore.Intent.RemoveIngredient(name))
    }

    override fun addTag(tag: RecipeTag) {
        Timber.i("${this::class.simpleName}: addTag()")
        store.accept(RecommendStore.Intent.AddTag(tag))
    }

    override fun removeTag(name: String) {
        Timber.i("${this::class.simpleName}: removeTag()")
        store.accept(RecommendStore.Intent.RemoveTag(name))
    }

    override fun recommend() {
        Timber.i("${this::class.simpleName}: recommend()")
        store.accept(RecommendStore.Intent.Recommend)
    }

    override fun showAddTagDialog() {
        Timber.i("${this::class.simpleName}: showAddTagDialog()")
        store.accept(RecommendStore.Intent.ShowAddRequiredTagDialog)
    }

    override fun hideDialog() {
        Timber.i("${this::class.simpleName}: hideDialog()")
        store.accept(RecommendStore.Intent.HideDialog)
    }

    override fun openRecipeDetails(recipeId: Int) {
        Timber.i("${this::class.simpleName}: openRecipeDetails($recipeId)")
        store.accept(RecommendStore.Intent.OpenRecipeDetails(recipeId))
    }

    override fun openFridge() {
        Timber.i("${this::class.simpleName}: openFridge()")
        openFridgeCallback()
    }

    override fun changeFavouriteStatus(recipeId: Int) {
        Timber.i("${this::class.simpleName}: changeFavouriteStatus($recipeId)")
        store.accept(RecommendStore.Intent.ChangeFavouriteStatus(recipeId))
    }

    override fun searchTags(query: String) {
        Timber.i("${this::class.simpleName}: searchTags($query)")
        store.accept(RecommendStore.Intent.SearchTag(query))
    }

    override fun changeSearchTagQuery(newValue: String) {
        Timber.i("${this::class.simpleName}: changeSearchTagQuery($newValue)")
        store.accept(RecommendStore.Intent.ChangeSearchTagQuery(newValue))
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("context") componentContext: ComponentContext,
            @Assisted("open_recipe") openRecipeDetailsCallback: (Recipe) -> Unit,
            @Assisted("open_fridge") openFridgeCallback: () -> Unit,
        ): RecommendComponentImpl
    }
}