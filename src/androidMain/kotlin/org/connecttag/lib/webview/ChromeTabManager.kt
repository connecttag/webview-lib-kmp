package org.connecttag.lib.webview

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession

class ChromeTabManager private constructor() : CustomTabsServiceConnection() {
    private var customTabsClient: CustomTabsClient? = null
    private var customTabsSession: CustomTabsSession? = null
    private var customTabsPackageName: String? = null
    private var connectionCallback: (() -> Unit)? = null
    private var isBinding = false

    companion object {
        private const val TAG = "ChromeTabManager"
        private const val CUSTOM_TABS_SERVICE_ACTION = "android.support.customtabs.action.CustomTabsService"

        private val preferredCustomTabsPackages = listOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.sec.android.app.sbrowser",
        )

        @Volatile
        private var instance: ChromeTabManager? = null

        fun getInstance(): ChromeTabManager {
            return instance ?: synchronized(this) {
                instance ?: ChromeTabManager().also { instance = it }
            }
        }
    }

    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        customTabsClient = client
        customTabsPackageName = name.packageName
        isBinding = false
        client.warmup(0)
        customTabsSession = client.newSession(null)
        connectionCallback?.invoke()
    }

    override fun onServiceDisconnected(name: ComponentName) {
        customTabsClient = null
        customTabsSession = null
        isBinding = false
    }

    fun bindService(context: Context, callback: (() -> Unit)? = null): Boolean {
        connectionCallback = callback

        if (customTabsSession != null) {
            callback?.invoke()
            return true
        }
        if (isBinding) {
            return true
        }

        val packageName = resolveCustomTabsPackageName(context)
            ?: run {
                Log.w(TAG, "No Custom Tabs provider is visible to the app")
                return false
            }

        customTabsPackageName = packageName
        return try {
            val bound = CustomTabsClient.bindCustomTabsService(context, packageName, this)
            isBinding = bound
            if (!bound) {
                Log.w(TAG, "Custom Tabs service bind returned false for $packageName")
            }
            bound
        } catch (e: Exception) {
            isBinding = false
            Log.w(TAG, "Failed to bind Custom Tabs service for $packageName", e)
            false
        }
    }

    fun unbindService(context: Context) {
        if (!isBinding && customTabsClient == null) {
            return
        }
        try {
            context.unbindService(this)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unbind Custom Tabs service", e)
        } finally {
            customTabsClient = null
            customTabsSession = null
            isBinding = false
        }
    }

    fun mayLaunchUrl(uri: Uri): Boolean {
        return customTabsSession?.mayLaunchUrl(uri, null, null) ?: false
    }

    fun openUrl(
        context: Context,
        url: String,
        toolbarColor: Int? = null,
        secondaryToolbarColor: Int? = null,
        showTitle: Boolean = true,
        onFallback: (() -> Unit)? = null,
    ) {
        val uri = Uri.parse(url)
        val session = customTabsSession

        val colorParams = CustomTabColorSchemeParams.Builder().apply {
            toolbarColor?.let { setToolbarColor(it) }
            secondaryToolbarColor?.let { setSecondaryToolbarColor(it) }
        }.build()

        val customTabsIntent = CustomTabsIntent.Builder(session)
            .setDefaultColorSchemeParams(colorParams)
            .setShowTitle(showTitle)
            .build()

        customTabsPackageName?.let {
            customTabsIntent.intent.setPackage(it)
        }

        try {
            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to launch Custom Tab for $url; falling back to default browser", e)
            if (onFallback != null) {
                onFallback()
            } else {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (fallbackEx: Exception) {
                    Log.e(TAG, "Failed to launch default browser for $url", fallbackEx)
                }
            }
        }
    }

    private fun resolveCustomTabsPackageName(context: Context): String? {
        val packageManager = context.packageManager
        val serviceIntent = Intent(CUSTOM_TABS_SERVICE_ACTION)
        val resolveInfos = packageManager.queryIntentServices(serviceIntent, 0)
        val supportedPackages = resolveInfos.mapNotNull { it.serviceInfo?.packageName }.toSet()

        for (preferred in preferredCustomTabsPackages) {
            if (preferred in supportedPackages) {
                return preferred
            }
        }
        return supportedPackages.firstOrNull()
    }
}
