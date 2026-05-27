package com.android.tv.reference.tv

import android.provider.Settings
import android.view.KeyEvent as AndroidKeyEvent
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

private enum class FocusRegion {
    Header,
    StreamA,
    StreamB,
    TopShelf,
    Ticker,
    Dock,
    SocialPanel
}

private data class TopShelfCard(
    val id: String,
    val title: String,
    val metadata: String,
    val badge: String
)

@Composable
fun TvReferenceRoot() {
    val overscanPadding = 24.dp
    val streamARequester = remember { FocusRequester() }
    val streamBRequester = remember { FocusRequester() }
    val shelfRequester = remember { FocusRequester() }
    val tickerRequester = remember { FocusRequester() }
    val dockRequester = remember { FocusRequester() }
    val socialRequester = remember { FocusRequester() }
    var focusedRegion by rememberSaveable { mutableStateOf(FocusRegion.TopShelf) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedProvider by rememberSaveable { mutableStateOf<SocialProviderClient>(SocialProviders.youtube) }
    var tickerPaused by rememberSaveable { mutableStateOf(false) }
    val reducedMotion = rememberReducedMotion()
    val hazeState = remember { HazeState() }
    val cards = remember {
        listOf(
            TopShelfCard("resume-1", "Resume: Matchday Final", "Live sports • 4K", "LIVE"),
            TopShelfCard("live-2", "Newsroom Prime", "Breaking • 15m ago", "NEW"),
            TopShelfCard("movie-3", "Night Drive", "Movie • 2h 13m", "RESUME"),
            TopShelfCard("album-4", "Electric Bloom", "Music video • Dolby", "TRENDING"),
            TopShelfCard("show-5", "Deep Space Briefing", "Series • S2 E7", "HOT"),
            TopShelfCard("doc-6", "Ocean Signals", "Documentary • 58m", "NEW")
        )
    }

    fun focusRegion(region: FocusRegion): Boolean {
        focusedRegion = region
        return when (region) {
            FocusRegion.StreamA -> streamARequester.requestFocus()
            FocusRegion.StreamB -> streamBRequester.requestFocus()
            FocusRegion.TopShelf -> shelfRequester.requestFocus()
            FocusRegion.Ticker -> tickerRequester.requestFocus()
            FocusRegion.Dock -> dockRequester.requestFocus()
            FocusRegion.SocialPanel -> socialRequester.requestFocus()
            FocusRegion.Header -> false
        }
    }

    LaunchedEffect(Unit) {
        delay(120)
        shelfRequester.requestFocus()
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF31215D),
                            Color(0xFF21143D),
                            Color(0xFF130B24)
                        )
                    )
                )
                .hazeSource(hazeState)
                .padding(horizontal = overscanPadding, vertical = 20.dp)
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action != AndroidKeyEvent.ACTION_DOWN) {
                        return@onPreviewKeyEvent false
                    }
                    when (event.nativeKeyEvent.keyCode) {
                        AndroidKeyEvent.KEYCODE_DPAD_UP -> when (focusedRegion) {
                            FocusRegion.TopShelf -> focusRegion(FocusRegion.StreamA)
                            FocusRegion.Ticker -> focusRegion(FocusRegion.TopShelf)
                            FocusRegion.Dock -> focusRegion(FocusRegion.Ticker)
                            FocusRegion.SocialPanel -> focusRegion(FocusRegion.TopShelf)
                            else -> false
                        }

                        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> when (focusedRegion) {
                            FocusRegion.StreamA, FocusRegion.StreamB -> focusRegion(FocusRegion.TopShelf)
                            FocusRegion.TopShelf -> focusRegion(FocusRegion.Ticker)
                            FocusRegion.Ticker -> focusRegion(FocusRegion.Dock)
                            else -> false
                        }

                        AndroidKeyEvent.KEYCODE_DPAD_LEFT -> when (focusedRegion) {
                            FocusRegion.StreamB -> focusRegion(FocusRegion.StreamA)
                            FocusRegion.SocialPanel -> focusRegion(FocusRegion.TopShelf)
                            else -> false
                        }

                        AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> when (focusedRegion) {
                            FocusRegion.StreamA -> focusRegion(FocusRegion.StreamB)
                            FocusRegion.TopShelf -> focusRegion(FocusRegion.SocialPanel)
                            else -> false
                        }

                        else -> false
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 344.dp)
            ) {
                CompactHeader()
                Spacer(modifier = Modifier.height(12.dp))
                DualStreamBand(
                    streamARequester = streamARequester,
                    streamBRequester = streamBRequester,
                    onFocusedRegion = { focusedRegion = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                TopShelfRail(
                    cards = cards,
                    selectedTab = selectedTab,
                    onSelectedTab = { selectedTab = it },
                    shelfRequester = shelfRequester,
                    onFocused = { focusedRegion = FocusRegion.TopShelf }
                )
                Spacer(modifier = Modifier.height(12.dp))
                NewsTicker(
                    paused = tickerPaused || reducedMotion,
                    tickerRequester = tickerRequester,
                    onFocused = {
                        focusedRegion = FocusRegion.Ticker
                        tickerPaused = true
                    },
                    onBlurred = { tickerPaused = false }
                )
                Spacer(modifier = Modifier.height(12.dp))
                MiniDock(
                    dockRequester = dockRequester,
                    onFocused = { focusedRegion = FocusRegion.Dock }
                )
            }

            SocialPanel(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(320.dp)
                    .fillMaxHeight()
                    .hazeEffect(hazeState)
                    .focusRequester(socialRequester)
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusedRegion = FocusRegion.SocialPanel
                        }
                    }
                    .focusable(),
                selectedProvider = selectedProvider,
                    onSelectedProvider = { selectedProvider = it }
            )
        }
    }
}

