package net.perfectdreams.loritta.sweetmorenitta.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style

class Error404View(
        locale: BaseLocale,
        path: String
) : NavbarView(
        locale,
        path
) {
    override fun getTitle() = locale["website.donate.title"]

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            style = "text-align: center;"

            div(classes = "media single-column") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        img(src = "https://loritta.website/assets/img/fanarts/l4.png") {
                            width = "175"
                        }

                        h1 {
                            + locale["website.error404.title"]
                        }

                        for (str in locale.getList("website.error404.description")) {
                            p {
                                + str
                            }
                        }
                    }
                }
            }
        }
    }
}