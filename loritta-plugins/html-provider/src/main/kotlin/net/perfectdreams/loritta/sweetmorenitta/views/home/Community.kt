package net.perfectdreams.loritta.sweetmorenitta.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.loritta.sweetmorenitta.utils.NitroPayAdGenerator
import net.perfectdreams.loritta.sweetmorenitta.utils.adWrapper
import net.perfectdreams.loritta.sweetmorenitta.utils.generateNitroPayAd
import net.perfectdreams.loritta.sweetmorenitta.utils.imgSrcSet
import net.perfectdreams.loritta.sweetmorenitta.views.BaseView

fun DIV.community(locale: BaseLocale) {
    div(classes = "even-wrapper wobbly-bg") {
        style = "text-align: center;"

        adWrapper {
            generateNitroPayAd("home-community", NitroPayAdGenerator.ALL_SIZES)
        }

        // generateAd("8109140955", "Loritta v2 Community", true)
        // generateAd("8109140955", "Loritta v2 Community", false)

        div(classes = "media") {
            div(classes = "media-figure") {
                imgSrcSet(
                        "${BaseView.versionPrefix}/assets/img/home/",
                        "lori_community.png",
                        "(max-width: 800px) 50vw, 15vw",
                        768,
                        168,
                        100
                )
            }

            div(classes = "media-body") {
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
        }
    }
}