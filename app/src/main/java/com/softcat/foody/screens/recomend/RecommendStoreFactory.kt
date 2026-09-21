package com.softcat.foody.screens.recomend

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.softcat.domain.entities.Ingredient
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.RecipeTag
import com.softcat.domain.entities.User
import com.softcat.domain.usecases.FavouritesUseCase
import com.softcat.domain.usecases.IngredientUseCase
import com.softcat.domain.usecases.RecipeTagUseCase
import com.softcat.domain.usecases.RecipeUseCase
import com.softcat.domain.usecases.ScoreUseCase
import com.softcat.domain.usecases.UserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class RecommendStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val recipeUseCase: RecipeUseCase,
        private val favouritesUseCase: FavouritesUseCase,
        private val userUseCase: UserUseCase,
        private val scoreUseCase: ScoreUseCase,
        private val ingredientUseCase: IngredientUseCase,
        private val tagUseCase: RecipeTagUseCase,
    ) {

        private var currentUser: User? = null


        fun create(): RecommendStore =
            object :
                RecommendStore,
                Store<RecommendStore.Intent, RecommendStore.State, RecommendStore.Label>
            by
                storeFactory.create(
                    name = this::class.simpleName,
                    initialState =
                        RecommendStore.State(
                            ingredients = emptyList(),
                            tags = emptyList(),
                            maxAbsentIngredients = 0,
                            resultStatus = RecommendStore.State.RecommendationStatus.Initial,
                            tagDialogState = RecommendStore.State.SelectTagDialogState.Hidden,
                        ),
                    executorFactory = { RecommendationsExecutor() },
                    reducer = RecommendReducer,
                    bootstrapper = RecommendBootstrapper()
                ) {}

    private inner class RecommendBootstrapper: CoroutineBootstrapper<Action>() {

        private var favouritesCollectingJob: Job? = null

        override fun invoke() {
            scope.launch(Dispatchers.IO) {
                ingredientUseCase.getAvailableIngredients().collect { ingredients ->
                    withContext(Dispatchers.Main) {
                        dispatch(Action.IngredientsUpdate(ingredients))
                    }
                }
            }
            scope.launch {
                userUseCase.observeLastEnteredUser().collect(::lastEnteredUserCollector)
            }
        }

        private fun lastEnteredUserCollector(newUser: User?) {
            currentUser = newUser
            favouritesCollectingJob?.cancel()
            val userId = newUser?.id

            favouritesCollectingJob = if (userId == null) {
                null
            } else {
                scope.launch(Dispatchers.IO) {
                    favouritesUseCase.observeFavouriteIds(userId).collect {
                        withContext(Dispatchers.Main) {
                            dispatch(Action.UpdateFavourites(it))
                        }
                    }
                }
            }
        }
    }

    sealed interface Action {
        data class IngredientsUpdate(val ingredients: List<Ingredient>): Action
        data class UpdateFavourites(val favouriteIds: Set<Int>): Action
    }

    sealed interface Msg {
        data class IngredientsUpdate(val ingredients: List<String>) : Msg
        data class TagsUpdate(val tags: List<String>) : Msg
        data class ChangeMaxAbsentIngredients(val newValue: Int) : Msg
        data class ChangeSearchTagQuery(val newValue: String) : Msg
        data object RecommendationLoading : Msg
        data object ShowAddRequiredTagDialog : Msg
        data object HideDialog : Msg
        data object Reset: Msg

        data class SearchTagResult(
            val query: String,
            val tags: List<RecipeTag>
        ): Msg

        data class RecommendationReady(val recipes: List<RecipeRecommendationModel>) : Msg
    }

    private inner class RecommendationsExecutor:
        CoroutineExecutor<RecommendStore.Intent, Action, RecommendStore.State, Msg, RecommendStore.Label>() {

        private var recommendation = emptyList<Recipe>()
        private var favouriteRecipesIds: Set<Int>? = null
        private val selectedTags = mutableListOf<RecipeTag>()
        private var selectedIngredients = listOf<Ingredient>()

        override fun executeAction(action: Action) {
            Timber.i("${this::class.simpleName}: Action is obtained: $action")
            when (action) {
                is Action.IngredientsUpdate -> {
                    selectedIngredients = action.ingredients
                    val ingredientNames = selectedIngredients.map { it.name }
                    dispatch(Msg.IngredientsUpdate(ingredientNames))
                }

                is Action.UpdateFavourites -> updateFavourites(action.favouriteIds)
            }
        }

        override fun executeIntent(intent: RecommendStore.Intent) {
            Timber.i("${this::class.simpleName}: Intent is obtained: $intent")
            when (intent) {
                is RecommendStore.Intent.AddTag -> {
                    if (intent.elem !in selectedTags) {
                        selectedTags.add(intent.elem)
                        val tags = selectedTags.map { it.name }
                        dispatch(Msg.TagsUpdate(tags))
                    }
                }

                is RecommendStore.Intent.RemoveIngredient -> {
                    val id = selectedIngredients.find { it.name == intent.name }?.id ?: return
                    scope.launch(Dispatchers.IO) {
                        ingredientUseCase.removeAvailableIngredient(id)
                    }
                }

                is RecommendStore.Intent.RemoveTag -> {
                    selectedTags.removeIf { it.name == intent.name }
                    val tags = selectedTags.map { it.name }
                    dispatch(Msg.TagsUpdate(tags))
                }

                is RecommendStore.Intent.OpenRecipeDetails -> {
                    val recipe = recommendation.find { it.id == intent.id } ?: return
                    publish(RecommendStore.Label.OpenRecipeDetails(recipe))
                }

                is RecommendStore.Intent.ChangeSearchTagQuery -> dispatch(Msg.ChangeSearchTagQuery(intent.vewValue))
                is RecommendStore.Intent.SearchTag -> searchTag(intent.query)
                is RecommendStore.Intent.ChangeFavouriteStatus -> changeFavouriteStatus(intent.recipeId)
                RecommendStore.Intent.Recommend -> makeRecipesRecommendation()
                RecommendStore.Intent.ShowAddRequiredTagDialog -> dispatch(Msg.ShowAddRequiredTagDialog)
                RecommendStore.Intent.HideDialog -> dispatch(Msg.HideDialog)
                is RecommendStore.Intent.ChangeMaxAbsentIngredients -> dispatch(Msg.ChangeMaxAbsentIngredients(intent.newValue))
            }
        }

        private fun changeFavouriteStatus(recipeId: Int) {
            val userId = currentUser?.id ?: return
            val content = state().resultStatus as? RecommendStore.State.RecommendationStatus.Content ?: return
            val recipeModel = content.recipes.find { it.id == recipeId } ?: return

            scope.launch(Dispatchers.IO) {
                if (recipeModel.isFavourite) {
                    favouritesUseCase.remove(userId, recipeId)
                } else {
                    favouritesUseCase.add(userId, recipeId)
                }
            }
        }

        private fun makeRecipesRecommendation() {
            val userId = currentUser?.id
            if (userId == null) {
                publish(RecommendStore.Label.Error(NoUserException()))
                return
            }
            dispatch(Msg.RecommendationLoading)
            scope.launch(Dispatchers.Default) {
                val scores = scoreUseCase.observe(userId).first()
                val result = recipeUseCase.recommend(
                    scores = scores,
                    ingredients = selectedIngredients,
                    maxAbsentIngredients = state().maxAbsentIngredients,
                    tags = selectedTags
                )
                result.onSuccess { recommendation ->
                    this@RecommendationsExecutor.recommendation = recommendation
                    val recipeModels = recommendation.map { recipe ->
                        RecipeRecommendationModel(
                            id = recipe.id,
                            name = recipe.name,
                            description = recipe.description,
                            isFavourite = favouriteRecipesIds?.let { recipe.id in it } ?: false,
                            isFavouriteVisible = favouriteRecipesIds != null,
                        )
                    }
                    withContext(Dispatchers.Main) {
                        dispatch(Msg.RecommendationReady(recipeModels))
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        publish(RecommendStore.Label.Error(it))
                        dispatch(Msg.Reset)
                    }
                }
            }
        }

        private fun searchTag(query: String) {
            scope.launch(Dispatchers.IO) {
                val result = tagUseCase.search(query)
                withContext(Dispatchers.Main) {
                    dispatch(Msg.SearchTagResult(query, result))
                }
            }
        }

        private fun updateFavourites(newFavouriteIds: Set<Int>) {
            favouriteRecipesIds = newFavouriteIds
            val status = state().resultStatus
                    as? RecommendStore.State.RecommendationStatus.Content ?: return
            val recipes = status.recipes.map { recipe ->
                recipe.copy(
                    isFavourite = favouriteRecipesIds?.let { recipe.id in it } ?: false,
                    isFavouriteVisible = favouriteRecipesIds != null,
                )
            }
            dispatch(Msg.RecommendationReady(recipes))
        }
    }

        private object RecommendReducer : Reducer<RecommendStore.State, Msg> {
            override fun RecommendStore.State.reduce(msg: Msg): RecommendStore.State {
                Timber.i("${this::class.simpleName}: Message is obtained: $msg")
                return when (msg) {
                    is Msg.ChangeMaxAbsentIngredients -> copy(maxAbsentIngredients = msg.newValue)
                    is Msg.RecommendationReady -> copy(resultStatus = RecommendStore.State.RecommendationStatus.Content(msg.recipes))
                    is Msg.IngredientsUpdate -> copy(ingredients = msg.ingredients)
                    is Msg.TagsUpdate -> copy(tags = msg.tags)
                    Msg.RecommendationLoading -> copy(resultStatus = RecommendStore.State.RecommendationStatus.Loading)
                    Msg.HideDialog -> copy(tagDialogState = RecommendStore.State.SelectTagDialogState.Hidden)
                    Msg.Reset -> copy(resultStatus = RecommendStore.State.RecommendationStatus.Initial)

                    Msg.ShowAddRequiredTagDialog -> copy(
                        tagDialogState = RecommendStore.State.SelectTagDialogState.Shown(
                            query = "",
                            searchResult = emptyList()
                        )
                    )

                    is Msg.SearchTagResult -> copy(
                        tagDialogState = RecommendStore.State.SelectTagDialogState.Shown(
                            query = msg.query,
                            searchResult = msg.tags
                        )
                    )

                    is Msg.ChangeSearchTagQuery -> {
                        if (tagDialogState is RecommendStore.State.SelectTagDialogState.Shown) {
                            copy(
                                tagDialogState = tagDialogState.copy(
                                    query = msg.newValue
                                )
                            )
                        } else
                            this
                    }
                }
            }
        }
    }
