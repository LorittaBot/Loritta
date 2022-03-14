package net.perfectdreams.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.showtime.backend.utils.NitroPayAdGenerator
import net.perfectdreams.showtime.backend.utils.SVGIconManager
import net.perfectdreams.showtime.backend.utils.adWrapper
import net.perfectdreams.showtime.backend.utils.generateNitroPayAd
import net.perfectdreams.showtime.backend.utils.imgSrcSetFromResources
import net.perfectdreams.showtime.backend.utils.mediaWithContentWrapper
import net.perfectdreams.showtime.backend.views.BaseView

fun DIV.notify(svgIconManager: SVGIconManager, locale: BaseLocale, sectionClassName: String, isImageOnTheRightSide: Boolean) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        // TODO: Sponsor
        adWrapper(svgIconManager) {
            // generateNitroPayAd("home-digital-influencers", "Loritta v2 Digital Influencers")
            generateNitroPayAd("home-digital-influencers", NitroPayAdGenerator.ALL_SIZES)
        }

        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                div {
                    style = "position: relative;"
                    imgSrcSetFromResources(
                        "${BaseView.versionPrefix}/assets/img/home/lori_notification.png",
                        "(max-width: 800px) 50vw, 15vw"
                    )
                    imgSrcSetFromResources(
                        "${BaseView.versionPrefix}/assets/img/home/lori_notification_video.png",
                        "(max-width: 800px) 50vw, 15vw"
                    ) {
                        classes = setOf("icon-middle")
                        style = "position: absolute; top: 0; left: 0;"
                    }
                }
            },
            {
                div {
                    style = "text-align: left;"

                    div {
                        style = "text-align: center;"
                        h1 {
                            + locale["website.home.notify.title"]
                        }
                    }

                    for (str in locale.getList("website.home.notify.description")) {
                        p {
                            + str
                        }
                    }
                }
            }
        )
    }
}