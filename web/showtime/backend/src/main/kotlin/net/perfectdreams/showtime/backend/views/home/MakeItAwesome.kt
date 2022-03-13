package net.perfectdreams.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.style
import net.perfectdreams.showtime.backend.ShowtimeBackend

fun DIV.makeItAwesome(showtimeBackend: ShowtimeBackend, locale: BaseLocale, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center; padding-bottom: 64px;"

        h1 {
            + locale["website.home.makeItAwesome.title"]
        }

        a(classes = "add-me button pink shadow big", href = showtimeBackend.addBotUrl.toString()) {
            style = "font-size: 1.5em;"

            showtimeBackend.svgIconManager.plus.apply(this)

            + " ${locale["website.jumbotron.addMe"]}"
        }
    }
}