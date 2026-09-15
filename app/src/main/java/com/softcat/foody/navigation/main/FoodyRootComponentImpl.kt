package com.softcat.foody.navigation.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value
import com.softcat.domain.entities.Recipe
import com.softcat.foody.navigation.favourites.FavouritesRootImpl
import com.softcat.foody.navigation.search.SearchRootImpl
import com.softcat.foody.navigation.profile.ProfileRootImpl
import com.softcat.foody.navigation.recommend.RecommendRootImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable
import timber.log.Timber

class FoodyRootComponentImpl @AssistedInject constructor(
    @Assisted("context") componentContext: ComponentContext,
    @Assisted("openCookingRecipe") private val openCookingRecipeCallback: (Recipe) -> Unit,
    private val searchRootFactory: SearchRootImpl.Factory,
    private val favouritesRootFactory: FavouritesRootImpl.Factory,
    private val profileRootFactory: ProfileRootImpl.Factory,
    private val recommendRootFactory: RecommendRootImpl.Factory
): FoodyRootComponent, ComponentContext by componentContext {
    private val navigation = PagesNavigation<Config>()

    override val pages: Value<ChildPages<*, FoodyRootComponent.Child>> = childPages(
        key = "RootComponentPages",
        source = navigation,
        childFactory = ::child,
        serializer = Config.serializer(),
        initialPages = {
            Pages(
                items = ENTRIES,
                selectedIndex = 0
            )
        }
    )

    override fun selectPage(index: Int) {
        Timber.i("${this::class.simpleName}: selectPage($index)")
        if (index != pages.value.selectedIndex)
            navigation.select(index)
    }

    private fun child(config: Config, componentContext: ComponentContext): FoodyRootComponent.Child {
        return when (config) {
            Config.FavouritesRoot -> {
                val component = favouritesRootFactory.create(
                    componentContext = componentContext,
                    openSearchCallback = {
                        navigation.select(ENTRIES.indexOf(Config.SearchRoot))
                    },
                    openRecommendationsCallback = {
                        navigation.select(ENTRIES.indexOf(Config.RecommendationsRoot))
                    },
                    openCookingRecipeCallback = openCookingRecipeCallback
                )
                FoodyRootComponent.Child.FavouritesNavComponent(component)
            }
            Config.ProfileRoot -> {
                val component = profileRootFactory.create(
                    componentContext = componentContext
                )
                FoodyRootComponent.Child.ProfileNavComponent(component)
            }
            Config.RecommendationsRoot -> {
                val component = recommendRootFactory.create(
                    componentContext = componentContext,
                    openCookingRecipeCallback = openCookingRecipeCallback,
                )
                FoodyRootComponent.Child.RecommendNavComponent(component)
            }
            Config.SearchRoot -> {
                val component = searchRootFactory.create(
                    componentContext = componentContext,
                    openCookingRecipeCallback = openCookingRecipeCallback,
                )
                FoodyRootComponent.Child.SearchNavComponent(component)
            }
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object FavouritesRoot: Config

        @Serializable
        data object SearchRoot: Config

        @Serializable
        data object RecommendationsRoot: Config

        @Serializable
        data object ProfileRoot: Config
    }

    companion object {
        private val ENTRIES = listOf(
            Config.SearchRoot,
            Config.RecommendationsRoot,
            Config.FavouritesRoot,
            Config.ProfileRoot
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("context") componentContext: ComponentContext,
            @Assisted("openCookingRecipe") openCookingRecipeCallback: (Recipe) -> Unit
        ): FoodyRootComponentImpl
    }
}