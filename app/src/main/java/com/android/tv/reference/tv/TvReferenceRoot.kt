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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.zIndex
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlin.math.max

private enum class FocusRegion {
    StreamA,
    StreamB,
    TopShelf,
    StockWidget,
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
    val safePadding = 24.dp
    val streamARequester = remember { FocusRequester() }
    val streamBRequester = remember { FocusRequester() }
    val shelfRequester = remember { FocusRequester() }
    val stockRequester = remember { FocusRequester() }
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
            TopShelfCard("resume-1", "Resume: Matchday Final", "live sports • 4k", "live"),
            TopShelfCard("live-2", "Newsroom Prime", "breaking • 15m ago", "new"),
            TopShelfCard("movie-3", "Night Drive", "movie • 2h 13m", "resume"),
            TopShelfCard("album-4", "Electric Bloom", "music video • dolby", "trending"),
            TopShelfCard("show-5", "Deep Space Briefing", "series • s2 e7", "hot"),
            TopShelfCard("doc-6", "Ocean Signals", "documentary • 58m", "new"),
            TopShelfCard("queue-7", "Live: City Derby", "play in stream b", "live")
        )
    }

    fun moveFocus(region: FocusRegion): Boolean {
        focusedRegion = region
        return when (region) {
            FocusRegion.StreamA -> streamARequester.requestFocus()
            FocusRegion.StreamB -> streamBRequester.requestFocus()
            FocusRegion.TopShelf -> shelfRequester.requestFocus()
            FocusRegion.StockWidget -> stockRequester.requestFocus()
            FocusRegion.Ticker -> tickerRequester.requestFocus()
            FocusRegion.Dock -> dockRequester.requestFocus()
            FocusRegion.SocialPanel -> socialRequester.requestFocus()
        }
    }

    LaunchedEffect(Unit) {
        delay(140)
        shelfRequester.requestFocus()
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF342262), Color(0xFF21143F), Color(0xFF140C26))
                    )
                )
                .hazeSource(hazeState)
                .padding(horizontal = safePadding, vertical = 20.dp)
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action != AndroidKeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false
                    when (event.nativeKeyEvent.keyCode) {
                        AndroidKeyEvent.KEYCODE_DPAD_UP -> when (focusedRegion) {
                            FocusRegion.TopShelf, FocusRegion.StockWidget -> moveFocus(FocusRegion.StreamA)
                            FocusRegion.Ticker -> moveFocus(FocusRegion.TopShelf)
                            FocusRegion.Dock -> moveFocus(FocusRegion.Ticker)
                            FocusRegion.SocialPanel -> moveFocus(FocusRegion.TopShelf)
                            else -> false
                        }

                        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> when (focusedRegion) {
                            FocusRegion.StreamA, FocusRegion.StreamB -> moveFocus(FocusRegion.TopShelf)
                            FocusRegion.TopShelf, FocusRegion.StockWidget -> moveFocus(FocusRegion.Ticker)
                            FocusRegion.Ticker -> moveFocus(FocusRegion.Dock)
                            else -> false
                        }

                        AndroidKeyEvent.KEYCODE_DPAD_LEFT -> when (focusedRegion) {
                            FocusRegion.StreamB -> moveFocus(FocusRegion.StreamA)
                            FocusRegion.SocialPanel -> moveFocus(FocusRegion.TopShelf)
                            else -> false
                        }

                        AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> when (focusedRegion) {
                            FocusRegion.StreamA -> moveFocus(FocusRegion.StreamB)
                            FocusRegion.TopShelf -> moveFocus(FocusRegion.StockWidget)
                            FocusRegion.StockWidget -> moveFocus(FocusRegion.SocialPanel)
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
                    stockRequester = stockRequester,
                    onFocusedShelf = { focusedRegion = FocusRegion.TopShelf },
                    onFocusedStock = { focusedRegion = FocusRegion.StockWidget }
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
                        if (it.isFocused) focusedRegion = FocusRegion.SocialPanel
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
            Text("command center", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(20.dp))
            listOf("search", "live", "guide", "my stuff").forEach { item ->
                Text(item, color = Color.White.copy(alpha = 0.86f), fontSize = 15.sp)
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
        Text("8:15 pm", color = Color.White.copy(alpha = 0.82f), fontSize = 15.sp)
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
            title = "stream a · live game",
            metadata = "primary audio · 4k hdr",
            progress = 0.62f,
            onFocused = { onFocusedRegion(FocusRegion.StreamA) }
        )
        StreamPane(
            modifier = Modifier.weight(1f),
            requester = streamBRequester,
            title = "stream b · night drive",
            metadata = "muted preview · dolby vision",
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
    val scale by animateFloatAsState(if (focused) 1.045f else 1f, label = "stream-scale")
    val glowColor = if (focused) Color(0x55BBAAFF) else Color.Transparent
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .scale(scale)
            .zIndex(if (focused) 2f else 0f)
            .background(Color(0x22101222))
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) Color(0xFFBCAFFF) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .background(glowColor)
            .focusRequester(requester)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .focusable()
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Column {
                Text(text = metadata, color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp)
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
                            .background(Color(0xFFEAD9FF))
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
    stockRequester: FocusRequester,
    onFocusedShelf: () -> Unit,
    onFocusedStock: () -> Unit
) {
    val tabs = listOf("for you", "live", "trending", "sports", "movies")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("top shelf", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
                            .focusable()
                            .onFocusChanged { if (it.isFocused) onSelectedTab(index) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            StockWidget(
                stockRequester = stockRequester,
                onFocused = onFocusedStock
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        TvLazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = cards, key = { it.id }) { card ->
                TopShelfCardItem(
                    card = card,
                    modifier = if (card.id == cards.first().id) Modifier.focusRequester(shelfRequester) else Modifier,
                    onFocused = onFocusedShelf
                )
            }
        }
    }
}

@Composable
private fun StockWidget(
    stockRequester: FocusRequester,
    onFocused: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    Text(
        text = "nasdaq +0.73%   s&p +0.42%   aapl +1.18%",
        color = if (focused) Color(0xFFE8FFD9) else Color(0xFFD0F7C2),
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x552C3B2C))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .focusRequester(stockRequester)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .focusable()
    )
}

