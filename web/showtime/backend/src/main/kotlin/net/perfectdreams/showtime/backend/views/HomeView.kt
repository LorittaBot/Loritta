package net.perfectdreams.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
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
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.dokyo.elements.HomeElements
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.utils.SVGIconManager
import net.perfectdreams.showtime.backend.utils.WebsiteAssetsHashManager
import net.perfectdreams.showtime.backend.utils.imgSrcSetFromResources
import net.perfectdreams.showtime.backend.views.home.chitChat
import net.perfectdreams.showtime.backend.views.home.community
import net.perfectdreams.showtime.backend.views.home.customization
import net.perfectdreams.showtime.backend.views.home.funnyCommands
import net.perfectdreams.showtime.backend.views.home.makeItAwesome
import net.perfectdreams.showtime.backend.views.home.moderation
import net.perfectdreams.showtime.backend.views.home.muchMore
import net.perfectdreams.showtime.backend.views.home.notify
import net.perfectdreams.showtime.backend.views.home.thankYou
import net.perfectdreams.showtime.backend.views.home.trust

class HomeView(
    websiteTheme: WebsiteTheme,
    iconManager: SVGIconManager,
    hashManager: WebsiteAssetsHashManager,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    websiteTheme,
    iconManager,
    hashManager,
    locale,
    i18nContext,
    path
) {
    override val hasDummyNavbar = false

    override fun getTitle() = locale["website.jumbotron.tagline"]

    override fun DIV.generateContent() {
        div {
            id = "jumbotron"

            div {
                HomeElements.lorittaSelfie.apply(this)

                // Ordem: Do primeiro (a base) para o último
                imgSrcSetFromResources(
                    "${versionPrefix}/assets/img/loritta/loritta_v2.png",
                    "(max-width: 1366px) 570px"
                )
                imgSrcSetFromResources(
                    "${versionPrefix}/assets/img/loritta/loritta_v2_dark.png",
                    "(max-width: 1366px) 570px"
                ) {
                    classes = classes + "dark-sweater-pose"
                }
                imgSrcSetFromResources(
                    "${versionPrefix}/assets/img/loritta/loritta_v2_blink.png",
                    "(max-width: 1366px) 570px"
                ) {
                    HomeElements.blinkingPose.apply(this)
                    classes = classes + "blinking-pose"
                }
                imgSrcSetFromResources(
                    "${versionPrefix}/assets/img/loritta/loritta_v2_blush.png",
                    "(max-width: 1366px) 570px"
                ) {
                    HomeElements.blushingPose.apply(this)
                    classes = classes + "blushing-pose"
                }
                // img(src = "${websiteUrl}/v2/assets/img/loritta_v2.png") {}
                // img(src = "${websiteUrl}/v2/assets/img/loritta_v2_dark.png", classes = "dark-sweater-pose") {}
                // img(src = "${websiteUrl}/v2/assets/img/loritta_v2_blink.png", classes = "blinking-pose") {}
                // img(src = "${websiteUrl}/v2/assets/img/loritta_v2_blush.png", classes = "blushing-pose") {}
            }

            div(classes = "right-side-text") {
                div(classes = "introduction") {
                    div(classes = "my-name-is animated fade-in-right duration-one-second") {
                        + locale["website.jumbotron.hello"]
                    }
                    div(classes = "loritta animated fade-in-right duration-one-second delayed-one-second") {
                        +"Loritta"
                    }
                    div(classes = "tagline animated fade-in-right duration-one-second delayed-two-seconds") {
                        + locale["website.jumbotron.tagline"]
                    }
                }
                div(classes = "buttons") {
                    div {
                        // TODO: Fix
                        a(classes = "add-me button pink has-shadow is-big", href = "https://google.com/") {
                            img(classes = "lori-happy", src = "$versionPrefix/assets/img/lori_happy.gif")
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
                iconManager.chevronDown.apply(this)
            }
        }

        div { id = "about-me" }
        var sectionId = 1
        div(classes = getOddOrEvenClassName(sectionId++)) {
            // generateNitroPayAdOrSponsor(0, "home-below-header1", "Loritta v2 Below Header") { true }
            // generateNitroPayAdOrSponsor(1, "home-below-header2", "Loritta v2 Below Header") { it != NitroPayAdDisplay.PHONE }

            // generateHowToSponsorButton(locale)

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
                    imgSrcSetFromResources(
                        "${versionPrefix}/assets/img/home/lori_gabi.png",
                        "(max-width: 800px) 50vw, 15vw"
                    )
                }
            }
        }

        trust(locale, getOddOrEvenClassName(sectionId++))
        funnyCommands(locale, websiteUrl, getOddOrEvenClassName(sectionId++))
        chitChat(locale, websiteUrl, getOddOrEvenClassName(sectionId++))
        // music(locale, websiteUrl)
        moderation(locale, websiteUrl, getOddOrEvenClassName(sectionId++))
        notify(locale, getOddOrEvenClassName(sectionId++))
        customization(locale, getOddOrEvenClassName(sectionId++))
        community(locale, getOddOrEvenClassName(sectionId++))
        muchMore(locale, getOddOrEvenClassName(sectionId++))
        makeItAwesome(locale, getOddOrEvenClassName(sectionId++))
        thankYou(locale, getOddOrEvenClassName(sectionId++))
    }

    fun getOddOrEvenClassName(id: Int): String {
        if (id % 2 == 1)
            return "odd-wrapper"
        return "even-wrapper"
    }
}