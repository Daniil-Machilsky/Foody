package com.softcat.foody.screens.cooking

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.softcat.foody.R
import com.softcat.foody.common.CookingTopBar
import com.softcat.foody.common.Switcher
import com.softcat.foody.ui.theme.FoodyTheme
import com.softcat.foody.ui.theme.FoodyTypography
import com.softcat.foody.ui.theme.LightGray

@Composable
fun CookingScreen(
    component: CookingComponent
) {
    val state by component.model.subscribeAsState()

    CookingContent(
        state = state,
        onBackClicked = component::back,
        onStepSelected = component::selectStep,
        portionsIncrement = component::increasePortions,
        portionsDecrement = component::decreasePortions,
        changeFavouriteStatus = component::changeFavouriteStatus,
        isCookedChange = component::changeIsCookedStatus
    )
}

@Composable
fun CookingContent(
    state: CookingStore.State,
    onBackClicked: () -> Unit,
    onStepSelected: (Int) -> Unit,
    portionsIncrement: () -> Unit,
    portionsDecrement: () -> Unit,
    changeFavouriteStatus: () -> Unit,
    isCookedChange: () -> Unit
) {
    CookingStep(
        imageUrl = state.imageUrl,
        stepNumber = state.stepNumber,
        stepCount = state.stepCount,
        onBackClicked = onBackClicked,
        onStepSelected = onStepSelected,
        isFavourite = state.isFavourite,
        isFavouriteVisible = state.isFavouriteVisible,
        onChangeFavouriteStatus = changeFavouriteStatus,
    ) {
        when (val content = state.content) {
            is CookingStore.State.StepContent.Instruction -> {
                StepDescription(
                    stepInstructionText = content.text,
                    isCooked = state.isCooked,
                    isCookedChange = isCookedChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                )
            }
            is CookingStore.State.StepContent.Prepare -> {
                PrepareIngredientsContent(
                    portions = content.portions,
                    ingredients = content.ingredients,
                    portionsIncrement = portionsIncrement,
                    portionsDecrement = portionsDecrement
                )
            }
        }
    }
}

@Composable
private fun StepDescription(
    stepInstructionText: String,
    isCooked: Boolean,
    isCookedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepInstruction(
            text = stepInstructionText,
            modifier = Modifier
        )
        Spacer(Modifier.weight(1f))
        Switcher(
            modifier = Modifier,
            checked = isCooked,
            onCheckedChanged = isCookedChange
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.is_cooked),
            color = LightGray,
            style = FoodyTypography.bodyMedium
        )
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun PrepareIngredientsContent(
    portions: Int,
    ingredients: List<CookingStore.State.IngredientDescription>,

    portionsIncrement: () -> Unit,
    portionsDecrement: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        PrepareTitle()
        Spacer(Modifier.height(16.dp))
        IngredientList(
            ingredients = ingredients,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.height(16.dp))
        PortionsSelector(
            portions = portions,
            portionsIncrement = portionsIncrement,
            portionsDecrement = portionsDecrement
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CookingStep(
    imageUrl: String,
    stepNumber: Int,
    stepCount: Int,
    isFavourite: Boolean,
    isFavouriteVisible: Boolean,

    onBackClicked: () -> Unit,
    onChangeFavouriteStatus: () -> Unit,
    onStepSelected: (Int) -> Unit,

    stepContent: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            CookingTopBar(
                onBackClicked = onBackClicked,
                onChangeFavouriteStatus = onChangeFavouriteStatus,
                isFavourite = isFavourite,
                isFavouriteVisible = isFavouriteVisible
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CookingStepImage(
                imageUrl = imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 256.dp),
            )
            Box(
                modifier = Modifier.weight(1f),
                content = { stepContent() }
            )
            StepSelector(
                stepNumber = stepNumber,
                stepCount = stepCount,
                onStepSelected = onStepSelected
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CookingStepImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    if (imageUrl.isEmpty()) {
        Image(
            modifier = modifier.padding(top = 4.dp),
            contentDescription = null,
            painter = painterResource(R.drawable.hat_smile_image),
            contentScale = ContentScale.Fit,
        )
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            placeholder = painterResource(R.drawable.hat_smile_image),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CookingContent_Preview() {

    val state = CookingStore.State(
        content = CookingStore.State.StepContent.Instruction(
            text = "Смешайте какао с мукой и разрыхлителем в большой миске."
        ),
        stepNumber = 1,
        stepCount = 3,
        imageUrl = "",
        isFavourite = false,
        isFavouriteVisible = false,
        isCooked = false
    )

    FoodyTheme {
        CookingContent(
            state = state,
            onBackClicked = {},
            onStepSelected = {},
            portionsIncrement = {},
            portionsDecrement = {},
            changeFavouriteStatus = {},
            isCookedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CookingContent_PrepareStep_Preview() {

    val state = CookingStore.State(
        content = CookingStore.State.StepContent.Prepare(
            ingredients = listOf(
                CookingStore.State.IngredientDescription(
                    id = 1,
                    name = "Какао",
                    quantity = "500",
                    units = "г",
                ),
                CookingStore.State.IngredientDescription(
                    id = 2,
                    name = "Сахар",
                    quantity = "200",
                    units = "г",
                ),
                CookingStore.State.IngredientDescription(
                    id = 3,
                    name = "Яйца",
                    quantity = "1",
                    units = "шт",
                ),
                CookingStore.State.IngredientDescription(
                    id = 4,
                    name = "Молоко",
                    quantity = "200",
                    units = "мл",
                ),
                CookingStore.State.IngredientDescription(
                    id = 5,
                    name = "Разрыхлитель",
                    quantity = "2",
                    units = "г",
                )
            ),
            portions = 1
        ),
        stepNumber = 1,
        stepCount = 3,
        imageUrl = "",
        isFavourite = true,
        isFavouriteVisible = false,
        isCooked = false
    )

    FoodyTheme {
        CookingContent(
            state = state,
            onBackClicked = {},
            onStepSelected = {},
            portionsIncrement = {},
            portionsDecrement = {},
            changeFavouriteStatus = {},
            isCookedChange = {}
        )
    }
}

@Preview
@Composable
private fun PrepareIngredientsContent_Preview() {
    FoodyTheme {
        PrepareIngredientsContent(
            portions = 1,
            ingredients = listOf(
                CookingStore.State.IngredientDescription(
                    id = 1,
                    name = "Какао",
                    quantity = "500",
                    units = "г",
                ),
                CookingStore.State.IngredientDescription(
                    id = 2,
                    name = "Сахар",
                    quantity = "200",
                    units = "г",
                ),
                CookingStore.State.IngredientDescription(
                    id = 3,
                    name = "Яйца",
                    quantity = "1",
                    units = "шт",
                ),
                CookingStore.State.IngredientDescription(
                    id = 4,
                    name = "Молоко",
                    quantity = "200",
                    units = "мл",
                ),
                CookingStore.State.IngredientDescription(
                    id = 5,
                    name = "Разрыхлитель",
                    quantity = "2",
                    units = "г",
                )
            ),
            portionsIncrement = {},
            portionsDecrement = {}
        )
    }
}