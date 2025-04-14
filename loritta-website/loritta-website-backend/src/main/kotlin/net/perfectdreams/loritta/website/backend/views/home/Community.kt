package net.perfectdreams.loritta.website.backend.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.*
import net.perfectdreams.loritta.website.backend.views.BaseView

fun DIV.community(m: LorittaWebsiteBackend, svgIconManager: SVGIconManager, locale: BaseLocale, sectionClassName: String, isImageOnTheRightSide: Boolean) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        // TODO: Sponsors
        // TODO: Readd ad here
        /* adWrapper(svgIconManager) {
            generateNitroPayAd("home-community", NitroPayAdGenerator.ALL_SIZES)
        } */
        // generateNitroPayAdOrSponsor(0, "home-community", "Loritta v2 Community") { true }

        // generateNitroPayAd("home-community", "Loritta v2 Community")
        // generateAd("8109140955", "Loritta v2 Community", true)
        // generateAd("8109140955", "Loritta v2 Community", false)

        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                imgSrcSetFromEtherealGambi(
                    m,
                    m.images.lorittaCommunity,
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