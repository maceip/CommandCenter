package io.github.alexzhirkevich.cupertino.tv

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android TV focus treatment helper for compose-cupertino-based surfaces.
 * This keeps focus feedback strong for D-pad UX while preserving Cupertino card shapes.
 */
@Composable
fun CupertinoTvFocusable(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    borderRadius: Dp = 16.dp,
    focusedScale: Float = 1.06f,
    focusedBorder: Color = Color(0xFFBBAFFF),
    unfocusedBorder: Color = Color.White.copy(alpha = 0.2f),
    content: @Composable BoxScope.(focused: Boolean) -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) focusedScale else 1f,
        label = "cupertino-tv-focus-scale"
    )
    Box(
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .scale(scale)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) focusedBorder else unfocusedBorder,
                shape = RoundedCornerShape(borderRadius)
            )
            .onFocusChanged { focused = it.isFocused }
            .focusable(interactionSource = remember { MutableInteractionSource() })
            .padding(1.dp)
    ) {
        content(focused)
    }
}
