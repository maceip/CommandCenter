package io.github.alexzhirkevich.cupertino.tv

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class CupertinoTvSafeArea(
    val horizontal: Dp = 24.dp,
    val top: Dp = 20.dp,
    val bottom: Dp = 20.dp
) {
    fun asPaddingValues(): PaddingValues = PaddingValues(
        start = horizontal,
        end = horizontal,
        top = top,
        bottom = bottom
    )
}

@Composable
fun rememberCupertinoTvSafeArea(
    horizontal: Dp = 24.dp,
    top: Dp = 20.dp,
    bottom: Dp = 20.dp
): CupertinoTvSafeArea = CupertinoTvSafeArea(
    horizontal = horizontal,
    top = top,
    bottom = bottom
)

/**
 * Placeholder extension point for performance-focused list tuning on Android TV.
 * Callers can chain this before lazy list modifiers and evolve behavior centrally.
 */
fun Modifier.cupertinoTvPerformanceHint(): Modifier = this
