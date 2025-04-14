package net.perfectdreams.loritta.website.frontend.views

import kotlinx.coroutines.delay
import net.perfectdreams.dokyo.elements.HomeElements
import net.perfectdreams.loritta.website.frontend.LorittaWebsiteFrontend
import net.perfectdreams.loritta.website.frontend.utils.extensions.get
import net.perfectdreams.loritta.website.frontend.utils.extensions.offset
import net.perfectdreams.loritta.website.frontend.utils.extensions.onClick
import web.html.HTMLDivElement
import web.html.HTMLImageElement

class HomeView(val showtime: LorittaWebsiteFrontend) : DokyoView() {
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

            if (x in 0.39..0.43 && y in 0.37 ..0.41) {
                blushingPose.style.visibility = "visible"
            }
        }
    }
}