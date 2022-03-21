package net.perfectdreams.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
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

fun DIV.community(svgIconManager: SVGIconManager, locale: BaseLocale, sectionClassName: String, isImageOnTheRightSide: Boolean) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        // TODO: Sponsors
        adWrapper(svgIconManager) {
            generateNitroPayAd("home-community", NitroPayAdGenerator.ALL_SIZES)
        }
        // generateNitroPayAdOrSponsor(0, "home-community", "Loritta v2 Community") { true }

        // generateNitroPayAd("home-community", "Loritta v2 Community")
        // generateAd("8109140955", "Loritta v2 Community", true)
        // generateAd("8109140955", "Loritta v2 Community", false)

        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                imgSrcSetFromResources(
                    "${BaseView.versionPrefix}/assets/img/home/lori_community.png",
                    "(max-width: 800px) 50vw, 15vw"
                )
            },
            {
                div {
                    style = "text-align: left;"

                    div {
                        style = "text-align: center;"
                        h1 {
                            + locale["website.home.community.title"]
                        }
                    }

                    for (str in locale.getList("website.home.community.description")) {
                        p {
                            + str
                        }
                    }
                }
            }
        )
    }
}