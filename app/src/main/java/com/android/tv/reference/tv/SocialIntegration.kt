package com.android.tv.reference.tv

/**
 * Abstraction layer for social feeds.
 * WebView is the default transport today, while native SDK implementations can be dropped in later.
 */
interface SocialProviderClient {
    val providerName: String
    val launchUrl: String
    val nativeProviderEnabled: Boolean
    val nativeProviderId: String?
}

data class WebViewSocialProviderClient(
    override val providerName: String,
    override val launchUrl: String,
    override val nativeProviderEnabled: Boolean = false,
    override val nativeProviderId: String? = null
) : SocialProviderClient

/**
 * Stub interface for future native provider adapters.
 * This allows wiring first-party SDKs later without changing UI contracts.
 */
interface NativeSocialProviderAdapter {
    val providerId: String
    fun isSupported(): Boolean
}

object SocialProviders {
    val youtube = WebViewSocialProviderClient(
        providerName = "YouTube",
        launchUrl = "https://m.youtube.com",
        nativeProviderId = "youtube-native"
    )
    val tiktok = WebViewSocialProviderClient(
        providerName = "TikTok",
        launchUrl = "https://www.tiktok.com/foryou",
        nativeProviderId = "tiktok-native"
    )
    val instagram = WebViewSocialProviderClient(
        providerName = "Instagram",
        launchUrl = "https://www.instagram.com",
        nativeProviderId = "instagram-native"
    )

    val all: List<SocialProviderClient> = listOf(youtube, tiktok, instagram)
}
