package net.perfectdreams.loritta.website.backend.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.style
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend

fun DIV.makeItAwesome(LorittaWebsiteBackend: LorittaWebsiteBackend, locale: BaseLocale, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center; padding-bottom: 64px;"

        h1 {
            + locale["website.home.makeItAwesome.title"]
        }

        a(classes = "add-me button pink shadow big", href = LorittaWebsiteBackend.addBotUrl.toString()) {
            style = "font-size: 1.5em; width: fit-content; margin: auto;"

            LorittaWebsiteBackend.svgIconManager.plus.apply(this)

            + " ${locale["website.jumbotron.addMe"]}"
        }
    }
}