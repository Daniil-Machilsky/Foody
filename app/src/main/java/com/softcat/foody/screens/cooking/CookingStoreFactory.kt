package com.softcat.foody.screens.cooking

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.Recipe.IngredientUnit.*
import com.softcat.domain.entities.User
import com.softcat.domain.usecases.FavouritesUseCase
import com.softcat.domain.usecases.UserUseCase
import com.softcat.foody.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CookingStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val favouritesUseCase: FavouritesUseCase,
    private val userUseCase: UserUseCase
) {
    fun create(recipe: Recipe): CookingStore =
        object:
            CookingStore, Store<CookingStore.Intent, CookingStore.State, Nothing>
        by storeFactory.create(
            name = this::class.simpleName,
            initialState = initialState(recipe),
            executorFactory = { CookingExecutor(recipe.id) },
            reducer = CookingReducer(recipe),
            bootstrapper = CookingBootstrapper(recipe.id)
        ) {}

    private sealed interface Action {
        data class IsFavouriteUpdated(
            val newUserId: String?,
            val isFavourite: Boolean,
            val isFavouriteVisible: Boolean
        ): Action
    }

    private inner class CookingBootstrapper(
        private val recipeId: Int
    ): CoroutineBootstrapper<Action>() {

        private var collectFavouritesJob: Job? = null

        override fun invoke() {
            scope.launch(Dispatchers.IO) {
                userUseCase.observeLastEnteredUser().collect(::userCollector)
            }
        }

        private suspend fun userCollector(user: User?) {
            collectFavouritesJob?.cancel()
            val userId = user?.id
            if (userId == null) {
                withContext(Dispatchers.Main) {
                    dispatch(
                        Action.IsFavouriteUpdated(
                            newUserId = null,
                            isFavourite = false,
                            isFavouriteVisible = false
                        )
                    )
                }
            } else {
                collectFavouritesJob = scope.launch(Dispatchers.IO) {
                    favouritesUseCase.observeIsFavourite(userId, recipeId).collect {
                        withContext(Dispatchers.Main) {
                            dispatch(
                                Action.IsFavouriteUpdated(
                                    newUserId = userId,
                                    isFavourite = it,
                                    isFavouriteVisible = true
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private inner class CookingExecutor(
        private val recipeId: Int
    ) : CoroutineExecutor<CookingStore.Intent, Action, CookingStore.State, Msg, Nothing>() {

        private var userId: String? = null

        override fun executeAction(action: Action) {
            Timber.i("${this::class.simpleName}.executeAction($action)")
            when (action) {
                is Action.IsFavouriteUpdated -> {
                    userId = action.newUserId
                    dispatch(Msg.IsFavouriteUpdated(action.isFavourite, action.isFavouriteVisible))
                }
            }
        }

        override fun executeIntent(intent: CookingStore.Intent) {
            Timber.i("${this::class.simpleName}.executeIntent($intent)")
            when (intent) {
                CookingStore.Intent.DecreasePortions -> decreasePortions()
                CookingStore.Intent.IncreasePortions -> increasePortions()
                is CookingStore.Intent.SelectStep -> dispatch(Msg.SetStep(intent.step))
                CookingStore.Intent.ChangeIsFavourite -> changeIsFavourite()
            }
        }

        private fun increasePortions() {
            val content = state().content
            if (
                content is CookingStore.State.StepContent.Prepare &&
                content.portions < MAX_PORTIONS
            ) {
                dispatch(Msg.SetPortions(content.portions + 1))
            }
        }

        private fun decreasePortions() {
            val content = state().content
            if (
                content is CookingStore.State.StepContent.Prepare &&
                content.portions > MIN_PORTIONS
            ) {
                dispatch(Msg.SetPortions(content.portions - 1))
            }
        }

        private fun changeIsFavourite() {
            if (!state().isFavouriteVisible)
                return
            userId?.let { id ->
                val isFavourite = state().isFavourite
                scope.launch(Dispatchers.IO) {
                    if (isFavourite)
                        favouritesUseCase.remove(id, recipeId)
                    else
                        favouritesUseCase.add(id, recipeId)
                }
            }
        }
    }

    private inner class CookingReducer(
        private val recipe: Recipe
    ): Reducer<CookingStore.State, Msg> {

        override fun CookingStore.State.reduce(msg: Msg): CookingStore.State {
            Timber.i("${this::class.simpleName}: Message is obtained: $msg")

            return when (msg) {
                is Msg.SetPortions -> {
                    recipeToPrepareState(recipe, msg.count)
                }

                is Msg.SetStep -> {
                    if (msg.step == 0)
                        recipeToPrepareState(recipe, 1)
                    else
                        recipeToStepContent(recipe, msg.step)
                }

                is Msg.IsFavouriteUpdated -> {
                    copy(
                        isFavourite = msg.isFavourite,
                        isFavouriteVisible = msg.isFavouriteVisible
                    )
                }
            }
        }

        private fun CookingStore.State.recipeToPrepareState(recipe: Recipe, portions: Int) = copy(
            content = CookingStore.State.StepContent.Prepare(
                ingredients = recipe.ingredients.mapIndexed { index, ingredient ->
                    CookingStore.State.IngredientDescription(
                        id = ingredient.id,
                        name = ingredient.name,
                        quantity = "%.2f".format(recipe.ingredientQuantity[index] * portions),
                        unitsResId = recipe.ingredientUnits[index].toLabelResId(),
                    )
                },
                portions = portions
            ),
            stepNumber = 0,
            stepCount = recipe.steps.size,
            imageUrl = recipe.imageUrl
        )

        private fun CookingStore.State.recipeToStepContent(recipe: Recipe, stepNumber: Int) = copy(
            content = CookingStore.State.StepContent.Instruction(
                text = recipe.steps[stepNumber - 1]
            ),
            stepNumber = stepNumber,
            stepCount = recipe.steps.size,
            imageUrl = recipe.stepImages[stepNumber - 1]
        )
    }

    private sealed interface Msg {
        data class SetStep(val step: Int): Msg
        data class SetPortions(val count: Int): Msg
        data class IsFavouriteUpdated(
            val isFavourite: Boolean,
            val isFavouriteVisible: Boolean
        ): Msg
    }

    private fun Recipe.IngredientUnit.toLabelResId() = when (this) {
        Piece -> R.string.unit_piece
        Milliliter -> R.string.unit_milliliter
        Liter -> R.string.unit_liter
        Gram -> R.string.unit_gram
        Kilo -> R.string.unit_kilo
    }

    private fun initialState(recipe: Recipe) = CookingStore.State(
        content = CookingStore.State.StepContent.Prepare(
            ingredients = recipe.ingredients.mapIndexed { index, ingredient ->
                CookingStore.State.IngredientDescription(
                    id = ingredient.id,
                    name = ingredient.name,
                    quantity = "%.2f".format(recipe.ingredientQuantity[index]),
                    unitsResId = recipe.ingredientUnits[index].toLabelResId(),
                )
            },
            portions = 1
        ),
        stepNumber = 0,
        stepCount = recipe.steps.size,
        imageUrl = recipe.imageUrl,
        isFavourite = false,
        isFavouriteVisible = false,
    )

    companion object {
        private const val MIN_PORTIONS = 1
        private const val MAX_PORTIONS = 100
    }
}