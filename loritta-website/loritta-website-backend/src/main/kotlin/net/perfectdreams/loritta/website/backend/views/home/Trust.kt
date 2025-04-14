package net.perfectdreams.loritta.website.backend.views.home

import kotlinx.html.*
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.EtherealGambiImages
import net.perfectdreams.loritta.website.backend.utils.imgSrcSetFromEtherealGambi
import net.perfectdreams.loritta.common.locale.BaseLocale

data class TrustedEntity(
    val title: String,
    val preloadedImageInfo: EtherealGambiImages.PreloadedImageInfo
)

fun DIV.trust(m: LorittaWebsiteBackend, locale: BaseLocale, sectionClassName: String) {
    val iconsLeftSide = listOf(
        TrustedEntity("Toca do Coelho", m.images.serverIcons.tocaDoCoelho),
        TrustedEntity("k a m a i t a c h i", m.images.serverIcons.kamaitachi),
        TrustedEntity("Detetive YouTuber", m.images.serverIcons.detetiveYoutuber),
        TrustedEntity("AnimesK", m.images.serverIcons.animesk),
        TrustedEntity("Piratas", m.images.serverIcons.piratas),
        TrustedEntity("Servidor da Seita", m.images.serverIcons.servidorDaSeita),
        TrustedEntity("Fábrica de Noobs", m.images.serverIcons.fdn),
        TrustedEntity("Ballerini", m.images.serverIcons.ballerini), // important
        TrustedEntity("Loucademia do Polícia", m.images.serverIcons.loucademiaDoPolicia),
        TrustedEntity("CidCidoso", m.images.serverIcons.cidcidoso),
        TrustedEntity("Maicon Küster OFICIAL", m.images.serverIcons.maiconKuster),
        TrustedEntity("DiogoDefante OFICIAL", m.images.serverIcons.diogoDefante),
        TrustedEntity("dokecord", m.images.serverIcons.dokebu), // important
        TrustedEntity("Servidor dos Mentecaptos", m.images.serverIcons.servidorDosMentecaptos), // important
        TrustedEntity("Riscord do Dik", m.images.serverIcons.rik), // important
        TrustedEntity("LubaTV", m.images.serverIcons.lubatv), // important
        TrustedEntity("Gartic", m.images.serverIcons.gartic), // important
    )

    val iconsRightSide = listOf(
        TrustedEntity("Drawn Mask", m.images.serverIcons.drawnMask), // super important!!
        TrustedEntity("Emiland", m.images.serverIcons.emiland), // important
        TrustedEntity("DISCORD DO CELLBIT", m.images.serverIcons.cellbit), // important
        TrustedEntity("Server do Felpinho", m.images.serverIcons.serverDoFelpinho), // important
        TrustedEntity("República Coisa de Nerd", m.images.serverIcons.republicaCoisaDeNerd), // important
        TrustedEntity("Forever", m.images.serverIcons.forever),
        TrustedEntity("Servidor do Buuf", m.images.serverIcons.buuf),
        TrustedEntity("Servidor da Chelly", m.images.serverIcons.chelly),
        TrustedEntity("Servidor do Oda", m.images.serverIcons.servidorDoOda), // important
        TrustedEntity("Flakes Power", m.images.serverIcons.flakesPower), // important
        TrustedEntity("Rede Dark", m.images.serverIcons.redeDark),
        TrustedEntity("DISCORD DO POLADO", m.images.serverIcons.polado),
        TrustedEntity("Servidor do Berg", m.images.serverIcons.servidorDoBerg),
        TrustedEntity("Colmeia do Abelha", m.images.serverIcons.colmeiaDoAbelha),
        // TrustedEntity("Woods", "/loritta/server-icons/woods.gif"), // important
        TrustedEntity("Kleberianus", m.images.serverIcons.kleberianus), // important
        TrustedEntity("keio", m.images.serverIcons.keio), // important-ish
        // TrustedEntity("Servidor do Neon", "/loritta/server-icons/servidor_do_neon.gif"),
        TrustedEntity("Server Utópico", m.images.serverIcons.serverUtopico),
        // TrustedEntity("Servidor do Sun", m.images.serverIcons.servidorDo"/loritta/server-icons/servidor_do_sun.png")
    ).also { if (iconsLeftSide.size != it.size) error("iconsRightSide is not balanced to iconsLeftSide! Left: ${iconsLeftSide.size} != Right: ${it.size}") }

    val icons = (iconsLeftSide + iconsRightSide)

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
                        imgSrcSetFromEtherealGambi(m, icon.preloadedImageInfo, "png", "6vw") {
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