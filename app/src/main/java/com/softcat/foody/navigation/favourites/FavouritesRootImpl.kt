package com.softcat.foody.navigation.favourites

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
import com.softcat.foody.screens.favourites.FavouritesComponentImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

class FavouritesRootImpl @AssistedInject constructor(
    @Assisted("context") componentContext: ComponentContext,
    @Assisted("openSearch") private val openSearchCallback: () -> Unit,
    @Assisted("openRecommendations") private val openRecommendationsCallback: () -> Unit,
    @Assisted("openCooking") private val openCookingRecipeCallback: (Recipe) -> Unit,
    private val favouritesComponentFactory: FavouritesComponentImpl.Factory,
    private val detailsComponentFactory: DetailsComponentImpl.Factory
): FavouritesRoot, ComponentContext by componentContext {
    private val navigation =  StackNavigation<Config>()

    override val stack: Value<ChildStack<*, FavouritesRoot.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Favourites,
        key = "FavouritesStack",
        handleBackButton = true,
        childFactory = ::child
    )

    @OptIn(DelicateDecomposeApi::class)
    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): FavouritesRoot.Child {
        return when (config) {
            is Config.Details -> {
                val component = detailsComponentFactory.create(
                    componentContext = componentContext,
                    recipe = config.recipe,
                    onBackClicked = { navigation.pop() },
                    openCookingRecipeCallback = openCookingRecipeCallback
                )
                FavouritesRoot.Child.Details(component)
            }

            Config.Favourites -> {
                val component = favouritesComponentFactory.create(
                    componentContext = componentContext,
                    openSearchCallback = openSearchCallback,
                    openRecommendationsCallback = openRecommendationsCallback,
                    openRecipeDetailsCallback = { navigation.push(Config.Details(it)) }
                )
                FavouritesRoot.Child.Favourites(component)
            }
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Favourites: Config

        @Serializable
        data class Details(val recipe: Recipe): Config
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("context") componentContext: ComponentContext,
            @Assisted("openSearch") openSearchCallback: () -> Unit,
            @Assisted("openRecommendations") openRecommendationsCallback: () -> Unit,
            @Assisted("openCooking") openCookingRecipeCallback: (Recipe) -> Unit,
        ): FavouritesRootImpl
    }
}