package net.perfectdreams.spicymorenitta.extensions

import kotlinx.html.*
import net.perfectdreams.spicymorenitta.locale
import org.w3c.dom.HTMLElement

fun TagConsumer<HTMLElement>.listIsEmptySection() {
    div {
        style = "text-align: center;font-size: 2em;opacity: 0.7;"
        div {
            img(src = "/assets/img/blog/lori_calca.gif") {
                style = "width: 20%; filter: grayscale(100%);"
            }
        }
        + "${locale["website.empty"]}${locale.getList("website.funnyEmpty").random()}"
    }
}

fun DIV.listIsEmptySection() {
    div {
        style = "text-align: center;font-size: 2em;opacity: 0.7;"
        div {
            img(src = "/assets/img/blog/lori_calca.gif") {
                style = "width: 20%; filter: grayscale(100%);"
            }
        }
        + "${locale["website.empty"]}${locale.getList("website.funnyEmpty").random()}"
    }
}