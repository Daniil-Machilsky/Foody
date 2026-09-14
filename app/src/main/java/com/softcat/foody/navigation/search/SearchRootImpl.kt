package com.softcat.foody.navigation.search

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
import com.softcat.foody.screens.search.SearchComponentImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

class SearchRootImpl @AssistedInject constructor(
    @Assisted("context") componentContext: ComponentContext,
    @Assisted("openCookingRecipe") private val openCookingRecipeCallback: (Recipe) -> Unit,
    private val searchComponentFactory: SearchComponentImpl.Factory,
    private val detailsComponentFactory: DetailsComponentImpl.Factory
): SearchRoot, ComponentContext by componentContext {
    private val navigation =  StackNavigation<Config>()

    override val stack: Value<ChildStack<*, SearchRoot.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Search,
        key = "FavouritesStack",
        handleBackButton = true,
        childFactory = ::child
    )

    @OptIn(DelicateDecomposeApi::class)
    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): SearchRoot.Child {
        return when (config) {
            is Config.Details -> {
                val component = detailsComponentFactory.create(
                    componentContext = componentContext,
                    recipe = config.recipe,
                    onBackClicked = { navigation.pop() },
                    openCookingRecipeCallback = openCookingRecipeCallback,
                )
                SearchRoot.Child.Details(component)
            }

            Config.Search -> {
                val component = searchComponentFactory.create(
                    componentContext = componentContext,
                    openRecipeDetailsCallback = { navigation.push(Config.Details(it)) }
                )
                SearchRoot.Child.Search(component)
            }
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Search: Config

        @Serializable
        data class Details(val recipe: Recipe): Config
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("context") componentContext: ComponentContext,
            @Assisted("openCookingRecipe") openCookingRecipeCallback: (Recipe) -> Unit,
        ): SearchRootImpl
    }
}