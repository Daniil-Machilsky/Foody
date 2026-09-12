package com.softcat.foody.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.softcat.foody.R

@Composable
@Preview(showBackground = true)
fun NavigationBar(
    selectedIndex: Int = 0,
    onIndexSelected: (Int) -> Unit = {}
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(),
        contentPadding = PaddingValues(0.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            shape = RectangleShape
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    R.drawable.search,
                    R.drawable.cook_hat_outlined,
                    R.drawable.bookmark_outlined,
                    R.drawable.profile_outlined
                ).forEachIndexed { index, resId ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RectangleShape)
                            .clickable(
                                onClick = { onIndexSelected(index) },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    ) {
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            painter = painterResource(resId),
                            tint = if (index == selectedIndex)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.shadow(
    color: Color = Black.copy(alpha = 0.15f),
    blur: Dp = 6.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp
) = drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            this.color = Color.Transparent.toArgb()
            setShadowLayer(
                blur.toPx(),
                offsetX.toPx(),
                offsetY.toPx(),
                color.toArgb()
            )
        }
        canvas.nativeCanvas.drawRect(
            0f,
            0f,
            size.width,
            size.height,
            paint
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SimpleAppBar(
    text: String = "Hello world!",
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    TopAppBar(
        expandedHeight = TopAppBarDefaults.MediumAppBarCollapsedHeight,
        windowInsets = windowInsets,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
@NonRestartableComposable
@Preview
fun ScoresTopBar(
    onBackClicked: () -> Unit = {}
) {
    TopBar(
        title = stringResource(R.string.scores),
        onBackClicked = onBackClicked,
        backButtonVisible = true
    )
}

@Composable
@NonRestartableComposable
@Preview
fun AuthTopBar(
    onBackClicked: () -> Unit = {}
) {
    TopBar(
        title = stringResource(R.string.authorization),
        onBackClicked = onBackClicked,
        backButtonVisible = true
    )
}

@Composable
@NonRestartableComposable
@Preview
fun CookingTopBar(
    onBackClicked: () -> Unit = {}
) {
    TopBar(
        title = stringResource(R.string.cooking),
        onBackClicked = onBackClicked,
        backButtonVisible = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NonRestartableComposable
fun TopBar(
    title: String,
    onBackClicked: () -> Unit = {},
    backButtonVisible: Boolean
) {
    TopAppBar(
        modifier = Modifier.height(64.dp),
        expandedHeight = TopAppBarDefaults.MediumAppBarCollapsedHeight,
        windowInsets = TopAppBarDefaults.windowInsets
            .only(WindowInsetsSides.Horizontal),
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = White
            )
        },
        navigationIcon = {
            if (backButtonVisible) {
                IconButton(onBackClicked) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(R.drawable.back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun DetailsTopBar(
    isFavourite: Boolean = true,
    isFavouriteVisible: Boolean = true,
    onChangeFavouriteStatus: () -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier.height(64.dp),
        expandedHeight = TopAppBarDefaults.MediumAppBarCollapsedHeight,
        windowInsets = TopAppBarDefaults.windowInsets
            .only(WindowInsetsSides.Horizontal),
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.recipe_details_title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            if (isFavouriteVisible) {
                AddToFavouritesButton(
                    isFavourite = isFavourite,
                    onClick = onChangeFavouriteStatus
                )
            }
        },
        navigationIcon = {
            IconButton(onBackClicked) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(R.drawable.back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun FridgeTopBar(
    onCartClicked: () -> Unit = {},
    onResetClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier.height(64.dp),
        expandedHeight = TopAppBarDefaults.MediumAppBarCollapsedHeight,
        windowInsets = TopAppBarDefaults.windowInsets
            .only(WindowInsetsSides.Horizontal),
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.fridge_title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            Row {
                IconButton(onResetClicked) {
                    Icon(
                        painter = painterResource(R.drawable.reset),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = White
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onCartClicked) {
                    Icon(
                        painter = painterResource(R.drawable.cart),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = White
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onBackClicked) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(R.drawable.back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    )
}