package net.perfectdreams.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.iframe
import kotlinx.html.img
import kotlinx.html.style

var icons = listOf(
        "/v3/assets/img/trust/cellbit.png", // Cellbit
        "/v3/assets/img/trust/felps.png", // Felps
        "/v3/assets/img/trust/gartic.png", // Gartic
        "/v3/assets/img/trust/loud.png", // Loud
        "/v3/assets/img/trust/dudscord.png", // Dudscord
        "/v3/assets/img/trust/chavecu.png", // Flakes
        "/v3/assets/img/trust/flakes_power.png", // Flakes
        "/v3/assets/img/trust/drawn_mask.png", // Drawn Mask
        "/v3/assets/img/trust/driscord.png", // Driscord
        "/v3/assets/img/trust/mikaru.png", // Mikaru
        "/v3/assets/img/trust/furry_amino.png", // Furry Amino
        "/v3/assets/img/trust/brksedu.png", // brksedu
        "/v3/assets/img/trust/lubatv.png", // LubaTV
        "/v3/assets/img/trust/mbj.png" // MBJ
)

fun DIV.trust(locale: BaseLocale, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        h1 {
            + locale["website.home.trust.title"]
        }

        div(classes = "guild-icons-horizontal") {
            div(classes = "guilds-wrapper") {
                var idx = 0

                for (icon in icons) {
                    var mod = idx % 4

                    var classNames = when (mod) {
                        1, 3 -> "icon-middle"
                        2 -> "icon-top"
                        else -> "icon-bottom"
                    }
                    img(classes = classNames, src = icon) {
                        attributes["loading"] = "lazy"
                        width = "96"
                        style = "border-radius: 9999px; margin-left: 4px; margin-right: 4px;"
                    }
                    idx++
                }
            }
        }

        br {}
        br {}

        /* p {
            + "Texto texto texto texto texto!"
        }
        p {
            + "Texto e mais texto"
        } */

        // Drawn Mask YouTube Video
        iframe {
            src = "https://www.youtube.com/embed/PwTKQyiTBVU"
            attributes["frameborder"] = "0"
            attributes["allowfullscreen"] = "true"
            attributes["allow"] = "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            attributes["height"] = "279"
            attributes["width"] = "496"
            attributes["loading"] = "lazy"
        }

        // Chavecu Twitch Clip
        iframe {
            src = "https://clips.twitch.tv/embed?clip=FrigidFaithfulLemurCoolCat&autoplay=false&parent=canary.loritta.website&parent=loritta.website"
            attributes["frameborder"] = "0"
            attributes["allowfullscreen"] = "true"
            attributes["height"] = "279"
            attributes["width"] = "496"
            attributes["loading"] = "lazy"
        }

        /* p {
            + "E eu nunca vou me contentar até conseguir transformar o mundo!"
        } */

        br {}
    }
}

fun DIV.trustBrasil(locale: BaseLocale) {
    div(classes = "even-wrapper wobbly-bg") {
        style = "text-align: center;"

        h1 {
            + "Todos os seus YouTubers e Streamers favoritos já me adicionaram!"
        }

        div(classes = "guild-icons-horizontal") {
            div(classes = "guilds-wrapper") {
                var idx = 0

                for (icon in icons) {
                    var mod = idx % 4

                    var classNames = when (mod) {
                        1, 3 -> "icon-middle"
                        2 -> "icon-top"
                        else -> "icon-bottom"
                    }
                    img(classes = classNames, src = icon) {
                        width = "96"
                        style = "border-radius: 9999px; margin-left: 4px; margin-right: 4px;"
                    }
                    idx++
                }
            }
        }

        br {}
        br {}

        /* p {
            + "Texto texto texto texto texto!"
        }
        p {
            + "Texto e mais texto"
        } */

        iframe() {
            // src = "https://clips.twitch.tv/embed?clip=PeacefulAlertEggplantBudStar&autoplay=false"
            attributes["frameborder"] = "0"
            attributes["allowfullscreen"] = "true"
            attributes["height"] = "279"
            attributes["width"] = "496"
            attributes["lazy-load-url"] = "https://clips.twitch.tv/embed?clip=PeacefulAlertEggplantBudStar&autoplay=false"
        }

        iframe() {
            // src = "https://clips.twitch.tv/embed?clip=PeacefulAlertEggplantBudStar&autoplay=false"
            attributes["frameborder"] = "0"
            attributes["allowfullscreen"] = "true"
            attributes["height"] = "279"
            attributes["width"] = "496"
            attributes["lazy-load-url"] = "https://clips.twitch.tv/embed?clip=FrigidFaithfulLemurCoolCat&autoplay=false"
        }

        /* p {
            + "E eu nunca vou me contentar até conseguir transformar o mundo!"
        } */

        br {}
    }
}