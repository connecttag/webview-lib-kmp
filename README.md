# ConnectTag WebView Library (KMP)

[![JitPack](https://jitpack.io/v/connecttag/webview-lib-kmp.svg)](https://jitpack.io/#connecttag/webview-lib-kmp)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.20-blue.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20JVM%20%7C%20iOS-orange.svg)](https://kotlinlang.org/docs/multiplatform.html)

A lightweight, production-grade Kotlin Multiplatform library providing webview contracts, security policies, cache modes, Chrome Custom Tabs manager, and external URL interception.

## Features

- **WebView Contracts (`PlatformWebViewContracts`)**: Multiplatform controller (`PlatformWebViewController`) and request structures (`PlatformWebViewRequest`).
- **Security & Caching Policies (`WebViewPolicy`)**: Cache modes (`PlatformWebCacheMode`), settings policy (`PlatformWebSettingsPolicy`), and resource blocking.
- **Chrome Custom Tabs (`ChromeTabManager`)**: Pre-warming, session connection, and custom theme styling on Android.
- **External URL Interception (`PlatformExternalUrlHandler`)**: Handlers for phone (`tel:`), messaging (`sms:`), email (`mailto:`), map (`geo:`), and market links.
- **Zero Inter-Library Dependencies**: Fully self-contained models and contracts with native iOS (`iosArm64`, `iosSimulatorArm64`), Android, and JVM support.

## Installation

Add JitPack to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://jitpack.io")
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
commonMain.dependencies {
    implementation("com.github.connecttag:webview-lib-kmp:1.0.0")
}
```

## Usage

```kotlin
import org.connecttag.lib.webview.PlatformWebSettingsPolicy
import org.connecttag.lib.webview.PlatformWebCacheMode
import org.connecttag.lib.webview.PlatformWebViewRequest

val policy = PlatformWebSettingsPolicy(
    javaScriptEnabled = true,
    domStorageEnabled = true,
    cacheMode = PlatformWebCacheMode.CacheElseNetwork,
)

val request = PlatformWebViewRequest(
    url = "https://connecttag.com/about",
    settings = policy,
)
```

## License

```
Copyright 2026 ConnectTag

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
