package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import net.perfectdreams.loritta.sweetmorenitta.utils.NitroPayAdGenerator
import net.perfectdreams.loritta.sweetmorenitta.utils.adWrapper
import net.perfectdreams.loritta.sweetmorenitta.utils.generateHowToSponsorButton
import net.perfectdreams.loritta.sweetmorenitta.utils.generateNitroPayAdOrSponsor
import net.perfectdreams.loritta.sweetmorenitta.utils.imgSrcSet
import net.perfectdreams.loritta.sweetmorenitta.views.home.chitChat
import net.perfectdreams.loritta.sweetmorenitta.views.home.community
import net.perfectdreams.loritta.sweetmorenitta.views.home.customization
import net.perfectdreams.loritta.sweetmorenitta.views.home.funnyCommands
import net.perfectdreams.loritta.sweetmorenitta.views.home.makeItAwesome
import net.perfectdreams.loritta.sweetmorenitta.views.home.moderation
import net.perfectdreams.loritta.sweetmorenitta.views.home.muchMore
import net.perfectdreams.loritta.sweetmorenitta.views.home.notify
import net.perfectdreams.loritta.sweetmorenitta.views.home.thankYou
import net.perfectdreams.loritta.sweetmorenitta.views.home.trust

class HomeView(
    locale: BaseLocale,
    path: String
) : NavbarView(
        locale,
        path
) {
    override fun getTitle() = locale["website.jumbotron.tagline"]

    override fun DIV.generateContent() {
        div {
            id = "jumbotron"

            div {
                id = "loritta-selfie"
                // Ordem: Do primeiro (a base) para o último
                imgSrcSet(
                        "${websiteUrl}${versionPrefix}/assets/img/loritta/",
                        "loritta_v2.png",
                        "(max-width: 1366px) 570px",
                        1681,
                        181,
                        100
                )
                imgSrcSet(
                        "${websiteUrl}${versionPrefix}/assets/img/loritta/",
                        "loritta_v2_dark.png",
                        "(max-width: 1366px) 570px",
                        1681,
                        181,
                        100
                ) { classes += "dark-sweater-pose" }
                imgSrcSet(
                        "${websiteUrl}${versionPrefix}/assets/img/loritta/",
                        "loritta_v2_blink.png",
                        "(max-width: 1366px) 570px",
                        1681,
                        181,
                        100
                ) { classes += "blinking-pose" }
                imgSrcSet(
                        "${websiteUrl}${versionPrefix}/assets/img/loritta/",
                        "loritta_v2_blush.png",
                        "(max-width: 1366px) 570px",
                        1681,
                        181,
                        100
                ) { classes += "blushing-pose" }
                // img(src = "${websiteUrl}/v2/assets/img/loritta_v2.png") {}
                // img(src = "${websiteUrl}/v2/assets/img/loritta_v2_dark.png", classes = "dark-sweater-pose") {}
                // img(src = "${websiteUrl}/v2/assets/img/loritta_v2_blink.png", classes = "blinking-pose") {}
                // img(src = "${websiteUrl}/v2/assets/img/loritta_v2_blush.png", classes = "blushing-pose") {}
            }

            div(classes = "right-side-text") {
                div(classes = "introduction") {
                    div(classes = "my-name-is invisible") {
                        + locale["website.jumbotron.hello"]
                    }
                    div(classes = "loritta invisible") {
                        +"Loritta"
                    }
                    div(classes = "tagline invisible") {
                        + locale["website.jumbotron.tagline"]
                    }
                }
                div(classes = "buttons") {
                    div {
                        a(classes = "add-me button pink has-shadow is-big", href = com.mrpowergamerbr.loritta.LorittaLauncher.loritta.discordInstanceConfig.discord.addBotUrl) {
                            img(classes = "lori-happy", src = "${websiteUrl}$versionPrefix/assets/img/lori_happy.gif")
                            i(classes = "fas fa-plus") {}

                            + " ${locale["website.jumbotron.addMe"]}"
                        }

                        a(classes = "button light-green has-shadow is-big", href = "#about-me") {
                            i(classes = "fas fa-star") {}

                            + " ${locale["website.jumbotron.moreInfo"]}"
                        }
                    }
                    div {
                        style = "margin-top: 0.5em;"
                        a(classes = "add-me button purple has-shadow is-big", href = "/${locale.path}/dashboard") {
                            i(classes = "fas fa-cogs") {}

                            + " ${locale["website.jumbotron.dashboard"]}"
                        }
                    }
                }
            }

            div(classes = "bouncy-arrow") {
                i(classes = "fas fa-chevron-down")
            }
        }

        div { id = "about-me" }
        div(classes = "odd-wrapper") {
            adWrapper {
                generateNitroPayAdOrSponsor(0, "home-below-header1", NitroPayAdGenerator.ALL_SIZES)
                generateNitroPayAdOrSponsor(1, "home-below-header2", NitroPayAdGenerator.ALL_SIZES)
            }

            generateHowToSponsorButton(locale)

            div(classes = "media") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        h1 {
                            // style = "font-size: 3.125rem;"
                            + locale["website.home.intro.title"]
                        }

                        p {
                            style = "font-size: 1.25em; text-align: left;"
                            span {
                                style = "text-decoration: underline dotted #fe8129;"
                                + locale["website.home.intro.everyServer"]
                            }
                            + " ${locale["website.home.intro.membersWant"]}"
                        }
                    }

                    div {
                        style = "text-align: left;"
                        for (str in locale.getList("website.home.intro.description")) {
                            p {
                                + str
                            }
                        }
                    }

                    p {
                        style = "font-size: 1.25em; text-align: center; text-decoration: underline dotted #fe8129;"
                        + locale["website.home.intro.itIsThatEasy"]
                    }
                }
                div(classes = "media-figure") {
                    imgSrcSet(
                            "${websiteUrl}${versionPrefix}/assets/img/home/",
                            "lori_gabi.png",
                            "(max-width: 800px) 50vw, 15vw",
                            1278,
                            178,
                            100
                    )
                }
            }
        }

        trust(locale)
        funnyCommands(locale, websiteUrl)
        chitChat(locale, websiteUrl)
        // music(locale, websiteUrl)
        moderation(locale, websiteUrl)
        notify(locale)
        customization(locale)
        community(locale)
        muchMore(locale)
        makeItAwesome(locale)
        thankYou(locale)
    }
}