@Composable
private fun CompactHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Reference TV", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(20.dp))
            listOf("Search", "Live", "Guide", "My Stuff").forEach { item ->
                Text(item, color = Color.White.copy(alpha = 0.86f), fontSize = 15.sp)
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
        Text("8:15 PM", color = Color.White.copy(alpha = 0.82f), fontSize = 15.sp)
    }
}

@Composable
private fun DualStreamBand(
    streamARequester: FocusRequester,
    streamBRequester: FocusRequester,
    onFocusedRegion: (FocusRegion) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StreamPane(
            modifier = Modifier.weight(1f),
            requester = streamARequester,
            title = "Stream A · Live Game",
            metadata = "Primary audio · 4K HDR",
            progress = 0.62f,
            onFocused = { onFocusedRegion(FocusRegion.StreamA) }
        )
        StreamPane(
            modifier = Modifier.weight(1f),
            requester = streamBRequester,
            title = "Stream B · Night Drive",
            metadata = "Muted preview · Dolby Vision",
            progress = 0.38f,
            onFocused = { onFocusedRegion(FocusRegion.StreamB) }
        )
    }
}

@Composable
private fun StreamPane(
    modifier: Modifier,
    requester: FocusRequester,
    title: String,
    metadata: String,
    progress: Float,
    onFocused: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.035f else 1f, label = "stream-scale")
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .scale(scale)
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) Color(0xFFAE9BFF) else Color.White.copy(alpha = 0.24f),
                shape = RoundedCornerShape(20.dp)
            )
            .focusRequester(requester)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .focusable()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x553A2A70),
                        Color(0xAA0A071A)
                    )
                )
            )
            .padding(18.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Column {
                Text(
                    text = metadata,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(100))
                        .background(Color.White.copy(alpha = 0.18f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(100))
                            .background(Color(0xFFEBD8FF))
                    )
                }
            }
        }
    }
}

@Composable
private fun TopShelfRail(
    cards: List<TopShelfCard>,
    selectedTab: Int,
    onSelectedTab: (Int) -> Unit,
    shelfRequester: FocusRequester,
    onFocused: () -> Unit
) {
    val tabs = listOf("For You", "Live", "Trending", "Sports", "Movies")
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Top Shelf", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(16.dp))
                tabs.forEachIndexed { index, tab ->
                    Text(
                        text = tab,
                        color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.64f),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == index) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Text(
                "NASDAQ +0.73%  S&P +0.42%  AAPL +1.18%",
                color = Color(0xFFD5FFC7),
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = cards,
                key = { it.id },
                contentType = { "topshelf-card" }
            ) { card ->
                TopShelfCardItem(
                    card = card,
                    modifier = Modifier.then(
                        if (card.id == cards.first().id) {
                            Modifier.focusRequester(shelfRequester)
                        } else {
                            Modifier
                        }
                    ),
                    onFocused = onFocused
                )
            }
        }
    }
}

@Composable
private fun TopShelfCardItem(card: TopShelfCard, modifier: Modifier, onFocused: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "card-scale")
    Box(
        modifier = modifier
            .width(210.dp)
            .height(136.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) Color(0xFFB9ABFF) else Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color(0xAA1C1631))
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .focusable(interactionSource = remember { MutableInteractionSource() })
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Text(
                text = card.badge,
                color = Color(0xFFE1D7FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Column {
                Text(
                    text = card.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = card.metadata,
                    color = Color.White.copy(alpha = 0.74f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NewsTicker(
    paused: Boolean,
    tickerRequester: FocusRequester,
    onFocused: () -> Unit,
    onBlurred: () -> Unit
) {
    val headlines = "NEWS  Markets rally after inflation report • Space launch successful • Local weather alert • Streaming rights update • Semi finals tonight"
    val tickerTransition = rememberInfiniteTransition(label = "ticker")
    val tickerOffset by tickerTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1200f,
            animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing)
        ),
        label = "ticker-offset"
    )
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x8824193C))
            .focusRequester(tickerRequester)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused() else onBlurred()
            }
            .focusable()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = headlines,
            color = if (focused) Color.White else Color.White.copy(alpha = 0.84f),
            fontSize = 13.sp,
            maxLines = 1,
            modifier = Modifier.padding(start = if (paused) 0.dp else max(0f, -tickerOffset).dp)
        )
    }
}

@Composable
private fun MiniDock(
    dockRequester: FocusRequester,
    onFocused: () -> Unit
) {
    val entries = listOf("Home", "Live TV", "Guide", "Apps", "Sports", "My Stuff")
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x77211535))
            .focusRequester(dockRequester)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .focusable()
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        entries.forEach { entry ->
            Text(
                text = entry,
                color = if (focused) Color.White else Color.White.copy(alpha = 0.74f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SocialPanel(
    modifier: Modifier,
    selectedProvider: SocialProviderClient,
    onSelectedProvider: (SocialProviderClient) -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x6620123A))
            .padding(14.dp)
    ) {
        Text(
            "Social",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SocialProviders.all.forEach { provider ->
                val active = selectedProvider == provider
                Text(
                    text = provider.providerName,
                    color = if (active) Color.White else Color.White.copy(alpha = 0.64f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .focusable()
                        .onFocusChanged {
                            if (it.isFocused) {
                                scope.launch { onSelectedProvider(provider) }
                            }
                        }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        SocialWebView(
            url = selectedProvider.launchUrl,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Native integration hook: replace web feed with official SDK provider when credentials are available.",
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun SocialWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.domStorageEnabled = true
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                loadUrl(url)
            }
        },
        update = { view ->
            if (view.url != url) {
                view.loadUrl(url)
            }
        }
    )
}

@Composable
private fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }
}
