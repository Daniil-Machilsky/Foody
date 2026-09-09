package com.softcat.foody.navigation.profile

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.softcat.foody.screens.authorization.AuthScreen
import com.softcat.foody.screens.profile.ProfileScreen
import com.softcat.foody.screens.scores.ScoresScreen

@Composable
fun ProfileRootContent(component: ProfileRoot) {
    Children(component.stack) {
        when (val instance = it.instance) {
            is ProfileRoot.Child.Authorization -> AuthScreen(instance.component)
            is ProfileRoot.Child.Profile -> ProfileScreen(instance.component)
            is ProfileRoot.Child.Scores -> ScoresScreen(instance.component)
        }
    }
}