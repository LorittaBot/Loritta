package net.perfectdreams.loritta.website.backend.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.*
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.*

fun DIV.notify(m: LorittaWebsiteBackend, svgIconManager: SVGIconManager, locale: BaseLocale, sectionClassName: String, isImageOnTheRightSide: Boolean) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        // TODO: Sponsor
        // TODO: Readd ad here
        /* adWrapper(svgIconManager) {
            // generateNitroPayAd("home-digital-influencers", "Loritta v2 Digital Influencers")
            generateNitroPayAd("home-digital-influencers", NitroPayAdGenerator.ALL_SIZES)
        } */

        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                div {
                    style = "position: relative;"
                    imgSrcSetFromEtherealGambi(
                        m,
                        m.images.lorittaNotification,
                        "png",
                        "(max-width: 800px) 50vw, 15vw"
                    )
                    imgSrcSetFromEtherealGambi(
                        m,
                        m.images.lorittaNotificationVideo,
                        "png",
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