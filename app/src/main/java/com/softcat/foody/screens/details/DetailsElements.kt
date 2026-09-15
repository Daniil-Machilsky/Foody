package com.softcat.foody.screens.details

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
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
@Preview
private fun CookRecipeButton_Preview() {
    FoodyTheme {
        CookRecipeButton(onClick = {})
    }
}