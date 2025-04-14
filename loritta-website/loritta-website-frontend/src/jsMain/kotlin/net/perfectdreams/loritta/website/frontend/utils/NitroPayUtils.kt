package net.perfectdreams.loritta.website.frontend.utils

import kotlinx.browser.document
import kotlinx.browser.window
import net.perfectdreams.loritta.website.frontend.utils.extensions.selectAll
import org.w3c.dom.HTMLModElement
import org.w3c.dom.get

object NitroPayUtils {
    @JsName("renderAds")
    fun renderAds() {
        if (window["nitroAds"] != undefined && window["nitroAds"].loaded == true) {
            println("NitroPay is loaded!")
            renderNitroPayAds()
        } else {
            println("NitroPay is not loaded yet! We are going to wait until the event is triggered to render the ads...")
            document.addEventListener("nitroAds.loaded", {
                // nitroAds just loaded
                renderNitroPayAds()
            })
        }
    }

    private fun renderNitroPayAds() {
        val ads = document.selectAll<HTMLModElement>(".nitropay-ad")

        println("There are ${ads.size} NitroPay ads in the page...")

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

                    println("Yay!")
                } catch (e: Throwable) {
                    console.log(e)
                }
            }
        }
    }
}