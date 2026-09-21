package com.softcat.foody.screens.favourites

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.softcat.data.filter
import com.softcat.domain.entities.FilterParams
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.User
import com.softcat.domain.usecases.FavouritesUseCase
import com.softcat.domain.usecases.IngredientUseCase
import com.softcat.domain.usecases.RecipeTagUseCase
import com.softcat.domain.usecases.UserUseCase
import com.softcat.foody.common.RecipeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class FavouritesStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val favouritesUseCase: FavouritesUseCase,
    private val userUseCase: UserUseCase,
    private val ingredientUseCase: IngredientUseCase,
    private val tagsUseCase: RecipeTagUseCase
) {
    fun create(lifecycle: Lifecycle): FavouritesStore = object:
        FavouritesStore, Store<FavouritesStore.Intent, FavouritesStore.State, FavouritesStore.Label> by
        storeFactory.create(
            name = this::class.simpleName,
            initialState = FavouritesStore.State(
                contentStatus = FavouritesStore.State.ContentStatus.Loading,
                filtersStatus = FavouritesStore.State.FiltersSheetState(
                    filterParameters = FilterParams(),
                    expanded = false,
                    suggestedTags = emptyList(),
                    suggestedIngredients = emptyList(),
                ),
            ),
            bootstrapper = FavouriteBootstrapper(),
            executorFactory = { FavouriteExecutor(lifecycle) },
            reducer = FavouriteReducer
        ) {}

    private var user: User? = null

    private inner class FavouriteBootstrapper: CoroutineBootstrapper<Action>() {

        override fun invoke() {
            scope.launch(Dispatchers.IO) {
                val ingredients = ingredientUseCase.getIngredients(1000).map { it.name }
                withContext(Dispatchers.Main) {
                    dispatch(Action.IngredientsLoaded(ingredients))
                }
                val tags = tagsUseCase.getTags(1000).map { it.name }
                withContext(Dispatchers.Main) {
                    dispatch(Action.TagsLoaded(tags))
                }
            }
        }
    }

    private sealed interface Action {
        data class TagsLoaded(val tags: List<String>): Action

        data class IngredientsLoaded(val ingredients: List<String>): Action
    }

    private inner class FavouriteExecutor(
        lifecycle: Lifecycle
    ): CoroutineExecutor<FavouritesStore.Intent, Action, FavouritesStore.State, Msg, FavouritesStore.Label>() {

        private var favourites: List<Recipe>? = null

        private var favouritesCollectingJob: Job? = null
        private var userCollectingJob: Job? = null

        private var visibleIngredients: List<String> = emptyList()
        private var visibleTags: List<String> = emptyList()

        init {
            lifecycle.doOnStart {
                userCollectingJob = scope.launch {
                    userUseCase.observeLastEnteredUser().collect(::lastEnteredUserCollector)
                }
            }
            lifecycle.doOnStop {
                favouritesCollectingJob?.cancel()
                userCollectingJob?.cancel()

                favouritesCollectingJob = null
                userCollectingJob = null
            }
        }

        override fun executeAction(action: Action) {
            Timber.i("${this::class.simpleName}: Action is obtained: $action")
            when (action) {
                is Action.IngredientsLoaded -> {
                    visibleIngredients = action.ingredients
                    dispatch(Msg.IngredientsSuggestionChanged(action.ingredients))
                }
                is Action.TagsLoaded -> {
                    visibleTags = action.tags
                    dispatch(Msg.TagsSuggestionChanged(action.tags))
                }
            }
        }

        override fun executeIntent(intent: FavouritesStore.Intent) {
            Timber.i("${this::class.simpleName}: Intent is obtained: $intent")
            when (intent) {
                is FavouritesStore.Intent.RemoveFromFavourites -> user?.id?.let { userId ->
                    scope.launch(Dispatchers.IO) {
                        favouritesUseCase.remove(userId, intent.recipeId)
                    }
                }

                is FavouritesStore.Intent.ChangeScore -> {
                    val params = state().filtersStatus.filterParameters.copy(minScore = intent.newValue)
                    updateFilterParams(params)
                }

                is FavouritesStore.Intent.ChangeCookingTime -> {
                    val params = state().filtersStatus.filterParameters.copy(duration = intent.newValue)
                    updateFilterParams(params)
                }

                is FavouritesStore.Intent.ChangeCalories -> {
                    val params = state().filtersStatus.filterParameters.copy(calories = intent.newValue)
                    updateFilterParams(params)
                }

                is FavouritesStore.Intent.ChangeIsCooked -> {
                    val params = state().filtersStatus.filterParameters.copy(isCooked = intent.newValue)
                    updateFilterParams(params)
                }

                FavouritesStore.Intent.ExpandFiltersSheet -> dispatch(Msg.ExpandFiltersSheet)

                FavouritesStore.Intent.HideFiltersSheet -> dispatch(Msg.HideFiltersSheet)

                is FavouritesStore.Intent.TagClicked -> tagClicked(intent.name)

                is FavouritesStore.Intent.IngredientClicked -> ingredientClicked(intent.name)

                FavouritesStore.Intent.ResetFilters -> updateFilterParams(FilterParams())

                is FavouritesStore.Intent.OpenRecipeDetails -> {
                    val recipe = favourites?.find { it.id == intent.recipeId }
                    recipe?.let { publish(FavouritesStore.Label.OpenRecipeDetails(it)) }
                }
            }
        }

        private suspend fun lastEnteredUserCollector(newUser: User?) {
            user = newUser
            val userId = user?.id
            favouritesCollectingJob?.cancel()

            if (userId == null) {
                withContext(Dispatchers.Main) {
                    dispatch(Msg.UserIsAbsent)
                }
                favouritesCollectingJob = null
            } else {
                favouritesCollectingJob = scope.launch(Dispatchers.IO) {
                    favouritesUseCase.observe(userId).collect {
                        withContext(Dispatchers.Main) { updateFavourites(it) }
                    }
                }
            }
        }

        private fun updateFilterParams(params: FilterParams) {
            dispatch(Msg.UpdateFilterParams(params))
            val recipes = mapToRecipeModels(favourites, params)
            dispatch(Msg.FavouritesLoaded(recipes))
        }

        private fun tagClicked(name: String) {
            val suggested = state().filtersStatus.suggestedTags.toMutableList()
            val selected = state().filtersStatus.filterParameters.tags.toMutableList()
            val tag = visibleTags.find { it == name } ?: return
            if (tag in selected) {
                selected.remove(tag)
                suggested.add(0, tag)
            } else {
                selected.add(tag)
                suggested.remove(tag)
            }
            val params = state().filtersStatus.filterParameters.copy(tags = selected)
            dispatch(Msg.TagsSuggestionChanged(suggested))
            updateFilterParams(params)
        }

        private fun ingredientClicked(name: String) {
            val suggested = state().filtersStatus.suggestedIngredients.toMutableList()
            val selected = state().filtersStatus.filterParameters.ingredients.toMutableList()
            val ingredient = visibleIngredients.find { it == name } ?: return
            if (ingredient in selected) {
                selected.remove(ingredient)
                suggested.add(0, ingredient)
            }
            else {
                selected.add(ingredient)
                suggested.remove(ingredient)
            }
            val params = state().filtersStatus.filterParameters.copy(ingredients = selected)
            dispatch(Msg.IngredientsSuggestionChanged(suggested))
            updateFilterParams(params)
        }

        private fun updateFavourites(recipes: List<Recipe>) {
            favourites = recipes
            if (recipes.isEmpty()) {
                dispatch(Msg.FavouritesIsEmpty)
            } else {
                val recipes = mapToRecipeModels(
                    recipes = favourites.orEmpty(),
                    filterParams = state().filtersStatus.filterParameters
                )
                dispatch(Msg.FavouritesLoaded(recipes))
            }
        }
    }

    private object FavouriteReducer: Reducer<FavouritesStore.State, Msg> {
        override fun FavouritesStore.State.reduce(msg: Msg): FavouritesStore.State {
            Timber.i("${this::class.simpleName}: Message is obtained: $msg")
            return when (msg) {
                is Msg.UpdateFilterParams ->
                    copy(filtersStatus = filtersStatus.copy(filterParameters = msg.params))

                is Msg.IngredientsSuggestionChanged -> {
                    copy(
                        filtersStatus = filtersStatus.copy(suggestedIngredients = msg.ingredients)
                    )
                }

                is Msg.TagsSuggestionChanged -> {
                    copy(
                        filtersStatus = filtersStatus.copy(suggestedTags = msg.tags)
                    )
                }

                Msg.ExpandFiltersSheet -> copy(filtersStatus = filtersStatus.copy(expanded = true))

                Msg.HideFiltersSheet -> copy(filtersStatus = filtersStatus.copy(expanded = false))

                Msg.UserIsAbsent -> copy(contentStatus = FavouritesStore.State.ContentStatus.UserIsAbsent)

                Msg.FavouritesIsEmpty -> copy(contentStatus = FavouritesStore.State.ContentStatus.Empty)

                is Msg.FavouritesLoaded ->
                    copy(contentStatus = FavouritesStore.State.ContentStatus.RecipeList(msg.recipes))
            }
        }
    }

    private sealed interface Msg {
        data class UpdateFilterParams(val params: FilterParams): Msg

        data class FavouritesLoaded(val recipes: List<RecipeModel>): Msg

        data class TagsSuggestionChanged(val tags: List<String>): Msg

        data class IngredientsSuggestionChanged(val ingredients: List<String>): Msg

        data object ExpandFiltersSheet: Msg

        data object HideFiltersSheet: Msg

        data object UserIsAbsent: Msg

        data object FavouritesIsEmpty: Msg
    }

    fun mapToRecipeModels(
        recipes: List<Recipe>?,
        filterParams: FilterParams
    ): List<RecipeModel> {
        return recipes
            .orEmpty()
            .filter(filterParams)
            .map { recipe ->
                RecipeModel(
                    id = recipe.id,
                    favouriteButtonVisible = true,
                    isFavourite = true,
                    ingredients = recipe.ingredients.map { it.name },
                    name = recipe.name,
                    description = recipe.description,
                    score = "%.2f".format(recipe.avgScore),
                    scoreVisible = true
                )
            }
    }
}