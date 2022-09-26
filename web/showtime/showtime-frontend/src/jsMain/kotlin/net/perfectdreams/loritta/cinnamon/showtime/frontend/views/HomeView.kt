package net.perfectdreams.loritta.cinnamon.showtime.frontend.views

import kotlinx.coroutines.delay
import net.perfectdreams.dokyo.elements.HomeElements
import net.perfectdreams.loritta.cinnamon.showtime.frontend.ShowtimeFrontend
import net.perfectdreams.loritta.cinnamon.showtime.frontend.utils.extensions.get
import net.perfectdreams.loritta.cinnamon.showtime.frontend.utils.extensions.offset
import net.perfectdreams.loritta.cinnamon.showtime.frontend.utils.extensions.onClick
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement

class HomeView(val showtime: ShowtimeFrontend) : DokyoView() {
    override suspend fun onLoad() {
        val blinkingPose = HomeElements.blinkingPose.get<HTMLImageElement>()
        val blushingPose = HomeElements.blushingPose.get<HTMLImageElement>()

        showtime.launch {
            while (true) {
                blinkingPose.style.visibility = ""
                delay(7000)
                blushingPose.style.visibility = ""
                blinkingPose.style.visibility = "visible"
                delay(140)
            }
        }

        val selfie = HomeElements.lorittaSelfie.get<HTMLDivElement>()
        selfie.onClick {
            val offset = selfie.offset()

            val x = (it.asDynamic().pageX - offset.left) / blushingPose.offsetWidth // Se usar "selfie.offsetWidth", sempre irá retornar 0 pois as imagens são absolutas
            val y = (it.asDynamic().pageY - offset.top) / selfie.offsetHeight

            // debug("x: $x; y: $y")
            if (x in 0.22..0.32 && y in 0.29..0.39) {
                blushingPose.style.visibility = "visible"
            }
        }
    }
}