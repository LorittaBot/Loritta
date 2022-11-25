package net.perfectdreams.spicymorenitta.routes

import kotlinx.coroutines.delay
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.utils.offset
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.dom.removeClass

class DiscordBotBrasileiroRoute : BaseRoute("/discord-bot-brasileiro") {
    override val requiresLocales = false
    override val requiresUserIdentification = false

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        SpicyMorenitta.INSTANCE.launch {
            val part0 = document.select<Element>(".right-side-text .introduction .my-name-is")
            part0.addClass("animated", "fade-in-right", "one-second")
            part0.removeClass("invisible")

            delay(900)

            val part1 = document.select<Element>(".right-side-text .introduction .loritta")
            part1.addClass("animated", "fade-in-right", "one-second")
            part1.removeClass("invisible")

            delay(900)

            val part2 = document.select<Element>(".right-side-text .introduction .tagline")
            part2.addClass("animated", "fade-in-right", "one-second")
            part2.removeClass("invisible")
        }

        val blinkingPose = document.select<HTMLElement>(".blinking-pose")
        val blushingPose = document.select<HTMLElement>(".blushing-pose")

        SpicyMorenitta.INSTANCE.launch {
            while (true) {
                blinkingPose.style.visibility = ""
                delay(7000)
                blushingPose.style.visibility = ""
                blinkingPose.style.visibility = "visible"
                delay(140)
            }
        }

        val selfie = document.select<HTMLElement>("#loritta-selfie")
        document.select<HTMLElement>("#loritta-selfie").onClick {
            val offset = selfie.offset()

            val x = (it.asDynamic().pageX - offset.left) / blushingPose.offsetWidth // Se usar "selfie.offsetWidth", sempre irá retornar 0 pois as imagens são absolutas
            val y = (it.asDynamic().pageY - offset.top) / selfie.offsetHeight

            debug("x: $x; y: $y")
            if (x in 0.19..0.30 && y in 0.24..0.35) {
                blushingPose.style.visibility = "visible"
            }
        }
    }
}