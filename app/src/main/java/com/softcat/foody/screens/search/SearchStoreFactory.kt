package com.softcat.foody.screens.search

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
import com.softcat.domain.usecases.RecipeUseCase
import com.softcat.domain.usecases.UserUseCase
import com.softcat.foody.common.RecipeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SearchStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val favouritesUseCase: FavouritesUseCase,
    private val recipeUseCase: RecipeUseCase,
    private val ingredientUseCase: IngredientUseCase,
    private val tagUseCase: RecipeTagUseCase,
    private val userUseCase: UserUseCase,
) {
    private var user: User? = null

    fun create(lifecycle: Lifecycle): SearchStore =
        object:
            SearchStore, Store<SearchStore.Intent, SearchStore.State, SearchStore.Label>
        by
            storeFactory.create(
                name = this::class.simpleName,
                initialState = SearchStore.State(
                    searchQuery = "",
                    filtersState = SearchStore.State.FiltersSheetState(
                        filterParameters = FilterParams(),
                        suggestedTags = emptyList(),
                        suggestedIngredients = emptyList(),
                        expanded = false,
                    ),
                    searchStatus = SearchStore.State.SearchStatus.Initial
                ),
                executorFactory = { SearchExecutor(lifecycle) },
                reducer = SearchReducer,
                bootstrapper = SearchBootstrapper()
            ) {}

    private sealed interface Msg {
        data class ChangeSearchQuery(val newValue: String): Msg

        data class ChangeFilterParams(val params: FilterParams): Msg

        data class ChangeSearchContent(val recipes: List<RecipeModel>): Msg

        data class IngredientsSuggestionChanged(val ingredients: List<String>): Msg

        data class TagsSuggestionChanged(val tags: List<String>): Msg

        data object ExpandFiltersSheet: Msg

        data object HideFiltersSheet: Msg

        data object LoadingStarted: Msg
    }

    sealed interface Action {
        data class IngredientsLoaded(val ingredients: List<String>): Action

        data class TagsLoaded(val tags: List<String>): Action

        data class InitialRecipeSampleLoaded(val recipes: List<Recipe>): Action
    }

    private inner class SearchBootstrapper: CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch(Dispatchers.IO) {
                val ingredients = ingredientUseCase.getIngredients(1000).map { it.name }
                withContext(Dispatchers.Main) {
                    dispatch(Action.IngredientsLoaded(ingredients))
                }
                val tags = tagUseCase.getTags(1000).map { it.name }
                withContext(Dispatchers.Main) {
                    dispatch(Action.TagsLoaded(tags))
                }
                val initialSearchResult = recipeUseCase.search("")
                withContext(Dispatchers.Main) {
                    dispatch(Action.InitialRecipeSampleLoaded(initialSearchResult))
                }
            }
        }
    }

    private inner class SearchExecutor(
        lifecycle: Lifecycle
    ): CoroutineExecutor<SearchStore.Intent, Action, SearchStore.State, Msg, SearchStore.Label>() {

        private var savedSearchResult: List<Recipe>? = null
        private var favouriteIds: Set<Int>? = null

        private var userCollectingJob: Job? = null
        private var favouritesCollectingJob: Job? = null

        private var visibleIngredients: List<String> = emptyList()
        private var visibleTags: List<String> = emptyList()

        init {
            lifecycle.doOnStart {
                userCollectingJob = scope.launch {
                    userUseCase.observeLastEnteredUser().collect(::userCollector)
                }
                // Для того, чтобы фильтр по isCooked нормально применялся, нужно обновить рецепты.
                // Ведь статус isCooked мог поменяться.
                val query = state().searchQuery
                if (query.isNotEmpty())
                    search(query)
            }
            lifecycle.doOnStop {
                favouritesCollectingJob?.cancel()
                userCollectingJob?.cancel()

                favouritesCollectingJob = null
                userCollectingJob = null
            }
        }

        private fun userCollector(newUser: User?) {
            user = newUser
            favouritesCollectingJob?.cancel()
            val userId = user?.id
            updateFavourites(null)

            favouritesCollectingJob = if (userId == null) {
                null
            } else {
                scope.launch {
                    favouritesUseCase.observe(userId).collect { recipes ->
                        val favourites = recipes.map { it.id }.toSet()
                        updateFavourites(favourites)
                    }
                }
            }
        }

        override fun executeIntent(intent: SearchStore.Intent) {
            Timber.i("${this::class.simpleName}: Intent is obtained: $intent")
            when (intent) {
                is SearchStore.Intent.ChangeFavouriteStatus -> changeFavouriteStatus(intent.recipeId)
                is SearchStore.Intent.Search -> search(intent.query)
                is SearchStore.Intent.ChangeSearchQuery -> dispatch(Msg.ChangeSearchQuery(intent.newValue))
                is SearchStore.Intent.TagClicked -> tagClicked(intent.name)
                is SearchStore.Intent.IngredientClicked -> ingredientClicked(intent.name)
                SearchStore.Intent.ExpandFiltersSheet -> dispatch(Msg.ExpandFiltersSheet)
                SearchStore.Intent.HideFiltersSheet -> dispatch(Msg.HideFiltersSheet)
                SearchStore.Intent.ResetFilters -> updateFilterParams(FilterParams())

                is SearchStore.Intent.ChangeScore -> {
                    val params = state().filtersState.filterParameters.copy(minScore = intent.newValue)
                    updateFilterParams(params)
                }

                is SearchStore.Intent.ChangeCookingTime -> {
                    val params = state().filtersState.filterParameters.copy(duration = intent.newValue)
                    updateFilterParams(params)
                }

                is SearchStore.Intent.ChangeCalories -> {
                    val params = state().filtersState.filterParameters.copy(calories = intent.newValue)
                    updateFilterParams(params)
                }

                is SearchStore.Intent.ChangeIsCooked -> {
                    val params = state().filtersState.filterParameters.copy(isCooked = intent.newValue)
                    updateFilterParams(params)
                }

                is SearchStore.Intent.OpenRecipeDetails -> {
                    val recipe = savedSearchResult?.find { it.id == intent.recipeId }
                    recipe?.let { publish(SearchStore.Label.OpenRecipeDetails(it)) }
                }
            }
        }

        private fun updateFilterParams(params: FilterParams) {
            dispatch(Msg.ChangeFilterParams(params))
            val recipes = mapToRecipeModels(savedSearchResult, favouriteIds, params)
            if (state().searchStatus !is SearchStore.State.SearchStatus.Initial)
                dispatch(Msg.ChangeSearchContent(recipes))
        }

        private fun tagClicked(name: String) {
            val suggested = state().filtersState.suggestedTags.toMutableList()
            val selected = state().filtersState.filterParameters.tags.toMutableList()
            val tag = visibleTags.find { it == name } ?: return
            if (tag in selected) {
                selected.remove(tag)
                suggested.add(0, tag)
            } else {
                selected.add(tag)
                suggested.remove(tag)
            }
            val params = state().filtersState.filterParameters.copy(tags = selected)
            dispatch(Msg.TagsSuggestionChanged(suggested))
            updateFilterParams(params)
        }

        private fun ingredientClicked(name: String) {
            val suggested = state().filtersState.suggestedIngredients.toMutableList()
            val selected = state().filtersState.filterParameters.ingredients.toMutableList()
            val ingredient = visibleIngredients.find { it == name } ?: return
            if (ingredient in selected) {
                selected.remove(ingredient)
                suggested.add(0, ingredient)
            }
            else {
                selected.add(ingredient)
                suggested.remove(ingredient)
            }
            val params = state().filtersState.filterParameters.copy(ingredients = selected)
            dispatch(Msg.IngredientsSuggestionChanged(suggested))
            updateFilterParams(params)
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
                is Action.InitialRecipeSampleLoaded ->
                    initialRecipeSampleLoaded(action.recipes)
            }
        }

        private fun initialRecipeSampleLoaded(recipes: List<Recipe>) {
            if (state().searchStatus is SearchStore.State.SearchStatus.Initial) {
                savedSearchResult = recipes
                val recipesModels = mapToRecipeModels(
                    recipes,
                    favouriteIds,
                    state().filtersState.filterParameters
                )
                dispatch(Msg.ChangeSearchContent(recipesModels))
            }
        }

        private fun search(query: String) {
            dispatch(Msg.LoadingStarted)
            scope.launch(Dispatchers.IO) {
                val searchResult = recipeUseCase.search(query)
                savedSearchResult = searchResult

                withContext(Dispatchers.Main) {
                    val recipes = mapToRecipeModels(
                        recipes = searchResult,
                        favourites = favouriteIds,
                        filterParams = state().filtersState.filterParameters
                    )
                    dispatch(Msg.ChangeSearchContent(recipes))
                }
            }
        }

        private fun changeFavouriteStatus(recipeId: Int) {
            val userId = user?.id ?: return
            val content = state().searchStatus as? SearchStore.State.SearchStatus.Content ?: return
            val recipeModel = content.recipes.find { it.id == recipeId } ?: return

            scope.launch(Dispatchers.IO) {
                if (recipeModel.isFavourite) {
                    favouritesUseCase.remove(userId, recipeId)
                } else {
                    favouritesUseCase.add(userId, recipeId)
                }
            }
        }

        private fun updateFavourites(favourites: Set<Int>?) {
            favouriteIds = favourites
            if (state().searchStatus is SearchStore.State.SearchStatus.Content) {
                val recipes = mapToRecipeModels(
                    recipes = savedSearchResult.orEmpty(),
                    favourites = favourites,
                    filterParams = state().filtersState.filterParameters
                )
                dispatch(Msg.ChangeSearchContent(recipes))
            }
        }
    }

    private object SearchReducer: Reducer<SearchStore.State, Msg> {

        override fun SearchStore.State.reduce(msg: Msg): SearchStore.State {
            Timber.i("${this::class.simpleName}: Message is obtained: $msg")
            return when (msg) {
                is Msg.ChangeSearchQuery -> copy(searchQuery = msg.newValue)

                is Msg.ChangeFilterParams ->
                    copy(filtersState = filtersState.copy(filterParameters = msg.params))

                is Msg.ChangeSearchContent -> {
                    if (msg.recipes.isEmpty())
                        copy(searchStatus = SearchStore.State.SearchStatus.Empty)
                    else
                        copy(searchStatus = SearchStore.State.SearchStatus.Content(msg.recipes))
                }

                is Msg.IngredientsSuggestionChanged ->
                    copy(filtersState = filtersState.copy(suggestedIngredients = msg.ingredients))

                is Msg.TagsSuggestionChanged ->
                    copy(filtersState = filtersState.copy(suggestedTags = msg.tags))

                Msg.LoadingStarted -> copy(searchStatus = SearchStore.State.SearchStatus.Loading)

                Msg.ExpandFiltersSheet -> copy(filtersState = filtersState.copy(expanded = true))

                Msg.HideFiltersSheet -> copy(filtersState = filtersState.copy(expanded = false))
            }
        }
    }

    fun mapToRecipeModels(
        recipes: List<Recipe>?,
        favourites: Set<Int>?,
        filterParams: FilterParams
    ): List<RecipeModel> {
        return recipes
            .orEmpty()
            .filter(filterParams)
            .map { recipe ->
                RecipeModel(
                    id = recipe.id,
                    favouriteButtonVisible = favourites != null,
                    isFavourite = favourites?.contains(recipe.id) ?: false,
                    ingredients = recipe.ingredients.map { it.name },
                    name = recipe.name,
                    description = recipe.description,
                    score = "%.2f".format(recipe.avgScore),
                    scoreVisible = true
                )
            }
    }
}