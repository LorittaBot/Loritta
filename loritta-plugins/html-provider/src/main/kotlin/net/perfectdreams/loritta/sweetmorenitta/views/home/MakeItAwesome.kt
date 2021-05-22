package net.perfectdreams.loritta.sweetmorenitta.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.*
import net.perfectdreams.loritta.sweetmorenitta.utils.generateAd
import net.perfectdreams.loritta.sweetmorenitta.utils.imgSrcSet
import net.perfectdreams.loritta.sweetmorenitta.views.BaseView

fun DIV.makeItAwesome(locale: BaseLocale, viewMoreFeatures: Boolean = false) {
    div(classes = "even-wrapper wobbly-bg") {
        style = "text-align: center; padding-bottom: 64px;"

        h1 {
            + locale["website.home.makeItAwesome.title"]
        }

        a(classes = "add-me button pink shadow big", href = com.mrpowergamerbr.loritta.LorittaLauncher.loritta.discordInstanceConfig.discord.addBotUrl) {
            style = "font-size: 1.5em;"

            i(classes = "fas fa-plus") {}

            + " ${locale["website.jumbotron.addMe"]}"
        }

        if (viewMoreFeatures) {
            a(classes = "add-me button light-green shadow big", href = "/${locale.path}/") {
                style = "font-size: 1.5em;"

                i(classes = "fas fa-star") {}

                attributes["data-enable-link-preload"] = "true"
                + " ${locale["website.jumbotron.viewMoreFeatures"]}"
            }
        }
    }
}