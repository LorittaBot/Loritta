package net.perfectdreams.loritta.sweetmorenitta.views

import com.google.gson.JsonArray
import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.*
import net.perfectdreams.loritta.sweetmorenitta.utils.imgSrcSet
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession

class DonateView(
    locale: BaseLocale,
    path: String,
    val userIdentification: LorittaJsonWebSession.UserIdentification?,
    val keys: JsonArray
) : NavbarView(
        locale,
        path
) {
    override fun getTitle() = locale["website.donate.title"]

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            div {
                style = "text-align: center;"
                h1(classes = "sectionHeader") {
                    style = "font-size: 50px;"
                    + locale["website.donate.needYourHelp"]
                }

                h2(classes = "sectionHeader") {
                    style = "font-size: 30px;"
                    + locale["website.donate.stayAwesome"]
                }
            }

            div(classes = "media") {
                div(classes = "media-figure") {
                    imgSrcSet(
                            "${websiteUrl}${versionPrefix}/assets/img/donate/",
                            "lori_donate.png",
                            "(max-width: 800px) 50vw, 15vw",
                            1272,
                            272,
                            100
                    )
                    // img(src = "${websiteUrl}/assets/img/loritta_pobre.png", alt = "Loritta Pobre") {}
                }

                div(classes = "media-body") {
                    h2(classes = "sectionHeader") {
                        + locale["website.donate.title"]
                    }

                    for (text in locale.getList("website.donate.introDonate")) {
                        p {
                            unsafe {
                                + text
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

                        if (keys.size() != 0) {
                            div(classes = "button-discord button-discord-info pure-button") {
                                id = "renew-button"
                                style = "font-size: 1.5em;"

                                i(classes = "fas fa-sync-alt") {}
                                + " ${locale["website.donate.renewPlan"]}"
                            }
                        }
                    }

                    div {
                        style = "text-align: center; margin: 8px;"

                        video {
                            controls = true
                            width = "400"
                            source {
                                src = "https://cdn.discordapp.com/attachments/510601125221761054/534473346642083851/Lorisemdinheiro.mp4"
                                type = "video/mp4"
                            }
                            + "Your browser does not support HTML5 video."
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
                                + locale["website.donate.donationBenefits"]
                            }

                            p {
                                + locale["website.donate.benefitsExplain"]
                            }

                            p {
                                a(href = "/sponsors", target = "_blank") {
                                    + locale["website.donate.benefitsSponsor"]
                                }
                            }
                        }

                        div(classes = "sectionText") {
                            div {
                                style = "text-align: center;"
                                h2(classes = "sectionHeader") {
                                    + "Nossos Planos"
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
                        + locale["website.donate.thanksToEveryone"]
                    }

                    /* img(src = "https://loritta.website/assets/img/loritta_pudim.png", alt = "Loritta com um pudim na mão", classes = "animate-on-scroll-up is-invisible") {
                        height = "300"
                    } */
                }
            }
        }

        div {
            id = "donation-keys-json"
            style = "display: none;"

            unsafe {
                + keys.toString()
            }
        }
    }
}