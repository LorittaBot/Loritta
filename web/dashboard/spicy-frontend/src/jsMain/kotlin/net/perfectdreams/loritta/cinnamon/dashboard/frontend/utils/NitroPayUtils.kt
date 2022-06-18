package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import mu.KotlinLogging
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

object NitroPayUtils {
    private val demoMode = window.location.hostname in listOf("127.0.0.1", "localhost", "dash-canary.loritta.website")
    private val logger = KotlinLogging.loggerClassName(NitroPayUtils::class)
    var nitroPayLoaded by mutableStateOf(false)

    fun prepareNitroPayState() {
        if (window["nitroAds"] != undefined && window["nitroAds"].loaded == true) {
            logger.info { "NitroPay is loaded!" }
            nitroPayLoaded = true
        } else {
            logger.info { "NitroPay is not loaded yet! We are going to wait until the event is triggered to render the ads..." }
            document.addEventListener("nitroAds.loaded", {
                // nitroAds just loaded
                logger.info { "NitroPay is loaded!" }
                nitroPayLoaded = true
            })
        }
    }

    @NoLiveLiterals
    fun createAd(
        element: HTMLElement,
        width: Int,
        height: Int
    ) {
        try {
            console.log(element)

            val adType = element.getAttribute("data-nitropay-ad-type")

            val dynamic = object {}.asDynamic()

            // Enable demo mode if we aren't running it in a real website
            dynamic.demo = demoMode

            if (adType == "video_player") {
                dynamic.format = "video-ac"
            } else {
                dynamic.refreshLimit = 10
                dynamic.refreshTime = 30
                // Lazy loading
                dynamic.renderVisibleOnly = true
                dynamic.refreshVisibleOnly = true

                /* val mediaQuery = it.getAttribute("data-nitropay-media-query")

                if (mediaQuery != null)
                    dynamic.mediaQuery = mediaQuery */

                dynamic.sizes = arrayOf(
                    arrayOf(
                        width,
                        height
                    )
                )

                // While the report button is cool, it affects our ad container wrapper :(
                // val report = object {}.asDynamic()
                // report.enabled = true
                // report.wording = "Report Ad"
                // report.position = "top-right"
                // dynamic.report = report

                console.log(dynamic)
            }

            console.log(element.id)
            window["nitroAds"].createAd(element.id, dynamic)

            logger.info { "Yay!" }
        } catch (e: Throwable) {
            console.log(e)
        }
    }
}