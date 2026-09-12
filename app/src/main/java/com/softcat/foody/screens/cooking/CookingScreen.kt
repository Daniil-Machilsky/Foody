package com.softcat.foody.screens.cooking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.softcat.foody.R
import com.softcat.foody.common.CookingTopBar
import com.softcat.foody.ui.theme.FoodyTheme

@Composable
fun CookingScreen(
    component: CookingComponent
) {
    val state by component.model.collectAsStateWithLifecycle()

    CookingContent(
        state = state,
        onBackClicked = component::back,
        onStepSelected = component::selectStep,
        portionsIncrement = component::increasePortions,
        portionsDecrement = component::decreasePortions
    )
}

@Composable
fun CookingContent(
    state: CookingStore.State,
    onBackClicked: () -> Unit,
    onStepSelected: (Int) -> Unit,
    portionsIncrement: () -> Unit,
    portionsDecrement: () -> Unit,
) {
    CookingStep(
        imageUrl = state.imageUrl,
        stepNumber = state.stepNumber,
        stepCount = state.stepCount,
        onBackClicked = onBackClicked,
        onStepSelected = onStepSelected,
    ) {
        when (val content = state.content) {
            is CookingStore.State.StepContent.Instruction -> {
                StepInstruction(
                    text = content.text
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
private fun PrepareIngredientsContent(
    portions: Int,
    ingredients: List<CookingStore.State.IngredientDescription>,

    portionsIncrement: () -> Unit,
    portionsDecrement: () -> Unit,
) {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(Modifier.height(32.dp))
        PrepareTitle()
        IngredientList(
            ingredients = ingredients,
        )
        PortionsSelector(
            portions = portions,
            portionsIncrement = portionsIncrement,
            portionsDecrement = portionsDecrement
        )
    }
}

@Composable
private fun CookingStep(
    imageUrl: String,
    stepNumber: Int,
    stepCount: Int,

    onBackClicked: () -> Unit,
    onStepSelected: (Int) -> Unit,

    stepContent: @Composable () -> Unit,
) {
    Scaffold(
        topBar = { CookingTopBar(onBackClicked) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 256.dp)
                    .padding(top = 4.dp),
                contentScale = ContentScale.Fit,
                placeholder = painterResource(R.drawable.hat_smile_image),
            )
            stepContent()
            Spacer(Modifier.weight(1f))
            StepSelector(
                stepNumber = stepNumber,
                stepCount = stepCount,
                onStepSelected = onStepSelected
            )
            Spacer(Modifier.height(16.dp))
        }
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
        imageUrl = ""
    )

    FoodyTheme {
        CookingContent(
            state = state,
            onBackClicked = {},
            onStepSelected = {},
            portionsIncrement = {},
            portionsDecrement = {}
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
                    name = "Какао",
                    quantity = "500",
                    units = "г"
                ),
                CookingStore.State.IngredientDescription(
                    name = "Сахар",
                    quantity = "200",
                    units = "г"
                ),
                CookingStore.State.IngredientDescription(
                    name = "Яйца",
                    quantity = "1",
                    units = "шт"
                ),
                CookingStore.State.IngredientDescription(
                    name = "Молоко",
                    quantity = "200",
                    units = "мл"
                ),
                CookingStore.State.IngredientDescription(
                    name = "Разрыхлитель",
                    quantity = "2",
                    units = "г"
                )
            ),
            portions = 1
        ),
        stepNumber = 1,
        stepCount = 3,
        imageUrl = ""
    )

    FoodyTheme {
        CookingContent(
            state = state,
            onBackClicked = {},
            onStepSelected = {},
            portionsIncrement = {},
            portionsDecrement = {}
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
                    name = "Какао",
                    quantity = "500",
                    units = "г"
                ),
                CookingStore.State.IngredientDescription(
                    name = "Сахар",
                    quantity = "200",
                    units = "г"
                ),
                CookingStore.State.IngredientDescription(
                    name = "Яйца",
                    quantity = "1",
                    units = "шт"
                ),
                CookingStore.State.IngredientDescription(
                    name = "Молоко",
                    quantity = "200",
                    units = "мл"
                ),
                CookingStore.State.IngredientDescription(
                    name = "Разрыхлитель",
                    quantity = "2",
                    units = "г"
                )
            ),
            portionsIncrement = {},
            portionsDecrement = {}
        )
    }
}