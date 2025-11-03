package net.perfectdreams.loritta.website.backend.views

import kotlinx.html.*
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.dokyo.elements.HomeElements
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import java.time.LocalDate

abstract class NavbarView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    val websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : BaseView(
    LorittaWebsiteBackend,
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
                attributes["bliss-preserve"] = "true"
            }

            if (hasNavbar) {
                nav(classes = "navigation-bar fixed") {
                    attributes["bliss-preserve"] = "true"
                    id = "navigation-bar"

                    // We intentionally don't use hx-select here, if you do use it and select body, the page will be empty
                    // While browsers are smart enough nowadays to not require this due to paint holding
                    // We use it because we don't want to "undo" the navbar every time the user navigates to a new page
                    div(classes = "left-side-entries") {
                        // Loritta Logo/Home Page
                        /* aBodySwap(classes = "entry home-page loritta-navbar-logo", href = "$base/") {
                            style = "font-family: 'Pacifico', cursive; text-transform: none;"

                            +"Loritta"
                        } */
                        a(classes = "entry home-page loritta-navbar-logo", href = "$base/") {
                            attributes["bliss-get"] = "[href]"
                            attributes["bliss-push-url"] = "[href]"
                            attributes["bliss-swaps"] = "body -> body"
                            attributes["bliss-after"] = "scroll:window:top"
                            attributes["bliss-preload"] = "true"

                            style = "font-family: 'Pacifico', cursive; text-transform: none;"

                            +"Loritta"
                        }

                        // Support
                        a(classes = "entry support", href = "$base/support") {
                            attributes["bliss-get"] = "[href]"
                            attributes["bliss-push-url"] = "[href]"
                            attributes["bliss-swaps"] = "body -> body"
                            attributes["bliss-after"] = "scroll:window:top"
                            attributes["bliss-preload"] = "true"

                            iconManager.discord.apply(this)

                            +" ${locale["website.navbar.support"]}"
                        }

                        // Commands
                        a(classes = "entry commands", href = "$base/commands/slash") {
                            attributes["bliss-get"] = "[href]"
                            attributes["bliss-push-url"] = "[href]"
                            attributes["bliss-swaps"] = "body -> body"
                            attributes["bliss-after"] = "scroll:window:top"
                            attributes["bliss-preload"] = "true"

                            iconManager.terminal.apply(this)

                            +" ${locale["modules.sectionNames.commands"]}"
                        }

                        // Daily
                        a(classes = "entry daily", href = "$base/daily") {
                            iconManager.gift.apply(this)

                            +" Daily"
                        }

                        // Premium
                        a(classes = "entry donate", href = "$base/donate") {
                            // attributes["data-preload-link"] = "true"

                            iconManager.sparkles.apply(this)

                            +" Premium"
                        }

                        // Wiki
                        a(classes = "entry extras", href = "$base/extras") {
                            attributes["bliss-get"] = "[href]"
                            attributes["bliss-push-url"] = "[href]"
                            attributes["bliss-swaps"] = "body -> body"
                            attributes["bliss-after"] = "scroll:window:top"
                            attributes["bliss-preload"] = "true"

                            iconManager.book.apply(this)

                            +" Wiki"
                        }

                        // Merch
                        /* a(classes = "entry merch", href = "https://perfectdreams.store/") {
                            iconManager.shirt.apply(this)

                            +" Merch"
                        } */

                        // Equipe
                        a(classes = "entry", href = "$base/staff") {
                            attributes["bliss-get"] = "[href]"
                            attributes["bliss-push-url"] = "[href]"
                            attributes["bliss-swaps"] = "body -> body"
                            attributes["bliss-after"] = "scroll:window:top"
                            attributes["bliss-preload"] = "true"

                            iconManager.rocket.apply(this)

                            +" Equipe"
                        }

                        // Fan Arts
                        a(classes = "entry fan-arts", href = "https://fanarts.perfectdreams.net/") {
                            iconManager.paintBrush.apply(this)

                            +" Fan Arts"
                        }

                        a(classes = "entry sponsors", href = "$base/sponsors") {
                            iconManager.heart.apply(this)

                            +" ${locale["website.navbar.sponsors"]}"
                        }

                        a(classes = "entry sonhos-shop", href = "https://dash.loritta.website/sonhos-shop") {
                            iconManager.cartShopping.apply(this)

                            +" Lojinha de Sonhos"
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

                        // ===[ THEME CHANGER BUTTON ]===
                        div(classes = "entry theme-changer") {
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
                        // TODO: Fix
                        a(classes = "entry", href = "https://dash.loritta.website/") {
                            id = "login-button"
                            iconManager.signIn.apply(this)

                            +" Login"
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
                            a(href = "https://twitter.com/LorittaBot") {
                                iconManager.twitter.apply(this)
                            }

                            a(href = "https://instagram.com/lorittabot") {
                                iconManager.instagram.apply(this)
                            }

                            a(href = "https://youtube.com/c/Loritta") {
                                iconManager.youtube.apply(this)
                            }

                            a(href = "https://www.tiktok.com/@lorittamorenittabot") {
                                iconManager.tiktok.apply(this)
                            }

                            a(href = "https://github.com/LorittaBot") {
                                iconManager.github.apply(this)
                            }
                        }

                        nav(classes = "navigation-footer") {
                            div(classes = "section-entry") {
                                h3 {
                                    +"Loritta Bot"
                                }

                                a(href = "$base/") {
                                    attributes["bliss-get"] = "[href]"
                                    attributes["bliss-push-url"] = "[href]"
                                    attributes["bliss-swaps"] = "body -> body"
                                    attributes["bliss-after"] = "scroll:window:top"
                                    attributes["bliss-preload"] = "true"

                                    +locale["website.navbar.home"]
                                }
                                a(href = "$base/discord-bot-brasileiro") {
                                    +"Loritta: Apenas um simples bot brasileiro para o Discord"
                                }
                                a(href = "https://dash.loritta.website/") {
                                    +locale["website.navbar.dashboard"]
                                }
                                a(href = "$base/support") {
                                    attributes["bliss-get"] = "[href]"
                                    attributes["bliss-push-url"] = "[href]"
                                    attributes["bliss-swaps"] = "body -> body"
                                    attributes["bliss-after"] = "scroll:window:top"
                                    attributes["bliss-preload"] = "true"

                                    +locale["website.navbar.support"]
                                }
                                a(href = "$base/commands/slash") {
                                    attributes["bliss-get"] = "[href]"
                                    attributes["bliss-push-url"] = "[href]"
                                    attributes["bliss-swaps"] = "body -> body"
                                    attributes["bliss-after"] = "scroll:window:top"
                                    attributes["bliss-preload"] = "true"

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
                                a(href = "$base/extras/faq-loritta/about-loritta-bot") {
                                    attributes["bliss-get"] = "[href]"
                                    attributes["bliss-push-url"] = "[href]"
                                    attributes["bliss-swaps"] = "body -> body"
                                    attributes["bliss-after"] = "scroll:window:top"
                                    attributes["bliss-preload"] = "true"

                                    +"Sobre a Loritta (Bot)"
                                }
                                a(href = "$base/extras/stories/how-loritta-bot-was-created") {
                                    attributes["bliss-get"] = "[href]"
                                    attributes["bliss-push-url"] = "[href]"
                                    attributes["bliss-swaps"] = "body -> body"
                                    attributes["bliss-after"] = "scroll:window:top"
                                    attributes["bliss-preload"] = "true"

                                    +"História da Criação da Loritta (Bot)"
                                }
                            }

                            div(classes = "section-entry") {
                                h3 {
                                    +"Loritta Morenitta"
                                }

                                a(href = "https://fanarts.perfectdreams.net/") {
                                    +"Fan Arts"
                                }
                                a(href = "$base/blog") {
                                    +"Blog"
                                }
                                a(href = "$base/contact") {
                                    +i18nContext.get(I18nKeysData.Website.Contact.Title)
                                }
                                a(href = "$base/staff") {
                                    +i18nContext.get(I18nKeysData.Website.Staff.Title)
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
                                a(classes = "discord-button pink", href = LorittaWebsiteBackend.addBotUrl.toString()) {
                                    style = "font-size: 1.5em;"

                                    LorittaWebsiteBackend.svgIconManager.plus.apply(this)

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