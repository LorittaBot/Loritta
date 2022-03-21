package net.perfectdreams.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.showtime.backend.utils.imgSrcSetFromResources
import net.perfectdreams.showtime.backend.utils.mediaWithContentWrapper
import net.perfectdreams.showtime.backend.views.BaseView

fun DIV.chitChat(locale: BaseLocale, sectionClassName: String, isImageOnTheRightSide: Boolean) {
    div(classes = "$sectionClassName wobbly-bg") {
        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                imgSrcSetFromResources(
                    "${BaseView.versionPrefix}/assets/img/home/lori_prize.png",
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