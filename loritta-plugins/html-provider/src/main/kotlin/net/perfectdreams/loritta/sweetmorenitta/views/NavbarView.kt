package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
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

abstract class NavbarView(
    locale: BaseLocale,
    path: String
) : BaseView(
        locale,
        path
) {
    var hasNavbar: Boolean = true
    var hasFooter: Boolean = true

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

                        div(classes = "entry") {
                            a(classes = "fan-arts", href = "$base/fanarts") {
                                attributes["data-enable-link-preload"] = "true"

                                i(classes = "fas fa-paint-brush") {}

                                +" Fan Arts"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "donate", href = "$base/donate") {
                                i(classes = "fas fa-gift") {}

                                // attributes["data-enable-link-preload"] = "true"

                                +" Premium"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "extras", href = "$base/extras") {
                                i(classes = "fas fa-star") {}

                                if (false)
                                    attributes["data-enable-link-preload"] = "true"

                                +" Extras"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "blog", href = "$base/blog") {
                                attributes["data-enable-link-preload"] = "true"

                                i(classes = "fas fa-newspaper") {}

                                +" Blog"
                            }
                        }

                        div(classes = "entry") {
                            a(classes = "sponsors", href = "$base/sponsors") {
                                attributes["data-enable-link-preload"] = "true"

                                i(classes = "far fa-kiss-wink-heart") {}

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

                            a(href = "https://twitter.com/LorittaBot") {
                                i(classes = "fab fa-twitter") {}
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
                                a(href = "$base/dashboard") {
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
                                a(href = "$base/fanarts") {
                                    attributes["data-enable-link-preload"] = "true"
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
                                a(classes = "add-me button pink shadow big", href = loritta.discordInstanceConfig.discord.addBotUrl) {
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