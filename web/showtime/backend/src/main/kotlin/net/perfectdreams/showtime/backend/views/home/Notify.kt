package net.perfectdreams.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.showtime.backend.utils.imgSrcSet
import net.perfectdreams.showtime.backend.views.BaseView

fun DIV.notify(locale: BaseLocale, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        // generateNitroPayAd("home-digital-influencers", "Loritta v2 Digital Influencers")

        div(classes = "media") {
            div(classes = "media-figure") {
                div {
                    style = "position: relative;"
                    imgSrcSet(
                            "${BaseView.versionPrefix}/assets/img/home/",
                            "lori_notification.png",
                            "(max-width: 800px) 50vw, 15vw",
                            1182,
                            1180,
                            100
                    )
                    imgSrcSet(
                            "${BaseView.versionPrefix}/assets/img/home/",
                            "lori_notification_video.png",
                            "(max-width: 800px) 50vw, 15vw",
                            1182,
                            1180,
                            100
                    ) {
                        classes = setOf("icon-middle")
                        style = "position: absolute; top: 0; left: 0;"
                    }
                }
            }

            div(classes = "media-body") {
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
        }
    }
}