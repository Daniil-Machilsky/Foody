package com.softcat.foody.screens.details

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softcat.domain.entities.Ingredient
import com.softcat.domain.entities.IngredientCategory
import com.softcat.domain.entities.NutritionData
import com.softcat.domain.entities.Recipe
import com.softcat.domain.entities.RecipeTag
import com.softcat.foody.R
import com.softcat.foody.common.DetailsTopBar
import com.softcat.foody.common.ElementsScrollableFlow
import com.softcat.foody.ui.theme.FoodyTheme

@Composable
@Preview
private fun RecipeStep(
    modifier: Modifier = Modifier,
    stepNumber: Int = 1,
    stepCount: Int = 5,
    step: String = "Высыпать 500г муки, столовую ложку сахара и половину чайной ложки соли в миску.",
    onPreviousStepClicked: () -> Unit = {},
    onNextStepClicked: () -> Unit = {},
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.BottomStart),
                onClick = onPreviousStepClicked
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    contentDescription = null,
                    painter = painterResource(R.drawable.left_chevron),
                    tint = if (stepNumber > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }
            IconButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = onNextStepClicked
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    contentDescription = null,
                    painter = painterResource(R.drawable.right_chevron),
                    tint = if (stepNumber < stepCount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp)
                    .wrapContentSize()
                    .align(Alignment.TopCenter)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.step_number).format(stepNumber, stepCount),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                val scrollState = rememberScrollState()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    text = step,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ExtraInfoElement(
    modifier: Modifier = Modifier,
    iconResId: Int = R.drawable.sugar_icon,
    labelResId: Int = R.string.sugar,
    unitsResId: Int = R.string.calories_units,
    value: Int = 100
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.weight(1f))
        Text(
            modifier = Modifier.weight(2f),
            text = stringResource(labelResId),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(2f))
        Text(
            modifier = Modifier.weight(1f),
            text = value.toString() + " " + stringResource(unitsResId),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview
fun RecipeExtraInfoCard(
    modifier: Modifier = Modifier,
    data: NutritionData = NutritionData(
        calories = 1200f,
        fat = 150f,
        protein = 50f,
        carbohydrates = 250f
    ),
    cookingTime: Int = 30
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.nutrition_data_title),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            ExtraInfoElement(
                modifier = Modifier.fillMaxWidth(),
                iconResId = R.drawable.clock_filled,
                labelResId = R.string.cooking_time,
                unitsResId = R.string.min,
                value = cookingTime
            )
            ExtraInfoElement(
                modifier = Modifier.fillMaxWidth(),
                iconResId = R.drawable.calories_icon,
                labelResId = R.string.calories,
                unitsResId = R.string.calories_units,
                value = data.calories.toInt()
            )
            ExtraInfoElement(
                modifier = Modifier.fillMaxWidth(),
                iconResId = R.drawable.protein_icon,
                labelResId = R.string.protein,
                unitsResId = R.string.unit_gram,
                value = data.protein.toInt()
            )
            ExtraInfoElement(
                modifier = Modifier.fillMaxWidth(),
                iconResId = R.drawable.carbohydrates_icon,
                labelResId = R.string.carbohydrates,
                unitsResId = R.string.unit_gram,
                value = data.carbohydrates.toInt()
            )
            ExtraInfoElement(
                modifier = Modifier.fillMaxWidth(),
                iconResId = R.drawable.fat_icon,
                labelResId = R.string.fat,
                unitsResId = R.string.unit_gram,
                value = data.fat.toInt()
            )
        }
    }
}

