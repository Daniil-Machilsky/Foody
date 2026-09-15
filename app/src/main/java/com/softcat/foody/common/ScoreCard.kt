package com.softcat.foody.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softcat.foody.screens.scores.RecipeScoreModel
import com.softcat.foody.ui.theme.FoodyTheme

@Composable
fun ScoreCard(
    modifier: Modifier = Modifier,
    score: RecipeScoreModel,
    onScoreClicked: (Int) -> Unit = {},
    onFavouriteIconClick: () -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp, max = 128.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(2.5f)
                ) {
                    Text(
                        text = score.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = Black
                    )
                    Text(
                        text = score.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ScoreSelector(
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = 2.dp),
                    scoreValue = score.score,
                    maxScore = 5,
                    iconSize = 24.dp,
                    onScoreClicked = onScoreClicked
                )
            }
        }
        if (score.isFavouriteVisible) {
            AddToFavouritesButton(
                modifier = Modifier
                    .padding(top = 8.dp, end = 4.dp)
                    .align(Alignment.TopEnd)
                    .size(18.dp),
                isFavourite = score.isFavourite,
                onClick = onFavouriteIconClick
            )
        }
        Text(
            modifier = Modifier
                .padding(bottom = 4.dp, end = 8.dp)
                .align(Alignment.BottomEnd),
            text = score.date,
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
@Preview
private fun ScoreCard_Preview() {
    val model = RecipeScoreModel(
        id = 1,
        recipeId = 1,
        score = 3,
        name = "Ice cream",
        description = "Cold and tasty. Hard to cook.",
        isFavouriteVisible = true,
        isFavourite = false,
        date = "11.02.2026"
    )
    FoodyTheme {
        ScoreCard(
            modifier = Modifier,
            score = model
        )
    }
}