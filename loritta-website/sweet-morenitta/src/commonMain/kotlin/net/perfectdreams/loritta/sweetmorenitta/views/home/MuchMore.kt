package net.perfectdreams.loritta.sweetmorenitta.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.*

fun DIV.muchMore(locale: BaseLocale) {
    div(classes = "odd-wrapper wobbly-bg") {
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