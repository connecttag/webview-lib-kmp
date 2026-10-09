package org.connecttag.lib.webview

/**
 * Portable implementation of [PlatformWebViewController] for testing and desktop/mock environments.
 */
class DefaultWebViewController : PlatformWebViewController {
    private val history = mutableListOf<PlatformWebViewRequest>()
    private var currentIndex = -1
    private var stopped = false

    override val canGoBack: Boolean
        get() = currentIndex > 0

    override val currentUrl: String?
        get() = history.getOrNull(currentIndex)?.url

    override fun load(request: PlatformWebViewRequest) {
        stopped = false
        // Discard any forward history if we navigated from middle
        if (currentIndex < history.size - 1) {
            val toRemove = history.size - 1 - currentIndex
            repeat(toRemove) { history.removeAt(history.size - 1) }
        }
        history.add(request)
        currentIndex = history.size - 1
    }

    override fun reload() {
        stopped = false
    }

    override fun goBack(): Boolean {
        if (canGoBack) {
            currentIndex--
            stopped = false
            return true
        }
        return false
    }

    override fun stop() {
        stopped = true
    }

    fun isStopped(): Boolean = stopped
}
