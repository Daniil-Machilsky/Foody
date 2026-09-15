package com.softcat.foody.screens.details

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.User
import com.softcat.domain.usecases.FavouritesUseCase
import com.softcat.domain.usecases.ScoreUseCase
import com.softcat.domain.usecases.UserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class DetailsStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val favouritesUseCase: FavouritesUseCase,
    private val userUseCase: UserUseCase,
    private val scoreUseCase: ScoreUseCase,
) {

    fun create(recipe: Recipe, lifecycle: Lifecycle): DetailsStore =
        object: DetailsStore, Store<DetailsStore.Intent, DetailsStore.State, Nothing> by
        storeFactory.create(
            name = this::class.simpleName,
            initialState = DetailsStore.State(
                recipe = recipe,
                stepNumber = 1,
                isScoreVisible = false,
                score = 0,
                isFavourite = false,
                isFavouriteVisible = false
            ),
            executorFactory = { DetailsExecutor(recipe.id, lifecycle) },
            reducer = DetailsReducer
        ) {}

    sealed interface Msg {
        data class SetIsFavourite(val newValue: Boolean): Msg

        data class SetScore(val newValue: Int): Msg

        data object NextStep: Msg
        data object PreviousStep: Msg
        data object UserIsLost: Msg
    }

    private inner class DetailsExecutor(
        private val recipeId: Int,
        lifecycle: Lifecycle
    ): CoroutineExecutor<DetailsStore.Intent, Nothing, DetailsStore.State, Msg, Nothing>() {

        private var userId: String? = null

        private var favouritesCollectingJob: Job? = null
        private var scoreCollectJob: Job? = null
        private var userCollectJob: Job? = null

        init {
            lifecycle.doOnStart {
                userCollectJob = scope.launch {
                    userUseCase.observeLastEnteredUser().collect(::userCollector)
                }
            }
            lifecycle.doOnStop {
                favouritesCollectingJob?.cancel()
                scoreCollectJob?.cancel()
                userCollectJob?.cancel()

                favouritesCollectingJob = null
                scoreCollectJob = null
                userCollectJob = null
            }
        }

        private fun userCollector(user: User?) {
            userId = user?.id
            favouritesCollectingJob?.cancel()
            scoreCollectJob?.cancel()

            val currentUserId = userId
            if (currentUserId == null) {
                favouritesCollectingJob = null
                scoreCollectJob = null
                dispatch(Msg.UserIsLost)
            } else {
                favouritesCollectingJob = scope.launch(Dispatchers.IO) {
                    favouritesUseCase.observeIsFavourite(currentUserId, recipeId).collect {
                        withContext(Dispatchers.Main) {
                            dispatch(Msg.SetIsFavourite(it))
                        }
                    }
                }
                scoreCollectJob = scope.launch(Dispatchers.IO) {
                    scoreUseCase.observeScoreValue(currentUserId, recipeId).collect {
                        withContext(Dispatchers.Main) {
                            dispatch(Msg.SetScore(it))
                        }
                    }
                }
            }
        }

        override fun executeIntent(intent: DetailsStore.Intent) {
            Timber.i("${this::class.simpleName}: Intent is obtained: $intent")
            when (intent) {

                DetailsStore.Intent.ChangeFavouriteStatus -> updateIsFavourite()

                DetailsStore.Intent.NextStep -> {
                    if (state().stepNumber < state().recipe.steps.size)
                        dispatch(Msg.NextStep)
                }

                DetailsStore.Intent.PreviousStep -> {
                    if (state().stepNumber > 1)
                        dispatch(Msg.PreviousStep)
                }

                is DetailsStore.Intent.UpdateScore -> userId?.let {
                    scope.launch(Dispatchers.IO) {
                        scoreUseCase.save(it, recipeId, intent.newValue)
                    }
                }

                DetailsStore.Intent.RemoveScore -> userId?.let {
                    scope.launch(Dispatchers.IO) {
                        scoreUseCase.remove(it, recipeId)
                    }
                }
            }
        }

        private fun updateIsFavourite() {
            val id = userId ?: return
            if (!state().isFavouriteVisible)
                return

            scope.launch(Dispatchers.IO) {
                if (state().isFavourite) {
                    favouritesUseCase.remove(id, recipeId)
                } else {
                    favouritesUseCase.add(id, recipeId)
                }
            }
        }
    }

    private object DetailsReducer: Reducer<DetailsStore.State, Msg> {

        override fun DetailsStore.State.reduce(msg: Msg): DetailsStore.State {
            Timber.i("${this::class.simpleName}: Message is obtained: $msg")

            return when (msg) {
                is Msg.SetIsFavourite -> copy(isFavourite = msg.newValue, isFavouriteVisible = true)
                is Msg.SetScore -> copy(isScoreVisible = true, score = msg.newValue)
                Msg.NextStep -> copy(stepNumber = stepNumber + 1)
                Msg.PreviousStep -> copy(stepNumber = stepNumber - 1)
                Msg.UserIsLost -> copy(isScoreVisible = false, isFavouriteVisible = false)
            }
        }
    }
}