package com.softcat.foody.screens.cooking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.softcat.foody.R
import com.softcat.foody.ui.theme.FoodyTheme
import com.softcat.foody.ui.theme.LightGray

@Composable
fun IngredientList(
    ingredients: List<CookingStore.State.IngredientDescription>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .then(modifier)
    ) {
        itemsIndexed(
            items = ingredients,
            key = { _, item -> item.name }
        ) { index, ingredient ->
            IngredientDescription(
                name = ingredient.name,
                quantity = ingredient.quantity,
                units = ingredient.units,
                bottomDivider = index < ingredients.lastIndex
            )
        }
    }
}

@Composable
fun PortionsSelector(
    portions: Int,
    portionsIncrement: () -> Unit,
    portionsDecrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.portions_count),
            style = MaterialTheme.typography.headlineSmall,
            color = Black,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        IncrementButton(portionsIncrement)
        Text(
            text = portions.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = Black,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        DecrementButton(portionsDecrement)
    }
}

@Composable
@NonRestartableComposable
private fun DecrementButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.minus),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null,
            modifier = Modifier.padding(4.dp).size(32.dp)
        )
    }
}

@Composable
@NonRestartableComposable
private fun IncrementButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.plus),
            tint = White,
            contentDescription = null,
            modifier = Modifier.padding(4.dp).size(32.dp)
        )
    }
}

@Composable
fun StepInstruction(
    text: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Text(
            text = text,
            style= MaterialTheme.typography.bodyMedium,
            color = Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun IngredientDescription(
    name: String,
    quantity: String,
    units: String,
    bottomDivider: Boolean = false,
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .then(
                if (bottomDivider)
                    Modifier.bottomDivider()
                else
                    Modifier
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = LightGray
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = quantity,
            style = MaterialTheme.typography.labelLarge,
            color = LightGray
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = units,
            style = MaterialTheme.typography.labelLarge,
            color = LightGray
        )
        Spacer(Modifier.width(8.dp))
    }
}

fun Modifier.bottomDivider(color: Color = LightGray, thickness: Dp = 1.dp): Modifier = this.drawBehind {
    val strokeWidth = thickness.toPx()
    val y = size.height - strokeWidth / 2

    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth
    )
}

@Composable
@NonRestartableComposable
fun PrepareTitle() {
    Text(
        text = stringResource(R.string.prepare_title),
        style = MaterialTheme.typography.headlineSmall,
        color = Black,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        textAlign = TextAlign.Start,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun StepSelector(
    stepNumber: Int,
    stepCount: Int,

    onStepSelected: (Int) -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            repeat(stepCount + 1) { index ->
                item {
                    StepCard(
                        stepNumber = index,
                        selected = stepNumber == index,
                        onClick = { onStepSelected(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(
            color = MaterialTheme.colorScheme.primary,
            width = 2.dp
        ),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.chef_hat),
                    contentDescription = null,
                    tint = Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = if (stepNumber == 0)
                    stringResource(R.string.prepare_step_title)
                else
                    stringResource(R.string.step_title, stepNumber),
                style = MaterialTheme.typography.labelSmall,
                color = Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientList_Preview() {
    val ingredients = listOf(
        CookingStore.State.IngredientDescription(
            name = "Мука",
            quantity = "200",
            units = "г"
        ),
        CookingStore.State.IngredientDescription(
            name = "Яйца",
            quantity = "2",
            units = "шт"
        ),
        CookingStore.State.IngredientDescription(
            name = "Сахар",
            quantity = "150",
            units = "г"
        ),
        CookingStore.State.IngredientDescription(
            name = "Разрыхлитель",
            quantity = "1.5",
            units = "г"
        )
    )
    FoodyTheme {
        IngredientList(
            ingredients = ingredients
        )
    }
}

@Composable
@Preview
private fun IncrementButton_Preview() {
    FoodyTheme {
        Row {
            IncrementButton({})
            DecrementButton({})
        }
    }
}

@Composable
@Preview
private fun PortionsSelector_Preview() {
    FoodyTheme {
        PortionsSelector(
            portions = 2,
            portionsIncrement = {},
            portionsDecrement = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StepSelector_Preview() {
    FoodyTheme {
        StepSelector(
            stepNumber = 3,
            stepCount = 5,
            onStepSelected = {}
        )
    }
}