package com.softcat.foody.navigation.onboarding

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.softcat.domain.entities.Recipe
import com.softcat.foody.navigation.main.FoodyRootComponentImpl
import com.softcat.foody.screens.cooking.CookingComponentImpl
import com.softcat.foody.screens.initialization.InitializationComponentImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

class OnboardingRootComponentImpl @AssistedInject constructor(
    @Assisted("context") private val componentContext: ComponentContext,
    private val rootComponentFactory: FoodyRootComponentImpl.Factory,
    private val initComponentFactory: InitializationComponentImpl.Factory,
    private val cookingComponentFactory: CookingComponentImpl.Factory
): OnboardingRootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, OnboardingRootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Init,
            key = "OnboardingChildStack",
            handleBackButton = true,
            childFactory = ::child
        )

    @OptIn(DelicateDecomposeApi::class)
    private fun child(config: Config, componentContext: ComponentContext): OnboardingRootComponent.Child {
        return when (config) {
            Config.FoodyRoot -> {
                val component = rootComponentFactory.create(
                    componentContext = componentContext,
                    openCookingRecipeCallback = { navigation.push(Config.CookingRecipe(it)) },
                )
                OnboardingRootComponent.Child.FoodyRoot(component)
            }
            Config.Init -> {
                val component = initComponentFactory.create(
                    componentContext = componentContext,
                    openFoodyRootScreen = { navigation.replaceCurrent(Config.FoodyRoot) }
                )
                OnboardingRootComponent.Child.Initialization(component)
            }
            is Config.CookingRecipe -> {
                val component = cookingComponentFactory.create(
                    componentContext = componentContext,
                    backClickCallback = { navigation.pop() },
                    recipe = config.recipe
                )
                OnboardingRootComponent.Child.CookingRecipe(component)
            }
        }
    }

    @Serializable
    sealed interface Config {

        @Serializable
        data object FoodyRoot: Config

        @Serializable
        data object Init: Config

        @Serializable
        data class CookingRecipe(val recipe: Recipe): Config
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("context") componentContext: ComponentContext
        ): OnboardingRootComponentImpl
    }
}