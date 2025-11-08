package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.hr
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.unsafe
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL
import net.perfectdreams.loritta.morenitta.websitedashboard.AuthenticationState
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.AuthenticationStateUtils

abstract class NavbarView(
    val loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String
) : BaseView(
    i18nContext,
    locale,
    path
) {
    open val hasNavbar = true
    open val hasFooter = true

    override fun HTML.generateBody() {
        val base = "/${locale.path}"

        body {
            div {
                id = "loading-screen"
                img(src = "https://loritta.website/assets/img/loritta_loading.png", alt = "Loading Spinner")
                div(classes = "loading-text") {
                    +"Carregando..."
                }
            }

            if (hasNavbar) {
                nav(classes = "navigation-bar fixed") {
                    id = "navigation-bar"

                    div(classes = "left-side-entries") {
                        div(classes = "entry loritta-navbar-logo") {
                            a(classes = "home-page", href = "$base/") {
                                style = "font-family: 'Pacifico', cursive; text-transform: none;"

                                attributes["data-enable-link-preload"] = "true"

                                +"Loritta"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "support", href = "$base/support") {
                                attributes["data-enable-link-preload"] = "true"
                                attributes["data-sweet-page"] = "support"

                                i(classes = "fab fa-discord") {}

                                +" ${locale["website.navbar.support"]}"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "support", href = "$base/commands") {
                                // attributes["data-enable-link-preload"] = "true"
                                // attributes["data-sweet-page"] = "commands"

                                i(classes = "fas fa-terminal") {}

                                +" ${locale["modules.sectionNames.commands"]}"
                            }
                        }

                        // Daily
                        div(classes = "entry") {
                            a(classes = "daily", href = "$base/daily") {
                                i(classes = "fas fa-gift") {}

                                +" Daily"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "merch", href = "${loritta.config.loritta.dashboard.url.removeSuffix("/")}/sonhos-shop") {
                                i(classes = "fas fa-shopping-cart") {}
                                +" Lojinha de Sonhos"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "donate", href = "$base/donate") {
                                unsafe {
                                    // Font Awesome's "Sparkles" icon is Pro only smh
                                    raw("""
                                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 36 36" style="display: inline-block;width: 1em;height: 1em;stroke-width: 0;stroke: currentColor;fill: currentColor;font-size: inherit;color: inherit;vertical-align: -0.125em;filter: drop-shadow(0 1px rgba(0, 0, 0, 0.2));">
                                        <path d="M34.347 16.893l-8.899-3.294-3.323-10.891c-.128-.42-.517-.708-.956-.708-.439 0-.828.288-.956.708l-3.322 10.891-8.9 3.294c-.393.146-.653.519-.653.938 0 .418.26.793.653.938l8.895 3.293 3.324 11.223c.126.424.516.715.959.715.442 0 .833-.291.959-.716l3.324-11.223 8.896-3.293c.391-.144.652-.518.652-.937 0-.418-.261-.792-.653-.938z"></path><path d="M14.347 27.894l-2.314-.856-.9-3.3c-.118-.436-.513-.738-.964-.738-.451 0-.846.302-.965.737l-.9 3.3-2.313.856c-.393.145-.653.52-.653.938 0 .418.26.793.653.938l2.301.853.907 3.622c.112.444.511.756.97.756.459 0 .858-.312.97-.757l.907-3.622 2.301-.853c.393-.144.653-.519.653-.937 0-.418-.26-.793-.653-.937zM10.009 6.231l-2.364-.875-.876-2.365c-.145-.393-.519-.653-.938-.653-.418 0-.792.26-.938.653l-.875 2.365-2.365.875c-.393.146-.653.52-.653.938 0 .418.26.793.653.938l2.365.875.875 2.365c.146.393.52.653.938.653.418 0 .792-.26.938-.653l.875-2.365 2.365-.875c.393-.146.653-.52.653-.938 0-.418-.26-.792-.653-.938z"></path>
                                        </svg>
                                    """.trimIndent())
                                }

                                +" Premium"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "extras", href = "$base/extras") {
                                i(classes = "fas fa-book") {}
                                +" Wiki"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "fan-arts", href = "https://fanarts.perfectdreams.net/") {
                                i(classes = "fas fa-paint-brush") {}

                                +" Fan Arts"
                            }
                        }
                    }

                    div(classes = "right-side-entries") {
                        /* a(href = "https://twitter.com/LorittaBot", classes = "social-media-link") {
                        attributes["target"] = "_blank"

                        i(classes = "fab fa-twitter") {}
                    }
                    a(href = "https://instagram.com/lorittabot/", classes = "social-media-link") {
                        attributes["target"] = "_blank"

                        i(classes = "fab fa-instagram") {}
                    } */

                        div(classes = "entry") {
                            a(classes = "theme-changer") {
                                id = "theme-changer-button"

                                i(classes = "fas fa-moon") {}
                            }
                        }
                        div(classes = "entry") {
                            id = "locale-changer-button"
                            style = "cursor: default;"

                            i(classes = "fas fa-globe-americas") {}

                            +" "

                            +locale["loritta.languageShort"]

                            +" "

                            i(classes = "fas fa-chevron-down") {
                                style = "opacity: 0.7;"
                            }

                            div {
                                id = "languages"
                            }
                        }

                        div(classes = "entry") {
                            a {
                                id = "login-button"
                                i(classes = "fas fa-sign-in-alt") {}

                                +" Login"
                            }
                        }
                        div(classes = "entry") {
                            id = "hamburger-menu-button"

                            i(classes = "fas fa-bars") {}
                        }
                    }
                }
                div(classes = "dummy-navigation-bar") {
                    id = "dummy-navbar"
                    style = "height: 46px;"
                }
            }

            div {
                id = "content"

                generateContent()
            }

            if (hasFooter) {
                footer {
                    div {
                        div(classes = "social-networks") {
                            a(href = "https://github.com/LorittaBot") {
                                i(classes = "fab fa-github") {}
                            }

                            a(href = "https://x.com/LorittaBot") {
                                i(classes = "fab fa-square-x-twitter") {}
                            }

                            a(href = "https://bsky.app/profile/loritta.website") {
                                i(classes = "fab fa-bluesky") {}
                            }

                            a(href = "https://instagram.com/lorittabot") {
                                i(classes = "fab fa-instagram") {}
                            }
                        }

                        nav(classes = "navigation-footer") {
                            div(classes = "section-entry") {
                                h3 {
                                    +"Loritta Bot"
                                }

                                a(href = "$base/") {
                                    attributes["data-enable-link-preload"] = "true"
                                    +locale["website.navbar.home"]
                                }
                                a(href = "$base/discord-bot-brasileiro") {
                                    attributes["data-enable-link-preload"] = "true"
                                    +"Loritta: Apenas um simples bot brasileiro para o Discord"
                                }
                                a(href = loritta.config.loritta.dashboard.url) {
                                    +locale["website.navbar.dashboard"]
                                }
                                a(href = "$base/support") {
                                    attributes["data-enable-link-preload"] = "true"
                                    +locale["website.navbar.support"]
                                }
                                a(href = "$base/commands") {
                                    // attributes["data-enable-link-preload"] = "true"
                                    +locale["modules.sectionNames.commands"]
                                }
                                a(href = "$base/donate") {
                                    attributes["data-enable-link-preload"] = "true"
                                    +"Premium"
                                }
                                a(href = "$base/sponsors") {
                                    attributes["data-enable-link-preload"] = "true"
                                    +locale["website.navbar.sponsors"]
                                }
                                a(href = "$base/daily") {
                                    +"Daily"
                                }
                                a(href = "$base/extras/about-loritta-bot") {
                                    +"Sobre a Loritta (Bot)"
                                }
                            }

                            div(classes = "section-entry") {
                                h3 {
                                    +"Loritta Morenitta"
                                }

                                a(href = "$base/extras/about-loritta-character") {
                                    +"Sobre a Loritta (Personagem)"
                                }
                                a(href = "https://fanarts.perfectdreams.net/") {
                                    +"Fan Arts"
                                }
                                a(href = "$base/blog") {
                                    attributes["data-enable-link-preload"] = "true"
                                    +"Blog"
                                }
                                a(href = "https://produto.mercadolivre.com.br/MLB-1366127151-caneca-pster-da-loritta-morenitta-novembro-2019-_JM?quantity=1") {
                                    +"Merch"
                                }
                            }

                            div(classes = "section-entry") {
                                h3 {
                                    +locale["website.footer.sectionTitles.resources"]
                                }

                                a(href = "$base/guidelines") {
                                    attributes["data-enable-link-preload"] = "true"
                                    +locale["website.guidelines.communityGuidelines"]
                                }

                                a(href = "$base/privacy") {
                                    +"${locale["website.footer.sectionNames.termsOfService"]} & ${locale["website.footer.sectionNames.privacyPolicy"]}"
                                }
                            }

                            div(classes = "section-entry") {
                                h3 {
                                    +locale["website.footer.sectionTitles.beyondLoritta"]
                                }

                                a(href = "https://sparklypower.net/") {
                                    +"SparklyPower: Servidor de Minecraft"
                                }

                                a(href = "https://perfectdreams.net/") {
                                    +"PerfectDreams"
                                }

                                a(href = "https://mrpowergamerbr.com/") {
                                    +"MrPowerGamerBR Website"
                                }
                            }
                        }

                        p {
                            style = "text-align: center;"

                            +"© "
                            +"MrPowerGamerBR"
                            +" & "
                            +"PerfectDreams"
                            +" "
                            +"2017-"
                            script {
                                unsafe {
                                    raw("""document.write(new Date().getFullYear());""")
                                }
                            }
                            +" — "
                            +locale["website.footer.allRightsReserved"]
                        }

                        hr {}

                        div(classes = "call-to-action-footer") {
                            div(classes = "lets-transform") {
                                h2 {
                                    +locale["website.home.makeItAwesome.title"]
                                }
                                h3 {
                                    +locale["website.footer.joinUsAndAddLoritta"]
                                }
                            }

                            div(classes = "add-cta") {
                                a(classes = "add-me button pink shadow big", href = LorittaDiscordOAuth2AddBotURL(
                                    loritta,
                                    state = AuthenticationStateUtils.createStateAsBase64(
                                        AuthenticationState(
                                            source = "website",
                                            medium = "button",
                                            campaign = null,
                                            content = "footer"
                                        ),
                                        loritta
                                    )
                                ).toString()) {
                                    style = "font-size: 1.5em;"

                                    i(classes = "fas fa-plus") {}

                                    +" ${locale["website.jumbotron.addMe"]}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    abstract fun DIV.generateContent()
}