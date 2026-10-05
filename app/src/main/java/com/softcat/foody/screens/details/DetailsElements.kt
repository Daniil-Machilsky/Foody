package com.softcat.foody.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softcat.foody.R
import com.softcat.foody.ui.theme.FoodyTheme
import com.softcat.foody.ui.theme.FoodyTypography

@Composable
@NonRestartableComposable
fun CookRecipeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.start_cooking),
            style = FoodyTypography.labelMedium,
            color = White,
        )
    }
}

@Composable
fun RecipeCharacteristic(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.background(MaterialTheme.colorScheme.primary),
                text = label,
                style = FoodyTypography.bodySmall,
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .widthIn(min = 64.dp)
                ) {
                    Text(
                        text = value,
                        style = FoodyTypography.labelMedium,
                        color = Black
                    )
                    Text(
                        text = unit,
                        style = FoodyTypography.bodyMedium,
                        color = Black
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun CookRecipeButton_Preview() {
    FoodyTheme {
        CookRecipeButton(onClick = {})
    }
}

@Composable
@Preview
private fun RecipeCharacteristic_Preview() {
    FoodyTheme {
        RecipeCharacteristic(
            label = "Белки",
            value = "5.2",
            unit = "г"
        )
    }
}