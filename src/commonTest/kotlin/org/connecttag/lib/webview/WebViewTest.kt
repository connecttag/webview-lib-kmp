package org.connecttag.lib.webview

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WebViewTest {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    @Test
    fun resolvePlatformWebCacheMode_usesNoCacheFirst() {
        assertEquals(
            PlatformWebCacheMode.NoCache,
            resolvePlatformWebCacheMode(
                useNoCache = true,
                useCacheOnly = true,
                useCacheElseNetwork = true,
            ),
        )
    }

    @Test
    fun resolvePlatformWebCacheMode_usesCacheOnlyBeforeCacheElseNetwork() {
        assertEquals(
            PlatformWebCacheMode.CacheOnly,
            resolvePlatformWebCacheMode(
                useNoCache = false,
                useCacheOnly = true,
                useCacheElseNetwork = true,
            ),
        )
    }

    @Test
    fun resolvePlatformWebCacheMode_returnsDefaultWhenNoFlagIsSet() {
        assertEquals(
            PlatformWebCacheMode.Default,
            resolvePlatformWebCacheMode(
                useNoCache = false,
                useCacheOnly = false,
                useCacheElseNetwork = false,
                default = PlatformWebCacheMode.Default,
            ),
        )
    }

    @Test
    fun testPlatformWebSettingsPolicySerialization() {
        val policy = PlatformWebSettingsPolicy(
            javaScriptEnabled = true,
            domStorageEnabled = true,
            allowFileAccess = false,
            allowContentAccess = false,
            allowMixedContent = false,
            allowThirdPartyCookies = false,
            safeBrowsingRequired = true,
            cacheMode = PlatformWebCacheMode.CacheElseNetwork,
        )

        val encoded = json.encodeToString(policy)
        val decoded = json.decodeFromString<PlatformWebSettingsPolicy>(encoded)

        assertEquals(policy, decoded)
        assertTrue(decoded.javaScriptEnabled)
        assertEquals(PlatformWebCacheMode.CacheElseNetwork, decoded.cacheMode)
    }

    @Test
    fun testPlatformWebViewRequestSerialization() {
        val request = PlatformWebViewRequest(
            url = "https://connecttag.com/terms",
            headers = mapOf("Authorization" to "Bearer token123"),
            settings = PlatformWebSettingsPolicy(javaScriptEnabled = false),
        )

        val encoded = json.encodeToString(request)
        val decoded = json.decodeFromString<PlatformWebViewRequest>(encoded)

        assertEquals(request.url, decoded.url)
        assertEquals("Bearer token123", decoded.headers["Authorization"])
        assertFalse(decoded.settings.javaScriptEnabled)
    }

    @Test
    fun testDefaultWebViewController() {
        val controller = DefaultWebViewController()
        assertFalse(controller.canGoBack)
        assertEquals(null, controller.currentUrl)

        val req1 = PlatformWebViewRequest(url = "https://connecttag.com/page1")
        val req2 = PlatformWebViewRequest(url = "https://connecttag.com/page2")

        controller.load(req1)
        assertEquals("https://connecttag.com/page1", controller.currentUrl)
        assertFalse(controller.canGoBack)

        controller.load(req2)
        assertEquals("https://connecttag.com/page2", controller.currentUrl)
        assertTrue(controller.canGoBack)

        assertTrue(controller.goBack())
        assertEquals("https://connecttag.com/page1", controller.currentUrl)
        assertFalse(controller.canGoBack)

        controller.stop()
        assertTrue(controller.isStopped())
        controller.reload()
        assertFalse(controller.isStopped())
    }

    @Test
    fun testPlatformWebUserAgents() {
        assertTrue(PlatformWebUserAgents.all.isNotEmpty())
        val randomUa = PlatformWebUserAgents.random()
        assertNotNull(randomUa)
        assertTrue(randomUa.startsWith("Mozilla/5.0"))
    }
}
