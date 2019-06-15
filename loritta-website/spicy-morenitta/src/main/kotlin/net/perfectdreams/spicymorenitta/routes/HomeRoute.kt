package net.perfectdreams.spicymorenitta.routes

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.Element
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.removeClass

class HomeRoute : BaseRoute("/") {
    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        GlobalScope.launch {
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
    }
}