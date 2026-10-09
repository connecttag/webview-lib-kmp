package org.connecttag.lib.webview

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

fun WebView.applyPlatformWebSettings(
    policy: PlatformWebSettingsPolicy,
    defaultCacheMode: Int = WebSettings.LOAD_CACHE_ELSE_NETWORK,
) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
    )
    with(settings) {
        javaScriptEnabled = policy.javaScriptEnabled
        domStorageEnabled = policy.domStorageEnabled
        allowFileAccess = policy.allowFileAccess
        allowContentAccess = policy.allowContentAccess
        cacheMode = policy.cacheMode.toWebSettingsCacheMode(default = defaultCacheMode)
        loadsImagesAutomatically = true
        blockNetworkLoads = false
        useWideViewPort = true
        loadWithOverviewMode = true
        layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        textZoom = 100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            safeBrowsingEnabled = policy.safeBrowsingRequired
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = if (policy.allowMixedContent) {
                WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            } else {
                WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    } else {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }
    isFocusable = true
    isFocusableInTouchMode = true
    scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    overScrollMode = View.OVER_SCROLL_NEVER
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, policy.allowThirdPartyCookies)
    }
}

fun WebView.applyPlatformWebRuntimeDefaults(
    blockNetworkImage: Boolean,
    mediaPlaybackRequiresUserGesture: Boolean,
    offscreenPreRasterEnabled: Boolean = false,
) {
    with(settings) {
        this.blockNetworkImage = blockNetworkImage
        this.mediaPlaybackRequiresUserGesture = mediaPlaybackRequiresUserGesture
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            offscreenPreRaster = offscreenPreRasterEnabled
        }
    }
}

fun WebView.destroyPlatformWebView(
    resetWebViewClient: Boolean = true,
    javascriptInterfaceNames: Iterable<String> = emptyList(),
) {
    stopLoading()
    javascriptInterfaceNames.forEach(::removeJavascriptInterface)
    if (resetWebViewClient) {
        webViewClient = WebViewClient()
    }
    destroy()
}

fun PlatformWebCacheMode.toWebSettingsCacheMode(default: Int): Int {
    return when (this) {
        PlatformWebCacheMode.Default -> default
        PlatformWebCacheMode.NoCache -> WebSettings.LOAD_NO_CACHE
        PlatformWebCacheMode.CacheOnly -> WebSettings.LOAD_CACHE_ONLY
        PlatformWebCacheMode.CacheElseNetwork -> WebSettings.LOAD_CACHE_ELSE_NETWORK
    }
}

fun createEmptyWebResourceResponse(mimeType: String): WebResourceResponse {
    return createTextWebResourceResponse(
        mimeType = mimeType,
        body = "",
    )
}

fun createTextWebResourceResponse(
    mimeType: String,
    body: String,
    encoding: String = "UTF-8",
): WebResourceResponse {
    return WebResourceResponse(
        mimeType,
        encoding,
        ByteArrayInputStream(body.toByteArray()),
    )
}

fun createWebViewHeaders(
    userAgent: String,
    cacheControl: String? = null,
): Map<String, String> {
    return buildMap {
        put("User-Agent", userAgent)
        if (cacheControl != null) put("Cache-Control", cacheControl)
    }
}
