package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale

abstract class ProfileDashboardView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    private val legacyBaseLocale: LegacyBaseLocale,
    private val selectedType: String,
) : NavbarView(
    loritta,
    i18nContext,
    locale,
    path
) {
    override val hasFooter = false
    override val useOldStyleCss = true

    override fun DIV.generateContent() {
        div(classes = "totallyHidden") {
            id = "locale-json"
            + LorittaBot.GSON.toJson(legacyBaseLocale.strings)
        }

        div {
            id = "server-configuration"

            div {
                id = "left-sidebar"

                div(classes = "discord-scroller") {
                    id = "left-sidebar-contents"

                    fun appendEntry(url: String, enableLinkPreload: Boolean, name: String, icon: String, type: String) {
                        a(href = "/${locale.path}$url") {
                            if (enableLinkPreload)
                                attributes["data-enable-link-preload"] = "true"

                            div(classes = "item") {
                                if (selectedType == type)
                                    classes = classes + "selected-item"

                                i(classes = icon) {
                                    attributes["aria-hidden"] = "true"
                                }

                                + " "
                                + name
                            }
                        }
                    }

                    div(classes = "server-name") {
                        + "Configurações do Usuário"
                    }

                    appendEntry("/dashboard", true, locale["website.dashboard.profile.sectionNames.yourServers"], "fa fa-cogs", "main")
                    appendEntry("/user/@me/dashboard/profiles", true, locale["website.dashboard.profile.sectionNames.profileLayout"], "far fa-id-card", "profile_list")
                    appendEntry("/user/@me/dashboard/backgrounds", true, "Backgrounds", "far fa-images", "background_list")
                    appendEntry("/user/@me/dashboard/ship-effects", true, locale["website.dashboard.profile.sectionNames.shipEffects"], "fas fa-heart", "ship_effects")

                    div(classes = "server-name") {
                        + "Miscelânea"
                    }

                    appendEntry("/daily", false, "Daily", "fas fa-money-bill-wave", "daily")
                    appendEntry("/user/@me/dashboard/daily-shop", true, locale["website.dailyShop.title"], "fas fa-store", "daily_shop")
                    appendEntry("/user/@me/dashboard/bundles", false, locale["website.dashboard.profile.sectionNames.sonhosShop"], "fas fa-shopping-cart", "bundles")
                    appendEntry("/guidelines", false, locale["website.guidelines.communityGuidelines"], "fas fa-asterisk", "guidelines")

                    hr {}

                    a {
                        id = "logout-button"

                        div(classes = "item") {
                            style = "color: rgb(240, 71, 71);"

                            i(classes = "fas fa-sign-out-alt") {
                                attributes["aria-hidden"] = "true"
                            }

                            + " "
                            + locale["website.dashboard.profile.sectionNames.logout"]
                        }
                    }
                }
            }

            div {
                id = "right-sidebar"

                div {
                    id = "right-sidebar-contents"

                    generateRightSidebarContents()

                    div {
                        id = "sidebar-ad"

                        div {
                            style = "position: relative; width: 100%; max-width: 100%;"

                            ins(classes = "adsbygoogle") {
                                style = "display:block; position: absolute; width: inherit; max-width: 100%;"
                                attributes["data-ad-client"] = "ca-pub-9989170954243288"
                                attributes["data-ad-slot"] = "3177212938"
                                attributes["data-ad-format"] = "auto"
                                attributes["data-full-width-responsive"] = "true"
                            }
                        }
                    }
                }

                hr {}
            }
        }
    }

    abstract fun DIV.generateRightSidebarContents()
}