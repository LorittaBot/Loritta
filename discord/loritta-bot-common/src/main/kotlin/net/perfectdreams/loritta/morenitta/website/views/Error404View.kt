package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot

class Error404View(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String
) : NavbarView(
    loritta,
    i18nContext,
    locale,
    path
) {
    override fun getTitle() = "404"

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            style = "text-align: center;"

            div(classes = "media single-column") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        img(src = "https://stuff.loritta.website/loritta-crying-heathecliff.png") {
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