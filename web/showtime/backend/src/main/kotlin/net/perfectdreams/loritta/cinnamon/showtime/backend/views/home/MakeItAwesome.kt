package net.perfectdreams.loritta.cinnamon.showtime.backend.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.style
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend

fun DIV.makeItAwesome(showtimeBackend: ShowtimeBackend, locale: BaseLocale, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center; padding-bottom: 64px;"

        h1 {
            + locale["website.home.makeItAwesome.title"]
        }

        a(classes = "add-me button pink shadow big", href = showtimeBackend.addBotUrl.toString()) {
            style = "font-size: 1.5em; width: fit-content; margin: auto;"

            showtimeBackend.svgIconManager.plus.apply(this)

            + " ${locale["website.jumbotron.addMe"]}"
        }
    }
}