package net.perfectdreams.loritta.sweetmorenitta.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.*
import net.perfectdreams.loritta.sweetmorenitta.utils.imgSrcSet
import net.perfectdreams.loritta.sweetmorenitta.views.BaseView

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