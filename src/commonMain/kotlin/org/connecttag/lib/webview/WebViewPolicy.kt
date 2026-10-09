package org.connecttag.lib.webview

import kotlinx.serialization.Serializable

@Serializable
enum class PlatformWebCacheMode {
    Default,
    NoCache,
    CacheOnly,
    CacheElseNetwork,
}

fun resolvePlatformWebCacheMode(
    useNoCache: Boolean,
    useCacheOnly: Boolean,
    useCacheElseNetwork: Boolean,
    default: PlatformWebCacheMode = PlatformWebCacheMode.CacheElseNetwork,
): PlatformWebCacheMode {
    return when {
        useNoCache -> PlatformWebCacheMode.NoCache
        useCacheOnly -> PlatformWebCacheMode.CacheOnly
        useCacheElseNetwork -> PlatformWebCacheMode.CacheElseNetwork
        else -> default
    }
}

@Serializable
data class PlatformWebSettingsPolicy(
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val allowFileAccess: Boolean = false,
    val allowContentAccess: Boolean = false,
    val allowMixedContent: Boolean = false,
    val allowThirdPartyCookies: Boolean = false,
    val safeBrowsingRequired: Boolean = true,
    val cacheMode: PlatformWebCacheMode = PlatformWebCacheMode.CacheElseNetwork,
)

fun interface PlatformWebUrlPolicy {
    fun canLoad(url: String?): Boolean
}

fun interface PlatformWebJavaScriptPolicy {
    fun canInjectJavaScript(url: String?): Boolean
}

@Serializable
data class PlatformWebResourceBlockRule(
    val token: String,
    val mimeType: String,
    val replacementBody: String = "",
)
