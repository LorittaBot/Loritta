package net.perfectdreams.loritta.website.backend.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.loritta.website.backend.utils.mediaWithContentWrapper

fun DIV.customization(locale: BaseLocale, sectionClassName: String, isImageOnTheRightSide: Boolean) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                img(src = "https://cdn.discordapp.com/attachments/510601125221761054/567911316094713856/customizacao.png") {
                    attributes["loading"] = "lazy"
                }
            },
            {
                div {
                    style = "text-align: left;"

                    div {
                        style = "text-align: center;"
                        h1 {
                            + locale["website.home.customization.title"]
                        }
                    }

                    for (str in locale.getList("website.home.customization.description")) {
                        p {
                            + str
                        }
                    }
                }
            }
        )
    }
}