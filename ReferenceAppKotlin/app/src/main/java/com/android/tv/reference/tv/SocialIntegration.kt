package com.android.tv.reference.tv

/**
 * Abstraction layer for social feeds.
 * WebView is the default transport today, while native SDK implementations can be dropped in later.
 */
interface SocialProviderClient {
    val providerName: String
    val launchUrl: String
    val nativeProviderEnabled: Boolean
}

data class WebViewSocialProviderClient(
    override val providerName: String,
    override val launchUrl: String,
    override val nativeProviderEnabled: Boolean = false
) : SocialProviderClient

object SocialProviders {
    val youtube = WebViewSocialProviderClient(
        providerName = "YouTube",
        launchUrl = "https://m.youtube.com"
    )
    val tiktok = WebViewSocialProviderClient(
        providerName = "TikTok",
        launchUrl = "https://www.tiktok.com/foryou"
    )
    val instagram = WebViewSocialProviderClient(
        providerName = "Instagram",
        launchUrl = "https://www.instagram.com"
    )

    val all: List<SocialProviderClient> = listOf(youtube, tiktok, instagram)
}
