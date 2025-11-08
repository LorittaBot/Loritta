package net.perfectdreams.loritta.website.backend.views.home

import kotlinx.html.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend

fun DIV.makeItAwesome(LorittaWebsiteBackend: LorittaWebsiteBackend, locale: BaseLocale, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center; padding-bottom: 64px;"

        h1 {
            + locale["website.home.makeItAwesome.title"]
        }

        a(classes = "discord-button pink", href = "https://dash.loritta.website/discord/add?source=website&medium=button&content=make_it_awesome") {
            style = "font-size: 1.5em; width: fit-content; margin: auto;"

            LorittaWebsiteBackend.svgIconManager.plus.apply(this)

            + " ${locale["website.jumbotron.addMe"]}"
        }
    }
}