package com.softcat.foody.screens.scores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softcat.foody.R
import com.softcat.foody.common.ScoreCard
import com.softcat.foody.common.ScoresTopBar
import com.softcat.foody.screens.scores.ScoresStore.State.ContentStatus.Content
import com.softcat.foody.ui.theme.FoodyTheme

@Composable
fun ScoresScreen(component: ScoresComponent) {
    val model = component.model.collectAsState()

    ScoresContent(
        state = model.value,
        back = component::back,
        onScoreClick = component::changeScoreValue,
        onFavouriteButtonClick = component::changeFavouriteStatus,
        isCookedChanged = component::changeIsCookedFilter
    )
}

@Composable
private fun ScoresContent(
    state: ScoresStore.State,
    back: () -> Unit,
    onScoreClick: (Int, Int) -> Unit,
    onFavouriteButtonClick: (Int) -> Unit,
    isCookedChanged: (Boolean) -> Unit
) {
    Scaffold(
        topBar = { ScoresTopBar(back) }
    ) { paddingValues ->
        Column(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            IsCookedSwitcher(
                modifier = Modifier.fillMaxWidth(),
                isCooked = state.isCookedRequired,
                isCookedChanged = isCookedChanged
            )
            when (val content = state.contentStatus) {
                is Content -> {
                    ScoresList(
                        modifier = Modifier.padding(top = 16.dp),
                        scores = content.scores,
                        onScoreClick = onScoreClick,
                        onFavouriteButtonClick = onFavouriteButtonClick
                    )
                }

                ScoresStore.State.ContentStatus.Loading -> {
                    Loading()
                }
            }
        }
    }
}

@Composable
private fun IsCookedSwitcher(
    modifier: Modifier = Modifier,
    isCooked: Boolean,
    isCookedChanged: (Boolean) -> Unit
) {
    val firstTextColor = if (!isCooked) MaterialTheme.colorScheme.secondary else Black
    val secondTextColor = if (isCooked) MaterialTheme.colorScheme.secondary else Black
    val firstUnderlineColor = if (!isCooked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background
    val secondUnderlineColor = if (isCooked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { isCookedChanged(false) },
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.all_scores),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        color = firstTextColor
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .padding(vertical = 2.dp)
                    .background(MaterialTheme.colorScheme.tertiary)
            )

            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { isCookedChanged(true) },
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.cooked),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        color = secondTextColor
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .padding(horizontal = 4.dp)
                    .background(firstUnderlineColor)
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(secondUnderlineColor)
            )
        }
    }
}

@Composable
private fun ScoresList(
    modifier: Modifier = Modifier,
    scores: List<RecipeScoreModel>,
    onScoreClick: (Int, Int) -> Unit,
    onFavouriteButtonClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = scores,
            key = { it.id }
        ) { score ->
            ScoreCard(
                modifier = Modifier,
                score = score,
                onScoreClicked = { onScoreClick(score.recipeId, it) },
                onFavouriteIconClick = { onFavouriteButtonClick(score.recipeId) }
            )
        }
        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
@Preview
private fun Loading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
@Preview
fun Scores_Preview() {
    val state = ScoresStore.State(
        isCookedRequired = false,
        contentStatus = Content(
            scores = listOf(
                RecipeScoreModel(
                    id = 1,
                    recipeId = 1,
                    score = 4,
                    name = "Pancakes",
                    description = "A simple and tasty breakfast",
                    isFavouriteVisible = true,
                    isFavourite = false,
                    date = "11.04.2025"
                ),
                RecipeScoreModel(
                    id = 2,
                    recipeId = 2,
                    score = 4,
                    name = "Pancakes",
                    description = "A simple and tasty breakfast",
                    isFavouriteVisible = true,
                    isFavourite = false,
                    date = "11.04.2025"
                )
            )
        )
    )
    FoodyTheme {
        ScoresContent(
            state = state,
            back = {},
            onScoreClick = { _, _ -> },
            onFavouriteButtonClick = {},
            isCookedChanged = {},
        )
    }
}