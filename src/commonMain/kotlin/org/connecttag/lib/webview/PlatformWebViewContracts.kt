package org.connecttag.lib.webview

import kotlinx.serialization.Serializable

@Serializable
data class PlatformWebViewRequest(
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val settings: PlatformWebSettingsPolicy = PlatformWebSettingsPolicy(),
)

interface PlatformWebViewController {
    val canGoBack: Boolean
    val currentUrl: String?
    fun load(request: PlatformWebViewRequest)
    fun reload()
    fun goBack(): Boolean
    fun stop()
}
