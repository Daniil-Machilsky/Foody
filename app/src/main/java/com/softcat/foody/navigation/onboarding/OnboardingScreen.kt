package com.softcat.foody.navigation.onboarding

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.softcat.foody.navigation.main.FoodyRootScreen
import com.softcat.foody.screens.cooking.CookingScreen
import com.softcat.foody.screens.initialization.InitializationScreen

@Composable
fun OnboardingScreen(component: OnboardingRootComponent) {
    Children(component.stack) {
        when (val instance = it.instance) {
            is OnboardingRootComponent.Child.FoodyRoot -> FoodyRootScreen(instance.component)
            is OnboardingRootComponent.Child.Initialization -> InitializationScreen(instance.component)
            is OnboardingRootComponent.Child.CookingRecipe -> CookingScreen(instance.component)
        }
    }
}