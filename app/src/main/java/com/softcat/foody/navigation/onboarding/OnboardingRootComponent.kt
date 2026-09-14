package com.softcat.foody.navigation.onboarding

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.softcat.foody.navigation.main.FoodyRootComponent
import com.softcat.foody.screens.cooking.CookingComponent
import com.softcat.foody.screens.initialization.InitializationComponent


interface OnboardingRootComponent {

    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Initialization(val component: InitializationComponent): Child

        data class FoodyRoot(val component: FoodyRootComponent): Child

        data class CookingRecipe(val component: CookingComponent): Child
    }
}