@Composable
private fun TopShelfCardItem(card: TopShelfCard, modifier: Modifier, onFocused: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "card-scale")
    Box(
        modifier = modifier
            .width(220.dp)
            .height(140.dp)
            .scale(scale)
            .zIndex(if (focused) 1f else 0f)
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
                Text(text = card.title, color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    val headlines =
        "news  markets rally after inflation report • storm warnings issued • space launch successful • local headlines updated"
    val tickerTransition = rememberInfiniteTransition(label = "ticker")
    val tickerOffset by tickerTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1200f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 20000, easing = LinearEasing)),
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
    val entries = listOf("home", "live tv", "guide", "apps", "sports", "my stuff")
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
            Text(text = entry, color = if (focused) Color.White else Color.White.copy(alpha = 0.74f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun SocialPanel(
    modifier: Modifier,
    selectedProvider: SocialProviderClient,
    onSelectedProvider: (SocialProviderClient) -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x6620123A))
            .padding(14.dp)
    ) {
        Text("social", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SocialProviders.all.forEach { provider ->
                val active = selectedProvider == provider
                Text(
                    text = provider.providerName.lowercase(),
                    color = if (active) Color.White else Color.White.copy(alpha = 0.64f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .focusable()
                        .onFocusChanged { if (it.isFocused) onSelectedProvider(provider) }
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
            text = "native integration hook available. swap to provider sdk when product credentials and policy reviews are ready.",
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
            if (view.url != url) view.loadUrl(url)
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
