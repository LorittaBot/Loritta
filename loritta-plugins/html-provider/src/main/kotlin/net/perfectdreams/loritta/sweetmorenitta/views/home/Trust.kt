package net.perfectdreams.loritta.sweetmorenitta.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.*

var icons = listOf(
        "/v2/assets/img/trust/cellbit.png", // Cellbit
        "/v2/assets/img/trust/felps.png", // Felps
        "/v2/assets/img/trust/gartic.png", // Gartic
        "/v2/assets/img/trust/loud.png", // Loud
        "/v2/assets/img/trust/dudscord.png", // Dudscord
        "/v2/assets/img/trust/chavecu.png", // Flakes
        "/v2/assets/img/trust/flakes_power.png", // Flakes
        "/v2/assets/img/trust/drawn_mask.png", // Drawn Mask
        "/v2/assets/img/trust/driscord.png", // Driscord
        "/v2/assets/img/trust/mikaru.png", // Mikaru
        "/v2/assets/img/trust/furry_amino.png", // Furry Amino
        "/v2/assets/img/trust/brksedu.png", // brksedu
        "/v2/assets/img/trust/lubatv.png", // LubaTV
        "/v2/assets/img/trust/mbj.png" // MBJ
)

fun DIV.trust(locale: BaseLocale) {
    div(classes = "even-wrapper wobbly-bg") {
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
            attributes["frameborder"] = "0"
            attributes["allowfullscreen"] = "true"
            attributes["allow"] = "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            attributes["height"] = "279"
            attributes["width"] = "496"
            attributes["lazy-load-url"] = "https://www.youtube.com/embed/PwTKQyiTBVU"
        }

        // Chavecu Twitch Clip
        iframe {
            attributes["frameborder"] = "0"
            attributes["allowfullscreen"] = "true"
            attributes["height"] = "279"
            attributes["width"] = "496"
            attributes["lazy-load-url"] = "https://clips.twitch.tv/embed?clip=FrigidFaithfulLemurCoolCat&autoplay=false&parent=canary.loritta.website&parent=loritta.website"
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