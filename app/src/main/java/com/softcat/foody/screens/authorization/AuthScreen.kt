package com.softcat.foody.screens.authorization

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.softcat.foody.R
import com.softcat.foody.common.AuthTopBar
import com.softcat.foody.common.ProgressBar
import com.softcat.foody.common.ResultText
import com.softcat.foody.common.ResultTitle
import com.softcat.foody.common.StringDataItem
import com.softcat.foody.screens.favourites.NavigationButton
import com.softcat.foody.ui.theme.FoodyTheme

@Composable
fun AuthScreen(component: AuthComponent) {
    val model = component.model.collectAsState()
    AuthScreen(
        state = model.value,
        switchToInitialScreen = component::switchToInitialScreen,
        enter = component::enter,
        register = component::register,
        switchToEnter = component::switchToEnter,
        switchToRegister = component::switchToRegister,
        changeName = component::changeName,
        changeEmail = component::changeEmail,
        changePassword = component::changePassword,
        changeRepeatedPassword = component::changeRepeatedPassword
    )
}

@Composable
private fun AuthScreen(
    state: AuthStore.State,
    switchToInitialScreen: () -> Unit,
    enter: () -> Unit,
    register: () -> Unit,
    switchToEnter: () -> Unit,
    switchToRegister: () -> Unit,
    changeName: (String) -> Unit,
    changeEmail: (String) -> Unit,
    changePassword: (String) -> Unit,
    changeRepeatedPassword: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        topBar = { AuthTopBar(switchToInitialScreen) }
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()

        when (state) {
            is AuthStore.State.Enter -> {
                Enter(
                    modifier = Modifier.padding(top = topPadding),
                    email = state.email,
                    password = state.password,
                    onEmailChange = changeEmail,
                    onPasswordChange = changePassword,
                    enter = enter,
                    openRegisterScreen = switchToRegister,
                    isLoading = state.isLoading
                )
            }
            AuthStore.State.NoUser -> {
                NoUser(
                    modifier = Modifier.padding(top = topPadding),
                    openEnterScreen = switchToEnter,
                    openRegistrationScreen = switchToRegister
                )
            }
            is AuthStore.State.Register -> {
                Registration(
                    modifier = Modifier.padding(top = topPadding),
                    name = state.name,
                    password = state.password,
                    repeatedPassword = state.repeatPassword,
                    email = state.email,
                    onNameChange = changeName,
                    onPasswordChange = changePassword,
                    onRepeatedPasswordChange = changeRepeatedPassword,
                    onEmailChange = changeEmail,
                    register = register,
                    openEnterScreen = switchToEnter,
                    isLoading = state.isLoading
                )
            }

            AuthStore.State.Initial -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = topPadding),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressBar(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun NoUser(
    modifier: Modifier = Modifier,
    openEnterScreen: () -> Unit,
    openRegistrationScreen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.fillMaxHeight(0.3f),
            painter = painterResource(R.drawable.cloud_link),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(16.dp))
        ResultTitle(
            text = stringResource(R.string.auth_initial_screen_title)
        )
        Spacer(Modifier.height(4.dp))
        ResultText(
            text = stringResource(R.string.auth_initial_screen_text)
        )
        Spacer(Modifier.height(24.dp))
        NavigationButton(
            text = stringResource(R.string.enter),
            onClick = openEnterScreen
        )
        Spacer(Modifier.height(32.dp))
        NavigationButton(
            text = stringResource(R.string.register),
            onClick = openRegistrationScreen
        )
    }
}

