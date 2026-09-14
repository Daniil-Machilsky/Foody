package com.softcat.foody.navigation.recommend

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.softcat.domain.entities.Recipe
import com.softcat.foody.screens.details.DetailsComponentImpl
import com.softcat.foody.screens.fridge.FridgeComponentImpl
import com.softcat.foody.screens.recomend.RecommendComponentImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

class RecommendRootImpl @AssistedInject constructor(
    @Assisted("context") componentContext: ComponentContext,
    @Assisted("openCookingRecipe") private val openCookingRecipeCallback: (Recipe) -> Unit,
    private val detailsComponentFactory: DetailsComponentImpl.Factory,
    private val recommendComponentFactory: RecommendComponentImpl.Factory,
    private val fridgeComponentFactory: FridgeComponentImpl.Factory,
): RecommendRoot, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RecommendRoot.Child>> = childStack(
        source = navigation,
        initialConfiguration = Config.Recommendations,
        serializer = Config.serializer(),
        key = "RecommendationsScreenStack",
        childFactory = ::child
    )

    @OptIn(DelicateDecomposeApi::class)
    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): RecommendRoot.Child {
        return when (config) {
            is Config.Details -> {
                val component = detailsComponentFactory.create(
                    componentContext = componentContext,
                    recipe = config.recipe,
                    onBackClicked = { navigation.pop() },
                    openCookingRecipeCallback = openCookingRecipeCallback
                )
                RecommendRoot.Child.Details(component)
            }
            Config.Recommendations -> {
                val component = recommendComponentFactory.create(
                    componentContext = componentContext,
                    openRecipeDetailsCallback = {
                        navigation.push(Config.Details(it))
                    },
                    openFridgeCallback = {
                        navigation.push(Config.Fridge)
                    }
                )
                RecommendRoot.Child.Recommend(component)
            }

            Config.Fridge -> {
                val component = fridgeComponentFactory.create(
                    openShoppingListCallback = {},
                    back = {
                        navigation.pop()
                    },
                    componentContext = componentContext
                )
                RecommendRoot.Child.Fridge(component)
            }
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Recommendations: Config

        @Serializable
        data class Details(val recipe: Recipe): Config

        @Serializable
        data object Fridge: Config
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("context") componentContext: ComponentContext,
            @Assisted("openCookingRecipe") openCookingRecipeCallback: (Recipe) -> Unit,
        ): RecommendRootImpl
    }
}