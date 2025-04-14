package net.perfectdreams.loritta.website.backend.views.home

import kotlinx.html.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.SVGIconManager
import net.perfectdreams.loritta.website.backend.utils.imgSrcSetFromEtherealGambi
import net.perfectdreams.loritta.website.backend.utils.mediaWithContentWrapper

fun DIV.funnyCommands(
    m: LorittaWebsiteBackend,
    svgIconManager: SVGIconManager,
    locale: BaseLocale,
    sectionClassName: String,
    isImageOnTheRightSide: Boolean
) {
    div(classes = "$sectionClassName wobbly-bg") {
        id = "fun-section"

        // TODO: Sponsor
        // TODO: Readd ad here
        /* adWrapper(svgIconManager) {
            // generateNitroPayAdOrSponsor(2, "home-funny-commands1", "Loritta v2 Funny Commands") { true }
            // generateNitroPayAdOrSponsor(3, "home-funny-commands2", "Loritta v2 Funny Commands") { it != NitroPayAdDisplay.PHONE }
            generateNitroPayAd("home-funny-commands1", NitroPayAdGenerator.ALL_SIZES)
            generateNitroPayAd("home-funny-commands2", NitroPayAdGenerator.ALL_SIZES)
        } */

        mediaWithContentWrapper(
            isImageOnTheRightSide,
            {
                imgSrcSetFromEtherealGambi(
                    m,
                    m.images.lorittaCommands,
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
                            + locale["website.home.funnyCommands.title"]
                        }
                    }

                    for (str in locale.getList("website.home.funnyCommands.description")) {
                        p {
                            + str
                        }
                    }
                }
            }
        )
    }
}