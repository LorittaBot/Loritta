package net.perfectdreams.loritta.cinnamon.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.br
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.iframe
import kotlinx.html.img
import kotlinx.html.style
import kotlinx.html.title
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.imgSrcSetFromResourcesOrFallbackToImgIfNotPresent

val iconsLeftSide = listOf(
    TrustedEntity("Família Zezão", "/v3/assets/img/trust/zezao.png"),
    TrustedEntity("Mundinho da imKary", "/v3/assets/img/trust/imkary.png"),
    TrustedEntity("AnimesK", "/v3/assets/img/trust/animesk.png"),
    TrustedEntity("Toca do Coelho", "/v3/assets/img/trust/toca_do_coelho.png"),
    TrustedEntity("k a m a i t a c h i", "/v3/assets/img/trust/kamaitachi.png"),
    TrustedEntity("Detetive YouTuber", "/v3/assets/img/trust/detetive_youtuber.png"),
    TrustedEntity("EI MINE • Discord", "/v3/assets/img/trust/eimine.png"),
    TrustedEntity("Piratas", "/v3/assets/img/trust/piratas.png"),
    TrustedEntity("Servidor da Seita", "/v3/assets/img/trust/servidor_da_seita.png"),
    TrustedEntity("Fábrica de Noobs", "/v3/assets/img/trust/fdn.png"),
    TrustedEntity("Ballerini", "/v3/assets/img/trust/ballerini.png"), // important
    TrustedEntity("Loucademia do Polícia", "/v3/assets/img/trust/loucademia_do_policia.png"),
    TrustedEntity("CidCidoso", "/v3/assets/img/trust/cidcidoso.png"),
    TrustedEntity("Maicon Küster OFICIAL", "/v3/assets/img/trust/maicon_kuster.png"),
    TrustedEntity("DiogoDefante OFICIAL", "/v3/assets/img/trust/diogo_defante.png"),
    TrustedEntity("dokecord", "/v3/assets/img/trust/dokebu.png"), // important
    TrustedEntity("Servidor dos Mentecaptos", "/v3/assets/img/trust/servidor_dos_mentecaptos.png"), // important
    TrustedEntity("Riscord do Dik", "/v3/assets/img/trust/rik.png"), // important
    TrustedEntity("LubaTV", "/v3/assets/img/trust/lubatv.png"), // important
    TrustedEntity("Gartic", "/v3/assets/img/trust/gartic.png"), // important
)

val iconsRightSide = listOf(
    TrustedEntity("Drawn Mask", "/v3/assets/img/trust/drawn_mask.jpg"), // super important!!
    TrustedEntity("Emiland", "/v3/assets/img/trust/emiland.png"), // important
    TrustedEntity("DISCORD DO CELLBIT", "/v3/assets/img/trust/cellbit.png"), // important
    TrustedEntity("Server do Felpinho", "/v3/assets/img/trust/server_do_felpinho.png"), // important
    TrustedEntity("República Coisa de Nerd", "/v3/assets/img/trust/republica_coisa_de_nerd.png"), // important
    TrustedEntity("Forever", "/v3/assets/img/trust/forever.png"),
    TrustedEntity("Servidor do Buuf", "/v3/assets/img/trust/buuf.png"),
    TrustedEntity("Servidor da Chelly", "/v3/assets/img/trust/chelly.png"),
    TrustedEntity("Servidor do Oda", "/v3/assets/img/trust/servidor_do_oda.png"), // important
    TrustedEntity("Flakes Power", "/v3/assets/img/trust/flakes_power.png"), // important
    TrustedEntity("Rede Dark", "/v3/assets/img/trust/rede_dark.png"),
    TrustedEntity("DISCORD DO POLADO", "/v3/assets/img/trust/polado.png"),
    TrustedEntity("Servidor do Berg", "/v3/assets/img/trust/servidor_do_berg.png"),
    TrustedEntity("Colmeia do Abelha", "/v3/assets/img/trust/colmeia_do_abelha.png"),
    TrustedEntity("Woods", "/v3/assets/img/trust/woods.gif"), // important
    TrustedEntity("Kleberianus", "/v3/assets/img/trust/kleberianus.png"), // important
    TrustedEntity("keio", "/v3/assets/img/trust/keio.png"), // important-ish
    TrustedEntity("Servidor do Neon", "/v3/assets/img/trust/servidor_do_neon.gif"),
    TrustedEntity("Server Utópico", "/v3/assets/img/trust/server_utopico.png"),
    TrustedEntity("Servidor do Sun", "/v3/assets/img/trust/servidor_do_sun.png")
).also { if (iconsLeftSide.size != it.size) error("iconsRightSide is not balanced to iconsLeftSide! Left: ${iconsLeftSide.size} != Right: ${it.size}") }

val icons = (iconsLeftSide + iconsRightSide)

data class TrustedEntity(
    val title: String,
    val path: String
)

fun DIV.trust(locale: BaseLocale, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        style = "text-align: center;"

        h1 {
            + locale["website.home.trust.title"]
        }

        div(classes = "guild-icons-horizontal") {
            // We will only take 20 icons instead of all, to avoid adding icons to the DOM that are outside of most monitors' viewports
            val middle = icons.size / 2
            val icons = icons.subList(middle - 10, middle + 10).withIndex()

            // We need to duplicate the DOM for the Marquee effect
            repeat(2) {
                div(classes = "guilds-wrapper") {
                    for ((idx, icon) in icons) {
                        val classNames = when (idx % 4) {
                            1, 3 -> "icon-middle"
                            2 -> "icon-top"
                            else -> "icon-bottom"
                        }

                        // Checked the sizes with Firefox: Put "6vw" and hover the image URL in Firefox's inspect tab
                        imgSrcSetFromResourcesOrFallbackToImgIfNotPresent(icon.path, "6vw") {
                            classes = setOf(classNames)
                            title = icon.title
                            attributes["loading"] = "lazy"
                            width = "96"
                            style = "border-radius: 9999px; margin-left: 4px; margin-right: 4px;"
                        }
                    }
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
                    img(classes = classNames, src = icon.path) {
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