@Composable
private fun MainAuthButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SecondaryAuthButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = White
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun Enter(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    enter: () -> Unit,
    openRegisterScreen: () -> Unit,
    isLoading: Boolean
) {
    var passwordShown by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxTextFieldHeight = screenHeight * 0.2f
    val visualTransformation = if (passwordShown)
        VisualTransformation.None
    else
        PasswordVisualTransformation()

    val context = LocalContext.current
    val gifImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            modifier = Modifier.size(192.dp),
            model = ImageRequest.Builder(context)
                .data(R.drawable.lock_animation)
                .crossfade(false)
                .build(),
            imageLoader = gifImageLoader,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(64.dp))
        StringDataItem(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxTextFieldHeight),
            text = email,
            leadingIconId = R.drawable.email_filled,
            hint = stringResource(R.string.email),
            onValueChange = onEmailChange
        )
        Spacer(Modifier.height(16.dp))
        StringDataItem(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxTextFieldHeight),
            text = password,
            leadingIconId = R.drawable.lock_filled,
            trailingIconId = if (passwordShown) R.drawable.eye else R.drawable.eye_crossed,
            trailingIconAction = { passwordShown = !passwordShown },
            hint = stringResource(R.string.password),
            onValueChange = onPasswordChange,
            visualTransformation = visualTransformation
        )
        Spacer(Modifier.weight(1f))
        MainAuthButton(
            modifier = Modifier.fillMaxWidth(0.7f),
            text = stringResource(R.string.enter),
            onClick = enter
        )
        if (isLoading) {
            Spacer(Modifier.weight(1f))
            ProgressBar(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(2f))
        }
        SecondaryAuthButton(
            modifier = Modifier.fillMaxWidth(0.7f),
            text = stringResource(R.string.register),
            onClick = openRegisterScreen
        )
        Spacer(Modifier.weight(1f))
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun Registration(
    modifier: Modifier = Modifier,
    name: String,
    email: String,
    password: String,
    repeatedPassword: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRepeatedPasswordChange: (String) -> Unit,
    register: () -> Unit,
    openEnterScreen: () -> Unit,
    isLoading: Boolean
) {
    var passwordShown by remember { mutableStateOf(false) }
    var repeatedPasswordShown by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxTextFieldHeight = screenHeight * 0.15f
    val context = LocalContext.current
    val gifImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.heightIn(min = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .size(128.dp)
                    .padding(8.dp),
                painter = painterResource(R.drawable.person),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
            AsyncImage(
                modifier = Modifier.size(48.dp).align(Alignment.BottomEnd),
                model = ImageRequest.Builder(context)
                    .data(R.drawable.plus_animation)
                    .crossfade(false)
                    .build(),
                imageLoader = gifImageLoader,
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(8.dp))
        StringDataItem(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxTextFieldHeight),
            text = name,
            leadingIconId = R.drawable.lock_filled,
            hint = stringResource(R.string.user_name),
            onValueChange = onNameChange
        )
        Spacer(Modifier.height(16.dp))
        StringDataItem(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxTextFieldHeight),
            text = email,
            leadingIconId = R.drawable.email_filled,
            hint = stringResource(R.string.email),
            onValueChange = onEmailChange
        )
        Spacer(Modifier.height(16.dp))
        StringDataItem(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxTextFieldHeight),
            text = password,
            leadingIconId = R.drawable.lock_filled,
            trailingIconId = if (passwordShown) R.drawable.eye else R.drawable.eye_crossed,
            trailingIconAction = { passwordShown = !passwordShown },
            hint = stringResource(R.string.password),
            onValueChange = onPasswordChange,
            visualTransformation = if (passwordShown)
                VisualTransformation.None
            else
                PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))
        StringDataItem(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxTextFieldHeight),
            text = repeatedPassword,
            leadingIconId = R.drawable.lock_filled,
            trailingIconId = if (repeatedPasswordShown) R.drawable.eye else R.drawable.eye_crossed,
            trailingIconAction = { repeatedPasswordShown = !repeatedPasswordShown },
            hint = stringResource(R.string.repeat_password),
            onValueChange = onRepeatedPasswordChange,
            visualTransformation = if (repeatedPasswordShown)
                VisualTransformation.None
            else
                PasswordVisualTransformation()
        )
        Spacer(Modifier.weight(1f))
        MainAuthButton(
            modifier = Modifier.fillMaxWidth(0.7f),
            text = stringResource(R.string.register),
            onClick = register
        )
        if (isLoading) {
            Spacer(Modifier.weight(1f))
            ProgressBar(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(2f))
        }
        SecondaryAuthButton(
            modifier = Modifier.fillMaxWidth(0.7f),
            text = stringResource(R.string.enter),
            onClick = openEnterScreen
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
@Preview(showBackground = true)
private fun RegistrationScreen_Preview() {
    val state = AuthStore.State.Register(
        name = "Soft cat",
        email = "d5.machilskiy@gmail.com",
        password = "Rh6F9!fG",
        repeatPassword = "Rh6F9!fG",
        isLoading = false
    )
    FoodyTheme {
        AuthScreen(
            state = state,
            switchToInitialScreen = {},
            enter = {},
            register = {},
            switchToEnter = {},
            switchToRegister = {},
            changeName = {},
            changeEmail = {},
            changePassword = {},
            changeRepeatedPassword = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun EnterScreen_Preview() {
    val state = AuthStore.State.Enter(
        email = "d5.machilskiy@gmail.com",
        password = "Rh6F9!fG",
        isLoading = false
    )
    FoodyTheme {
        AuthScreen(
            state = state,
            switchToInitialScreen = {},
            enter = {},
            register = {},
            switchToEnter = {},
            switchToRegister = {},
            changeName = {},
            changeEmail = {},
            changePassword = {},
            changeRepeatedPassword = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun NoUser_Preview() {
    val state = AuthStore.State.NoUser
    FoodyTheme {
        AuthScreen(
            state = state,
            switchToInitialScreen = {},
            enter = {},
            register = {},
            switchToEnter = {},
            switchToRegister = {},
            changeName = {},
            changeEmail = {},
            changePassword = {},
            changeRepeatedPassword = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun EnterLoading_Preview() {
    val state = AuthStore.State.Enter(
        "", "", true
    )
    FoodyTheme {
        AuthScreen(
            state = state,
            switchToInitialScreen = {},
            enter = {},
            register = {},
            switchToEnter = {},
            switchToRegister = {},
            changeName = {},
            changeEmail = {},
            changePassword = {},
            changeRepeatedPassword = {},
        )
    }
}