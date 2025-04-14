package net.perfectdreams.loritta.website.backend.views.home

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.imgSrcSetFromEtherealGambi
import net.perfectdreams.loritta.website.backend.utils.mediaWithContentWrapper
import net.perfectdreams.loritta.common.locale.BaseLocale

fun DIV.chitChat(m: LorittaWebsiteBackend, locale: BaseLocale, sectionClassName: String, isImageOnTheRightSide: Boolean) {
    div(classes = "$sectionClassName wobbly-bg") {
        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                imgSrcSetFromEtherealGambi(
                    m,
                    m.images.lorittaPrize,
                    "png",
                    "(max-width: 800px) 50vw, 15vw"
                )
            },
            {
                div {
                    style = "text-align: left;"

                    div {
                        style = "text-align: center;"
                        h1 {
                            + locale["website.home.chitChat.title"]
                        }
                    }

                    for (str in locale.getList("website.home.chitChat.description")) {
                        p {
                            + str
                        }
                    }
                }
            }
        )
    }
}