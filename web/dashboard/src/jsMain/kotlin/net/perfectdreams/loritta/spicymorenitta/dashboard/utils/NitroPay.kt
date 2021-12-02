package net.perfectdreams.loritta.spicymorenitta.dashboard.utils

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.browser.window

class NitroPay(private val nitroAds: dynamic) {
    // even tho this ain't a composable, Jetpack Compose complains... smh
    @NoLiveLiterals
    fun renderAd(id: String, adSizes: List<String>) {
        val dynamic = object {}.asDynamic()

        // Enable demo mode if we aren't running it in a real website
        if (!window.location.hostname.contains("loritta.website"))
            dynamic.demo = true

        dynamic.refreshLimit = 10
        dynamic.refreshTime = 30
        // Lazy loading
        dynamic.renderVisibleOnly = false
        dynamic.refreshVisibleOnly = true

        dynamic.sizes = adSizes.map {
            val (width, height) = it.split("x")
            arrayOf(
                width,
                height
            )
        }.toTypedArray()

        nitroAds.createAd(id, dynamic)
    }

    // even tho this ain't a composable, Jetpack Compose complains... smh
    @NoLiveLiterals
    fun renderSizelessAd(id: String) {
        val dynamic = object {}.asDynamic()

        // Enable demo mode if we aren't running it in a real website
        if (!window.location.hostname.contains("loritta.website"))
            dynamic.demo = true

        dynamic.refreshLimit = 10
        dynamic.refreshTime = 30
        // Lazy loading
        dynamic.renderVisibleOnly = false
        dynamic.refreshVisibleOnly = true

        nitroAds.createAd(id, dynamic)
    }
}