package net.perfectdreams.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.dokyo.elements.HomeElements
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import java.time.LocalDate

abstract class NavbarView(
    showtimeBackend: ShowtimeBackend,
    val websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : BaseView(
    showtimeBackend,
    locale,
    i18nContext,
    path
) {
    /**
     * If the page should render the navigation bar
     */
    open val hasNavbar = true

    /**
     * If the page should render the footer
     */
    open val hasFooter = true

    /**
     * If the page should render the dummy (fake/empty) navbar
     */
    open val hasDummyNavbar = true

    override fun HTML.generateBody() {
        val base = "/${locale.path}"

        body(classes = websiteTheme.bodyClass) {
            div {
                HomeElements.progressIndicator.apply(this)
                attributes["data-preload-persist"] = "true"
            }

            if (hasNavbar) {
                nav(classes = "navigation-bar fixed") {
                    attributes["data-preload-persist"] = "true"
                    id = "navigation-bar"

                    div(classes = "left-side-entries") {
                        div(classes = "entry loritta-navbar-logo") {
                            a(classes = "home-page", href = "$base/") {
                                // attributes["data-preload-link"] = "true"
                                style = "font-family: 'Pacifico', cursive; text-transform: none;"

                                +"Loritta"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "support", href = "$base/support") {
                                // attributes["data-preload-link"] = "true"

                                iconManager.discord.apply(this)

                                +" ${locale["website.navbar.support"]}"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "commands", href = "$base/commands/slash") {
                                attributes["data-preload-link"] = "true"

                                iconManager.terminal.apply(this)

                                +" ${locale["modules.sectionNames.commands"]}"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "fan-arts", href = "$base/fanarts") {
                                iconManager.paintBrush.apply(this)

                                +" Fan Arts"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "donate", href = "$base/donate") {
                                // attributes["data-preload-link"] = "true"

                                iconManager.gift.apply(this)

                                +" Premium"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "extras", href = "$base/extras") {
                                attributes["data-preload-link"] = "true"

                                iconManager.star.apply(this)

                                +" Wiki"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "blog", href = "$base/blog") {
                                iconManager.newspaper.apply(this)

                                +" Blog"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "sponsors", href = "$base/sponsors") {
                                iconManager.heart.apply(this)

                                +" ${locale["website.navbar.sponsors"]}"
                            }
                        }

                        /*  a(classes = "lori-stickers", href = "https://produto.mercadolivre.com.br/MLB-1366127151-caneca-pster-da-loritta-morenitta-novembro-2019-_JM?quantity=1") {
                        i(classes = "fas fa-heart") {}
                        attributes["target"] = "_blank"

                        +" Merch"
                    }
                    a(classes = "lori-stickers", href = "$base/extras") {
                        i(classes = "far fa-grin-squint-tears") {}
                        attributes["target"] = "_blank"

                        +" Stickers"
                    } */
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

                        // ===[ THEME CHANGER BUTTON ]===
                        div(classes = "entry") {
                            a(classes = "theme-changer") {
                                id = "theme-changer-button"

                                span {
                                    if (websiteTheme != WebsiteTheme.DEFAULT)
                                        style = "display: none;"
                                    iconManager.moon.apply(this)
                                }

                                span {
                                    if (websiteTheme != WebsiteTheme.DARK_THEME)
                                        style = "display: none;"

                                    iconManager.sun.apply(this)
                                }
                            }
                        }

                        // ===[ LOCALE CHANGER BUTTON ]===
                        div(classes = "entry") {
                            id = "locale-changer-button"
                            style = "cursor: default;"

                            iconManager.globe.apply(this)

                            +" "

                            +locale["loritta.languageShort"]

                            +" "

                            span {
                                style = "opacity: 0.7;"

                                iconManager.chevronDown.apply(this)
                            }

                            div {
                                id = "languages"

                                div {
                                    a(href = "/br$path") {
                                        + "Português"
                                    }
                                }
                                hr {}
                                div {
                                    a(href = "/us$path") {
                                        + "English"
                                    }
                                }
                            }
                        }

                        // ===[ LOGIN BUTTON ]===
                        div(classes = "entry") {
                            // TODO: Fix
                            a(href = "/dashboard") {
                                id = "login-button"
                                iconManager.signIn.apply(this)

                                +" Login"
                            }
                        }

                        // ===[ HAMBURGER BUTTON ]===
                        div(classes = "entry") {
                            id = "hamburger-menu-button"

                            iconManager.bars.apply(this)
                        }
                    }
                }

                if (hasDummyNavbar) {
                    div(classes = "dummy-navigation-bar") {
                        id = "dummy-navbar"
                        style = "height: 46px;"
                    }
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
                                iconManager.github.apply(this)
                            }

                            a(href = "https://twitter.com/LorittaBot") {
                                iconManager.twitter.apply(this)
                            }

                            a(href = "https://instagram.com/lorittabot") {
                                iconManager.instagram.apply(this)
                            }
                        }

                        nav(classes = "navigation-footer") {
                            div(classes = "section-entry") {
                                h3 {
                                    +"Loritta Bot"
                                }

                                a(href = "$base/") {
                                    +locale["website.navbar.home"]
                                }
                                a(href = "$base/discord-bot-brasileiro") {
                                    +"Loritta: Apenas um simples bot brasileiro para o Discord"
                                }
                                a(href = "$base/dashboard") {
                                    +locale["website.navbar.dashboard"]
                                }
                                a(href = "$base/support") {
                                    +locale["website.navbar.support"]
                                }
                                a(href = "$base/commands/slash") {
                                    +locale["modules.sectionNames.commands"]
                                }
                                a(href = "$base/donate") {
                                    +"Premium"
                                }
                                a(href = "$base/sponsors") {
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
                                a(href = "$base/fanarts") {
                                    +"Fan Arts"
                                }
                                a(href = "$base/blog") {
                                    +"Blog"
                                }
                                // TODO: Fix
                                /* a(href = "https://produto.mercadolivre.com.br/MLB-1366127151-caneca-pster-da-loritta-morenitta-novembro-2019-_JM?quantity=1") {
                                    +"Merch"
                                } */
                            }

                            div(classes = "section-entry") {
                                h3 {
                                    +locale["website.footer.sectionTitles.resources"]
                                }

                                a(href = "$base/guidelines") {
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
                            iconManager.perfectDreams.apply(this) {
                                this.attr("style", "height: 1em; width: auto;")
                            }
                            +" "
                            +"2017-${LocalDate.now().year}"
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
                                a(classes = "add-me button pink shadow big", href = showtimeBackend.addBotUrl.toString()) {
                                    style = "font-size: 1.5em;"

                                    showtimeBackend.svgIconManager.plus.apply(this)

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