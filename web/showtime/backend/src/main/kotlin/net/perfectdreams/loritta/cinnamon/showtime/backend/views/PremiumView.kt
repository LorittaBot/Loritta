package net.perfectdreams.loritta.cinnamon.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.source
import kotlinx.html.style
import kotlinx.html.unsafe
import kotlinx.html.video
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.imgSrcSetFromResources
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.innerContent

class PremiumView(
    showtimeBackend: ShowtimeBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    showtimeBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override fun getTitle() = locale["modules.sectionNames.commands"]

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div {
                    style = "text-align: center;"
                    h1(classes = "sectionHeader") {
                        style = "font-size: 50px;"
                        +locale["website.donate.needYourHelp"]
                    }

                    h2(classes = "sectionHeader") {
                        style = "font-size: 30px;"
                        +locale["website.donate.stayAwesome"]
                    }
                }

                div(classes = "media") {
                    div(classes = "media-figure") {
                        imgSrcSetFromResources(
                            "${versionPrefix}/assets/img/donate/lori_donate.png",
                            "(max-width: 800px) 50vw, 15vw"
                        )
                        // img(src = "${websiteUrl}/assets/img/loritta_pobre.png", alt = "Loritta Pobre") {}
                    }

                    div(classes = "media-body") {
                        h2(classes = "sectionHeader") {
                            +locale["website.donate.title"]
                        }

                        for (text in locale.getList("website.donate.introDonate")) {
                            p {
                                unsafe {
                                    +text
                                }
                            }
                        }

                        div {
                            style = "text-align: center;"

                            a(href = "#plans-features") {
                                div(classes = "button-discord button-discord-info pure-button") {
                                    style = "font-size: 1.5em;"

                                    i(classes = "fas fa-list") {}
                                    +" ${locale["website.donate.viewPlans"]}"
                                }
                            }

                            /* if (keys.size() != 0) {
                            div(classes = "button-discord button-discord-info pure-button") {
                                id = "renew-button"
                                style = "font-size: 1.5em;"

                                i(classes = "fas fa-sync-alt") {}
                                + " ${locale["website.donate.renewPlan"]}"
                            }
                        } */
                        }

                        div {
                            style = "text-align: center; margin: 8px;"

                            video {
                                controls = true
                                width = "400"
                                source {
                                    src =
                                        "https://cdn.discordapp.com/attachments/510601125221761054/534473346642083851/Lorisemdinheiro.mp4"
                                    type = "video/mp4"
                                }
                                +"Your browser does not support HTML5 video."
                            }
                        }
                    }
                }
            }

            div(classes = "even-wrapper wobbly-bg") {
                div(classes = "media") {
                    div(classes = "vertically-centered-content") {
                        style = "max-width: 100%;"
                        div(classes = "sectionText") {
                            div {
                                style = "text-align: center;"
                                h2(classes = "sectionHeader") {
                                    +locale["website.donate.donationBenefits"]
                                }

                                p {
                                    +locale["website.donate.benefitsExplain"]
                                }

                                p {
                                    a(href = "/sponsors", target = "_blank") {
                                        +locale["website.donate.benefitsSponsor"]
                                    }
                                }
                            }

                            div(classes = "sectionText") {
                                div {
                                    style = "text-align: center;"
                                    h2(classes = "sectionHeader") {
                                        +"Nossos Planos"
                                    }
                                }
                            }

                            div {
                                id = "plans-features"
                                style = "margin: 0 auto"
                            }
                        }
                    }
                }
            }

            div(classes = "odd-wrapper wobbly-bg") {
                div(classes = "content-wrapper") {
                    style = "max-width: 100%;"
                    div(classes = "sectionText") {
                        id = "top-donators-scoreboard-wrapper"
                        style = "text-align: center;"

                        h2(classes = "sectionHeader") {
                            +locale["website.donate.thanksToEveryone"]
                        }

                        /* img(src = "https://loritta.website/assets/img/loritta_pudim.png", alt = "Loritta com um pudim na mão", classes = "animate-on-scroll-up is-invisible") {
                        height = "300"
                    } */
                    }
                }
            }

            /* div {
            id = "donation-keys-json"
            style = "display: none;"

            unsafe {
                + keys.toString()
            }
        } */
        }
    }
}