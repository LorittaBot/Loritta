package net.perfectdreams.loritta.cinnamon.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.mediaWithContentWrapper
import net.perfectdreams.loritta.cinnamon.showtime.backend.views.BaseView

fun DIV.moderation(locale: BaseLocale, sectionClassName: String, isImageOnTheRightSide: Boolean) {
    div(classes = "odd-wrapper wobbly-bg") {
        style = "text-align: center;"

        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                img(src = "${BaseView.versionPrefix}/assets/img/lori_police.png") {}
            },
            {
                div {
                    style = "text-align: left;"

                    div {
                        style = "text-align: center;"
                        h1 {
                            +locale["website.home.moderation.title"]
                        }
                    }

                    for (str in locale.getList("website.home.moderation.description")) {
                        p {
                            +str
                        }
                    }
                }
            }
        )
    }
}