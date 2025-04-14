package net.perfectdreams.loritta.website.backend.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.dokyo.elements.HomeElements
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.*
import net.perfectdreams.loritta.website.backend.views.home.chitChat
import net.perfectdreams.loritta.website.backend.views.home.community
import net.perfectdreams.loritta.website.backend.views.home.customization
import net.perfectdreams.loritta.website.backend.views.home.funnyCommands
import net.perfectdreams.loritta.website.backend.views.home.makeItAwesome
import net.perfectdreams.loritta.website.backend.views.home.moderation
import net.perfectdreams.loritta.website.backend.views.home.muchMore
import net.perfectdreams.loritta.website.backend.views.home.notify
import net.perfectdreams.loritta.website.backend.views.home.trust

class HomeView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    LorittaWebsiteBackend,
    websiteTheme,
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
                imgSrcSetFromEtherealGambi(
                    LorittaWebsiteBackend,
                    LorittaWebsiteBackend.images.lorittaJumbotronBase,
                    "png",
                    "(max-width: 1366px) 570px"
                )
                imgSrcSetFromEtherealGambi(
                    LorittaWebsiteBackend,
                    LorittaWebsiteBackend.images.lorittaJumbotronDark,
                    "png",
                    "(max-width: 1366px) 570px"
                ) {
                    classes = classes + "dark-sweater-pose"
                }
                imgSrcSetFromEtherealGambi(
                    LorittaWebsiteBackend,
                    LorittaWebsiteBackend.images.lorittaJumbotronEyesOpen,
                    "png",
                    "(max-width: 1366px) 570px"
                ) {
                    classes = classes + "eyes-open-pose"
                }
                imgSrcSetFromEtherealGambi(
                    LorittaWebsiteBackend,
                    LorittaWebsiteBackend.images.lorittaJumbotronBlink,
                    "png",
                    "(max-width: 1366px) 570px"
                ) {
                    HomeElements.blinkingPose.apply(this)
                    classes = classes + "blinking-pose"
                }
                imgSrcSetFromEtherealGambi(
                    LorittaWebsiteBackend,
                    LorittaWebsiteBackend.images.lorittaJumbotronBlush,
                    "png",
                    "(max-width: 1366px) 570px"
                ) {
                    HomeElements.blushingPose.apply(this)
                    classes = classes + "blushing-pose"
                }
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
                        a(classes = "add-me button pink has-shadow is-big", href = LorittaWebsiteBackend.addBotUrl.toString()) {
                            img(classes = "lori-happy", src = "$versionPrefix/assets/img/lori_happy.gif")
                            LorittaWebsiteBackend.svgIconManager.plus.apply(this)

                            + " ${locale["website.jumbotron.addMe"]}"
                        }

                        a(classes = "button light-green has-shadow is-big", href = "#about-me") {
                            LorittaWebsiteBackend.svgIconManager.star.apply(this)

                            + " ${locale["website.jumbotron.moreInfo"]}"
                        }
                    }
                    div {
                        style = "margin-top: 0.5em;"
                        a(classes = "add-me button purple has-shadow is-big", href = "/${locale.path}/dashboard") {
                            LorittaWebsiteBackend.svgIconManager.cogs.apply(this)

                            + " ${locale["website.jumbotron.dashboard"]}"
                        }
                    }
                }
            }

            div(classes = "bouncy-arrow") {
                iconManager.chevronDown.apply(this)
            }
        }

        innerContent {
            div { id = "about-me" }
            var sectionId = 1
            div(classes = getOddOrEvenClassName(sectionId++)) {
                // TODO: Readd ad here

                // generateHowToSponsorButton(locale)

                div(classes = "media") {
                    div(classes = "media-body") {
                        div {
                            style = "text-align: center;"

                            h1 {
                                // style = "font-size: 3.125rem;"
                                +locale["website.home.intro.title"]
                            }

                            p {
                                style = "font-size: 1.25em; text-align: left;"
                                span {
                                    style = "text-decoration: underline dotted #fe8129;"
                                    +locale["website.home.intro.everyServer"]
                                }
                                +" ${locale["website.home.intro.membersWant"]}"
                            }
                        }

                        div {
                            style = "text-align: left;"
                            for (str in locale.getList("website.home.intro.description")) {
                                p {
                                    +str
                                }
                            }
                        }

                        p {
                            style =
                                "font-size: 1.25em; text-align: center; text-decoration: underline dotted #fe8129;"
                            +locale["website.home.intro.itIsThatEasy"]
                        }
                    }
                    div(classes = "media-figure") {
                        imgSrcSetFromEtherealGambi(
                            LorittaWebsiteBackend,
                            LorittaWebsiteBackend.images.lorittaGabi,
                            "png",
                            "(max-width: 800px) 50vw, 15vw"
                        )
                    }
                }
            }

            var imageOnTheRightSide = false
            fun getAndAlternate(): Boolean {
                val original = imageOnTheRightSide
                imageOnTheRightSide = !imageOnTheRightSide
                return original
            }

            trust(LorittaWebsiteBackend, locale, getOddOrEvenClassName(sectionId++))
            funnyCommands(LorittaWebsiteBackend, iconManager, locale, getOddOrEvenClassName(sectionId++), getAndAlternate())
            chitChat(LorittaWebsiteBackend, locale, getOddOrEvenClassName(sectionId++), getAndAlternate())
            // music(locale, websiteUrl)
            moderation(locale, getOddOrEvenClassName(sectionId++), getAndAlternate())
            notify(LorittaWebsiteBackend, iconManager, locale, getOddOrEvenClassName(sectionId++), getAndAlternate())
            customization(locale, getOddOrEvenClassName(sectionId++), getAndAlternate())
            community(LorittaWebsiteBackend, iconManager, locale, getOddOrEvenClassName(sectionId++), getAndAlternate())
            muchMore(locale, getOddOrEvenClassName(sectionId++))
            makeItAwesome(LorittaWebsiteBackend, locale, getOddOrEvenClassName(sectionId++))
            // Disabled for now because our YourKit license is expired because I need to ask to renew it
            // (or maybe just buy YourKit?)
            // thankYou(locale, getOddOrEvenClassName(sectionId++))
        }
    }

    fun getOddOrEvenClassName(id: Int): String {
        if (id % 2 == 1)
            return "odd-wrapper"
        return "even-wrapper"
    }
}