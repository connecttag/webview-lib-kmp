package org.connecttag.lib.webview

import android.content.Context
import android.content.Intent
import android.net.Uri

enum class PlatformExternalUrlKind {
    Tel,
    External,
    Facebook,
}

data class PlatformExternalUrlFailure(
    val url: String,
    val kind: PlatformExternalUrlKind,
    val throwable: Throwable,
)

fun handlePlatformExternalUrlLoading(
    context: Context,
    url: String?,
    allowExternalApps: Boolean,
    canOpenExternalUrl: (String) -> Boolean,
    isAllowedHttpUrl: (String) -> Boolean = { true },
    onFailure: (PlatformExternalUrlFailure) -> Unit = {},
): Boolean {
    val urlString = url ?: return false
    val kind = urlString.externalUrlKind() ?: return false
    if (!allowExternalApps || !canOpenExternalUrl(urlString)) return true
    if (kind == PlatformExternalUrlKind.Facebook && !isAllowedHttpUrl(urlString)) return true

    return try {
        val action = when (kind) {
            PlatformExternalUrlKind.Tel -> Intent.ACTION_DIAL
            PlatformExternalUrlKind.External,
            PlatformExternalUrlKind.Facebook -> Intent.ACTION_VIEW
        }
        context.startActivity(Intent(action, Uri.parse(urlString)))
        true
    } catch (throwable: Throwable) {
        onFailure(
            PlatformExternalUrlFailure(
                url = urlString,
                kind = kind,
                throwable = throwable,
            ),
        )
        false
    }
}

private fun String.externalUrlKind(): PlatformExternalUrlKind? {
    val trimmed = trim()
    return when {
        trimmed.startsWith("tel:", ignoreCase = true) -> PlatformExternalUrlKind.Tel
        trimmed.startsWith("sms:", ignoreCase = true) ||
            trimmed.startsWith("mailto:", ignoreCase = true) ||
            trimmed.startsWith("geo:", ignoreCase = true) ||
            trimmed.startsWith("market:", ignoreCase = true) -> PlatformExternalUrlKind.External
        trimmed.contains("facebook.com", ignoreCase = true) -> PlatformExternalUrlKind.Facebook
        else -> null
    }
}
