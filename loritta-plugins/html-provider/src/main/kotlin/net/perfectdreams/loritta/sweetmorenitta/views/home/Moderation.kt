package net.perfectdreams.loritta.sweetmorenitta.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.*
import net.perfectdreams.loritta.sweetmorenitta.views.BaseView

fun DIV.moderation(locale: BaseLocale, websiteUrl: String) {
    div(classes = "odd-wrapper wobbly-bg") {
        style = "text-align: center;"

        div(classes = "media") {
            div(classes = "media-body") {
                div {
                    style = "text-align: left;"

                    div {
                        style = "text-align: center;"
                        h1 {
                            + locale["website.home.moderation.title"]
                        }
                    }

                    for (str in locale.getList("website.home.moderation.description")) {
                        p {
                            + str
                        }
                    }
                }
            }

            div(classes = "media-figure") {
                img(src = "${websiteUrl}${BaseView.versionPrefix}/assets/img/lori_police.png") {}
            }
        }
    }
}