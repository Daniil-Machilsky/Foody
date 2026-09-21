package com.softcat.foody.screens.initialization

import android.app.Application
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.softcat.domain.usecases.InitializeUseCase
import com.softcat.foody.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class InitializationStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val initUseCase: InitializeUseCase,
    private val application: Application
) {

    fun create(): InitializationStore =
        object:
            InitializationStore, Store<InitializationStore.Intent, InitializationStore.State, InitializationStore.Label>
        by
            storeFactory.create(
                name = this::class.simpleName,
                initialState = InitializationStore.State.Loading,
                executorFactory = ::InitializationExecutor,
                reducer = InitializationReducer,
                bootstrapper = InitializationBootstrapper()
            ) {}

    private inner class InitializationBootstrapper: CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                if (initUseCase.isInitialized())
                    dispatch(Action.AlreadyInitialized)
                else
                    dispatch(Action.NeedsInitialization)
            }
        }
    }

    sealed interface Action {
        data object AlreadyInitialized: Action
        data object NeedsInitialization: Action
    }

    sealed interface Msg {
        data object LoadingStarted: Msg
        data class OptionsUpdated(val options: List<OptionModel>): Msg
    }

    private inner class InitializationExecutor: CoroutineExecutor<InitializationStore.Intent, Action, InitializationStore.State, Msg, InitializationStore.Label>() {

        override fun executeIntent(intent: InitializationStore.Intent) {
            Timber.i("${this::class.simpleName}: Intent is obtained: $intent")
            when (intent) {
                InitializationStore.Intent.InitRecipes -> initDatabase()
                is InitializationStore.Intent.SelectOption -> changeSelection(intent.value)
            }
        }

        override fun executeAction(action: Action) {
            Timber.i("${this::class.simpleName}: Action is obtained: $action")
            when (action) {
                Action.AlreadyInitialized -> publish(InitializationStore.Label.Initialized)
                Action.NeedsInitialization -> dispatch(Msg.OptionsUpdated(getInitialOptions()))
            }
        }

        private fun initDatabase() {
            val options = (state() as? InitializationStore.State.Options)?.options ?: return
            val value = options.find { it.isSelected }?.value ?: return

            dispatch(Msg.LoadingStarted)
            scope.launch(Dispatchers.IO) {
                initUseCase(requiredCount = value).onSuccess {
                    withContext(Dispatchers.Main) {
                        publish(InitializationStore.Label.Initialized)
                    }
                }.onFailure {
                    val title = application.getString(R.string.initialization_error)
                    val msg = "$title: ${it.message} <- ${it.cause?.message}"
                    withContext(Dispatchers.Main) {
                        publish(InitializationStore.Label.Error(msg))
                        dispatch(Msg.OptionsUpdated(getInitialOptions()))
                    }
                }
            }
        }

        private fun changeSelection(value: Int) {
            val currentState = state() as? InitializationStore.State.Options ?: return
            val options = currentState.options.toMutableList().apply {
                replaceAll {
                    if (it.value == value)
                        it.copy(isSelected = true)
                    else
                        it.copy(isSelected = false)
                }
            }
            dispatch(Msg.OptionsUpdated(options))
        }
    }

    private object InitializationReducer: Reducer<InitializationStore.State, Msg> {

        override fun InitializationStore.State.reduce(msg: Msg): InitializationStore.State {
            Timber.i("${this::class.simpleName}: Message is obtained: $msg")
            return when (msg) {
                Msg.LoadingStarted -> InitializationStore.State.Loading
                is Msg.OptionsUpdated -> InitializationStore.State.Options(msg.options)
            }
        }
    }

    private fun getInitialOptions(): List<OptionModel> {
        return listOf(
            OptionModel(
                title = application
                    .getString(R.string.add_recipes_title)
                    .format(RECIPE_COUNT),
                text = application.getString(R.string.small_db_description),
                value = RECIPE_COUNT,
                isSelected = false
            )
        )
    }

    companion object {
        private const val RECIPE_COUNT = 2058
    }
}