@Composable
private fun RecipeScore(
    scoreValue: Int,
    isScoreVisible: Boolean,
    deleteScore: () -> Unit = {},
    onScoreChanged: (Int) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (isScoreVisible) {
                IconButton(
                    onClick = deleteScore,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cross),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                repeat(5) {
                    if (it + 1 <= scoreValue) {
                        IconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = { onScoreChanged(it + 1) }
                        ) {
                            Image(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource(R.drawable.star_filled),
                                contentDescription = null
                            )
                        }
                    } else {
                        IconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = { onScoreChanged(it + 1) }
                        ) {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource(R.drawable.star_outlined),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            } else {
                repeat(5) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(R.drawable.star_outlined),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
        val labelResId = if (isScoreVisible)
            R.string.score else R.string.authorize_to_access_scores
        Text(
            text = stringResource(labelResId),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
@Preview
private fun UserScoringData(
    modifier: Modifier = Modifier,
    scoreValue: Int = 3,
    isScoreVisible: Boolean = true,
    cookRecipeClicked: () -> Unit = {},
    deleteScore: () -> Unit = {},
    onScoreChanged: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        CookRecipeButton(
            onClick = cookRecipeClicked,
        )
        Spacer(Modifier.height(16.dp))
        RecipeScore(
            scoreValue = scoreValue,
            deleteScore = deleteScore,
            onScoreChanged = onScoreChanged,
            isScoreVisible = isScoreVisible
        )
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun DetailsScreen(component: DetailsComponent) {
    val model = component.model.collectAsState()

    DetailsContent(
        state = model.value,
        back = component::back,
        changeFavouriteStatus = component::changeFavouriteStatus,
        previousStep = component::previousStep,
        nextStep = component::nextStep,
        deleteScore = component::deleteScore,
        updateScore = component::updateScore,
        cookRecipeClicked = component::openCookingScreen
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun DetailsContent(
    state: DetailsStore.State,
    back: () -> Unit,
    changeFavouriteStatus: () -> Unit,
    previousStep: () -> Unit,
    nextStep: () -> Unit,
    cookRecipeClicked: () -> Unit,
    deleteScore: () -> Unit,
    updateScore: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            DetailsTopBar(
                isFavourite = state.isFavourite,
                isFavouriteVisible = state.isFavouriteVisible,
                onBackClicked = back,
                onChangeFavouriteStatus = changeFavouriteStatus
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(start = 16.dp, end = 16.dp)
                .verticalScroll(scrollState)
        ) {
            val ingredientStrings = state.recipe.ingredients.map { it.name }
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp

            Text(
                modifier = Modifier.padding(vertical = 4.dp),
                text = state.recipe.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Black
            )
            Text(
                modifier = Modifier.padding(vertical = 4.dp),
                text = state.recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray
            )
            ElementsScrollableFlow(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(max = screenHeight * 0.25f),
                elements = ingredientStrings,
                color = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.ingredients),
                iconId = R.drawable.vegetables,
                showAddButton = false
            )
            Spacer(Modifier.height(16.dp))
            RecipeStep(
                modifier = Modifier.heightIn(min = screenHeight * 0.1f, max = screenHeight * 0.25f),
                stepNumber = state.stepNumber,
                stepCount = state.recipe.steps.size,
                step = state.recipe.steps[state.stepNumber - 1],
                onPreviousStepClicked = previousStep,
                onNextStepClicked = nextStep
            )
            Spacer(Modifier.height(16.dp))
            RecipeExtraInfoCard(
                modifier = Modifier.wrapContentHeight(),
                data = state.recipe.nutrition,
                cookingTime = state.recipe.minutes
            )
            UserScoringData(
                modifier = Modifier,
                scoreValue = state.score,
                isScoreVisible = state.isScoreVisible,
                cookRecipeClicked = cookRecipeClicked,
                deleteScore = deleteScore,
                onScoreChanged = updateScore
            )
        }
    }
}

@Composable
@Preview
private fun Details_Preview() {
    val state = DetailsStore.State(
        recipe = Recipe(
            id = 1,
            name = "Chocolate cake",
            description = "A simple recipe for your birthday.",
            steps = listOf("Put flour into the bowl."),
            ingredients = listOf(
                Ingredient(1, "flour", IngredientCategory.Crops),
                Ingredient(2, "butter", IngredientCategory.Dairy),
                Ingredient(3, "sugar", IngredientCategory.Sweet)
            ),
            tags = listOf(
                RecipeTag("occasion"),
                RecipeTag("chocolate"),
                RecipeTag("90-minutes-or-less")
            ),
            isCooked = true,
            minutes = 85,
            nutrition = NutritionData(
                calories = 1190f,
                fat = 300f,
                protein = 5f,
                carbohydrates = 150f
            ),
            imageUrl = "",
            stepImages = emptyList(),
            ingredientQuantity = emptyList(),
            ingredientUnits = emptyList(),
            avgScore = 3.9f,
        ),
        stepNumber = 1,
        isScoreVisible = true,
        score = 4,
        isFavourite = true,
        isFavouriteVisible = true,
    )

    FoodyTheme {
        DetailsContent(
            state = state,
            back = {},
            changeFavouriteStatus = {},
            previousStep = {},
            nextStep = {},
            cookRecipeClicked = {},
            deleteScore = {},
            updateScore = {},
        )
    }
}