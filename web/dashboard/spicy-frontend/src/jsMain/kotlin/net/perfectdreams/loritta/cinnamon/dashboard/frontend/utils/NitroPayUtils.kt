package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.browser.document
import kotlinx.browser.window
import mu.KotlinLogging
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLModElement
import org.w3c.dom.asList
import org.w3c.dom.get

object NitroPayUtils {
    private val logger = KotlinLogging.loggerClassName(NitroPayUtils::class)

    @JsName("renderAds")
    fun renderAds() {
        if (window["nitroAds"] != undefined && window["nitroAds"].loaded == true) {
            logger.info { "NitroPay is loaded!" }
            renderNitroPayAds()
        } else {
            logger.info { "NitroPay is not loaded yet! We are going to wait until the event is triggered to render the ads..." }
            document.addEventListener("nitroAds.loaded", {
                // nitroAds just loaded
                renderNitroPayAds()
            })
        }
    }

    @NoLiveLiterals
    private fun renderNitroPayAds() {
        val ads = document.querySelectorAll(".nitropay-ad")
            .asList()
            .filterIsInstance<HTMLModElement>()

        logger.info { "There are ${ads.size} NitroPay ads in the page..." }

        ads.forEach {
            if (!it.hasAttribute("data-request-id")) {
                try {
                    console.log(it)

                    val adType = it.getAttribute("data-nitropay-ad-type")

                    val dynamic = object {}.asDynamic()

                    // Enable demo mode if we aren't running it in a real website
                    if (!window.location.hostname.contains("loritta.website"))
                        dynamic.demo = true

                    if (adType == "video_player") {
                        dynamic.format = "video-ac"
                    } else {
                        dynamic.refreshLimit = 10
                        dynamic.refreshTime = 30
                        // Lazy loading
                        dynamic.renderVisibleOnly = false
                        dynamic.refreshVisibleOnly = true

                        val mediaQuery = it.getAttribute("data-nitropay-media-query")

                        if (mediaQuery != null)
                            dynamic.mediaQuery = mediaQuery

                        val adSizes = it.getAttribute("data-nitropay-ad-sizes")!!.split(", ")

                        dynamic.sizes = adSizes.map {
                            val (width, height) = it.split("x")
                            arrayOf(
                                width,
                                height
                            )
                        }.toTypedArray()

                        // While the report button is cool, it affects our ad container wrapper :(
                        // val report = object {}.asDynamic()
                        // report.enabled = true
                        // report.wording = "Report Ad"
                        // report.position = "top-right"
                        // dynamic.report = report

                        console.log(dynamic)
                    }

                    window["nitroAds"].createAd(it.id, dynamic)

                    logger.info { "Yay!" }
                } catch (e: Throwable) {
                    console.log(e)
                }
            }
        }
    }

    @NoLiveLiterals
    fun createAd(
        element: HTMLElement,
        width: Int,
        height: Int
    ) {
        if (!element.hasAttribute("data-request-id")) {
            try {
                console.log(element)

                val adType = element.getAttribute("data-nitropay-ad-type")

                val dynamic = object {}.asDynamic()

                // Enable demo mode if we aren't running it in a real website
                if (window.location.hostname in listOf("127.0.0.1", "localhost", "dash-canary.loritta.website"))
                    dynamic.demo = true

                if (adType == "video_player") {
                    dynamic.format = "video-ac"
                } else {
                    dynamic.refreshLimit = 10
                    dynamic.refreshTime = 30
                    // Lazy loading
                    dynamic.renderVisibleOnly = false
                    dynamic.refreshVisibleOnly = true

                    /* val mediaQuery = it.getAttribute("data-nitropay-media-query")

                    if (mediaQuery != null)
                        dynamic.mediaQuery = mediaQuery */

                    dynamic.sizes = arrayOf(
                        width,
                        height
                    )

                    // While the report button is cool, it affects our ad container wrapper :(
                    // val report = object {}.asDynamic()
                    // report.enabled = true
                    // report.wording = "Report Ad"
                    // report.position = "top-right"
                    // dynamic.report = report

                    console.log(dynamic)
                }

                window["nitroAds"].createAd(element.id, dynamic)

                logger.info { "Yay!" }
            } catch (e: Throwable) {
                console.log(e)
            }
        }
    }
}