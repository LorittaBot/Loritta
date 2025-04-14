package net.perfectdreams.loritta.website.backend.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.style

fun DIV.muchMore(locale: BaseLocale, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        h1 {
            + locale["website.home.muchMore.title"]
        }

        /* div(classes = "cards") {
            repeat(20) {
            div {
                + "owo"
            }
            div {
                + "uwu"
            }
            }
        } */

        br {}
        br {}
        br {}
    }
}