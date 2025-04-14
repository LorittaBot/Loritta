package net.perfectdreams.loritta.morenitta.website.views.httpapidocs

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.website.Ads
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents.fillContentLoadingSection
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.closeModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.tsukiScript
import net.perfectdreams.loritta.morenitta.website.views.BaseView
import net.perfectdreams.loritta.morenitta.website.views.dashboard.DashboardView
import net.perfectdreams.loritta.morenitta.website.views.httpapidocs.LoriDevelopersDocsView.SidebarCategory
import net.perfectdreams.loritta.morenitta.website.views.httpapidocs.LoriDevelopersDocsView.SidebarEntry
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoint
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

abstract class LoriDevelopersDocsDashboardView(
    internal val lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    internal val legacyBaseLocale: LegacyBaseLocale,
    internal val userIdentification: LorittaJsonWebSession.UserIdentification?,
    internal val userPremiumPlan: UserPremiumPlans,
    internal val colorTheme: ColorTheme,
    private val sidebarCategories: List<SidebarCategory>,
) : BaseView(
    i18nContext,
    locale,
    path
) {
    override val useDashboardStyleCss = true
    override val useOldStyleCss = false

    override fun HTML.generateBody() {
        body {
            // TODO - htmx-adventures: Fix this! (this may be removed later, but there are still things in SpicyMorenitta that requires this)

            div(classes = "totallyHidden") {
                style = "display:none;"
                id = "locale-json"
                // The full legacy base locale string is FAT and uses A LOT of precious kbs
                // (daily shop)
                // with super legacy locales: 43,03kb / 184,89kb
                // with no locales: 8,34kb / 66,76kb
                // So we will filter to only get the keys that the old frontend uses right now
                + LorittaBot.GSON.toJson(
                    legacyBaseLocale.strings
                        .filterKeys { it in legacyBaseLocaleKeysUsedInTheFrontend }
                )
            }

            // TODO - htmx-adventures: Remove this! This is used by the old SpicyMorenitta coded for loading screens
            div {
                id = "loading-screen"
                div(classes = "loading-text") {
                    style = "display: none;"
                }
            }

            div {
                attributes["hx-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/pocket-loritta"
                attributes["hx-trigger"] = "load"
            }

            div {
                id = "root"

                div(classes = colorTheme.className) {
                    id = "app-wrapper"

                    div {
                        // TODO - htmx-adventures: Is this ID even used?
                        id = "server-configuration"

                        div {
                            id = "outer-wrapper"

                            // List of modals
                            div {
                                id = "modal-list"
                            }

                            // List of toasts
                            div {
                                id = "toast-list"
                            }

                            div {
                                id = "wrapper"

                                nav(classes = "is-closed") {
                                    id = "left-sidebar"

                                    generateLeftSidebarContents()

                                    div(classes = "user-info-wrapper") {
                                        if (userIdentification != null) {
                                            div(classes = "user-info") {
                                                // TODO - htmx-adventures: Move this somewhere else
                                                val userAvatarId = userIdentification.avatar
                                                val avatarUrl = if (userAvatarId != null) {
                                                    val extension =
                                                        if (userAvatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                                                            "gif"
                                                        } else {
                                                            "png"
                                                        }

                                                    "https://cdn.discordapp.com/avatars/${userIdentification.id}/${userAvatarId}.${extension}?size=64"
                                                } else {
                                                    val avatarId = (userIdentification.id.toLong() shr 22) % 6

                                                    "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
                                                }

                                                img(src = avatarUrl) {
                                                    width = "24"
                                                    height = "24"
                                                }

                                                div(classes = "user-tag") {
                                                    div(classes = "name") {
                                                        text(
                                                            userIdentification.globalName ?: userIdentification.username
                                                        )
                                                    }

                                                    div(classes = "discriminator") {
                                                        text("@${userIdentification.username}")
                                                    }
                                                }

                                                div(classes = "discord-button no-background-theme-dependent-dark-text") {
                                                    openEmbeddedModalOnClick(
                                                        "Escolha um Tema",
                                                        true,
                                                        {
                                                            div(classes = "theme-selector") {
                                                                div(classes = "theme-selector-lori") {
                                                                    div(classes = "theme-selector-lori-inner") {
                                                                        etherealGambiImg(
                                                                            "https://stuff.loritta.website/loritta-matrix-choice-cookiluck.png",
                                                                            sizes = "500px"
                                                                        ) {}

                                                                        div(classes = "theme-option light") {
                                                                            text("Tema Claro")
                                                                        }

                                                                        div(classes = "theme-option dark") {
                                                                            text("Tema Escuro")
                                                                        }
                                                                    }
                                                                }

                                                                div(classes = "theme-selector-buttons") {
                                                                    button(classes = "discord-button primary") {
                                                                        attributes["hx-post"] =
                                                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/theme"
                                                                        attributes["hx-swap"] = "none"
                                                                        attributes["hx-indicator"] =
                                                                            "find .htmx-discord-like-loading-button"
                                                                        attributes["hx-disabled-elt"] = "this"
                                                                        attributes["hx-vals"] = buildJsonObject {
                                                                            put("theme", ColorTheme.LIGHT.name)
                                                                        }.toString()

                                                                        div(classes = "htmx-discord-like-loading-button") {
                                                                            div {
                                                                                text("Tema Claro")
                                                                            }

                                                                            div(classes = "loading-text-wrapper") {
                                                                                img(src = LoadingSectionComponents.list.random())

                                                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                                                                            }
                                                                        }
                                                                    }

                                                                    button(classes = "discord-button primary") {
                                                                        attributes["hx-post"] =
                                                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/theme"
                                                                        attributes["hx-swap"] = "none"
                                                                        attributes["hx-indicator"] =
                                                                            "find .htmx-discord-like-loading-button"
                                                                        attributes["hx-disabled-elt"] = "this"
                                                                        attributes["hx-vals"] = buildJsonObject {
                                                                            put("theme", ColorTheme.DARK.name)
                                                                        }.toString()

                                                                        div(classes = "htmx-discord-like-loading-button") {
                                                                            div {
                                                                                text("Tema Escuro")
                                                                            }

                                                                            div(classes = "loading-text-wrapper") {
                                                                                img(src = LoadingSectionComponents.list.random())

                                                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        listOf()
                                                    )
                                                    text("Tema")
                                                }
                                            }
                                        }
                                    }
                                }

                                nav {
                                    id = "mobile-left-sidebar"

                                    button(classes = "hamburger-button") {
                                        type = ButtonType.button
                                        i(classes = "fa-solid fa-bars") {}
                                        // language=JavaScript
                                        tsukiScript(
                                            code = """
                                            self.on("click", (e) => {
                                                const leftSidebar = selectFirst("#left-sidebar");
                                                leftSidebar.toggleClass("is-open")
                                                leftSidebar.toggleClass("is-closed")
                                            })
                                        """.trimIndent()
                                        )
                                    }
                                }

                                section {
                                    id = "right-sidebar"

                                    div(classes = "htmx-fill-content-loading-section") {
                                        id = "right-sidebar-wrapper"

                                        div {
                                            article(classes = "content") {
                                                // This ID is used for content switch
                                                id = "right-sidebar-contents"

                                                div {
                                                    generateRightSidebarContents()
                                                }
                                            }
                                        }

                                        fillContentLoadingSection(i18nContext)
                                    }

                                    aside {
                                        id = "that-wasnt-very-cash-money-of-you"

                                        if (userPremiumPlan.displayAds) {
                                            val adType = Ads.RIGHT_SIDEBAR_AD
                                            ins(classes = "adsbygoogle") {
                                                classes += "adsbygoogle"
                                                style =
                                                    "display: inline-block; width: ${adType.size.width}px; height: ${adType.size.height}px;"
                                                attributes["data-ad-client"] = "ca-pub-9989170954243288"
                                                attributes["data-ad-slot"] = adType.googleAdSenseId
                                            }
                                            script {
                                                unsafe {
                                                    raw("(adsbygoogle = window.adsbygoogle || []).push({});")
                                                }
                                            }
                                        } else {
                                            aside {
                                                id = "loritta-snug"

                                                // Always 160px because we use the --sidebar-ad-width CSS variable for its size, so if that variable changes, we need to change it here too!
                                                etherealGambiImg(
                                                    src = "https://stuff.loritta.website/loritta-snuggle.png",
                                                    sizes = "160px"
                                                ) {
                                                    openEmbeddedModalOnClick(
                                                        i18nContext.get(I18nKeysData.Website.Dashboard.ThankYouMoneyModal.Title),
                                                        true,
                                                        {
                                                            div {
                                                                style = "text-align: center;"

                                                                img(src = "https://stuff.loritta.website/emotes/lori-kiss.png") {
                                                                    height = "200"
                                                                }

                                                                for (text in i18nContext.get(I18nKeysData.Website.Dashboard.ThankYouMoneyModal.Description)) {
                                                                    p {
                                                                        text(text)
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        listOf {
                                                            classes += "no-background-theme-dependent-dark-text"

                                                            closeModalOnClick()
                                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    aside {
                                        id = "that-wasnt-very-cash-money-of-you-reserved-space"
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /* aside {
                id = "that-wasnt-very-cash-money-of-you"

                ins(classes = "adsbygoogle") {
                    style = "display:block; position: absolute; width: inherit; max-width: 100%;"
                    attributes["data-ad-client"] = "ca-pub-9989170954243288"
                    attributes["data-ad-slot"] = "3177212938"
                    attributes["data-ad-format"] = "auto"
                    attributes["data-full-width-responsive"] = "true"
                }
            } */
        }
    }

    fun DIV.appendEntry(url: String, enableHtmxSwitch: Boolean, name: String, endpoint: LoriPublicHttpApiEndpoint) {
        // If we are appending a non-relative URL, then use it instead!
        val href = if (url.startsWith("https://") || url.startsWith("http://")) {
            url
        } else {
            "/${locale.path}$url"
        }
        a(href = href, classes = "entry") {
            // TODO - htmx-adventures: This actually works, but pages that rely on SpicyMorenitta's routing borks when we redirect in this way
            if (enableHtmxSwitch) {
                attributes["hx-select"] = "#right-sidebar-contents"
                attributes["hx-target"] = "#right-sidebar-contents"
                attributes["hx-get"] = "/${locale.path}$url"
                attributes["hx-indicator"] = "#right-sidebar-wrapper"
                attributes["hx-push-url"] = "true"
                // Fix bug when a user is rapidly clicking on multiple entries while they are loading, causing a htmx:swapError
                // Example: Click on entry1, then before it finishes loading, click on entry2, htmx will crash!
                // We use "replace" because we always want to honor the LAST click made by the user
                attributes["hx-sync"] = "#left-sidebar:replace"
                // show:top - Scroll to the top
                // settle:0ms - We don't want the settle animation beccause it is a full page swap
                // swap:0ms - We don't want the swap animation because it is a full page swap
                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                tsukiScript(code = DashboardView.JAVASCRIPT_CLOSE_LEFT_SIDEBAR_ON_CLICK)
            }

            // TODO - htmx-adventures: Is this useful?
            // if (selectedType == type)
            //     classes = classes + "selected-entry"

            span(classes = "http-method-sidebar") {
                span(classes = "http-method-circle http-method-${endpoint.method.value.lowercase()}") {}

                text(endpoint.method.value)
            }
            text(" ")
            text(name)
        }
    }

    fun NAV.generateLeftSidebarContents() {
        div(classes = "entries") {
            a(classes = "entry loritta-logo") {
                text("Loritta")

                div {
                    style = "font-family: monospace; font-size: 0.75em;"
                    text("for Developers")
                }
            }

            for (sidebarCategory in sidebarCategories) {
                hr(classes = "divider") {}
                if (sidebarCategory.name != null) {
                    div(classes = "category") {
                        text(sidebarCategory.name)
                    }
                }

                for (page in sidebarCategory.entries) {
                    when (page) {
                        is SidebarEntry.SidebarEndpointEntry -> {
                            appendEntry("/developers/docs/${page.path}", true, page.name, page.endpoint)
                        }
                        is SidebarEntry.SidebarPageEntry -> {
                            var baseUrl = "/developers/docs"
                            if (page.path != "index")
                                baseUrl += "/${page.path}"
                            appendEntry(baseUrl, true, page.name, page.icon, "main")
                        }
                    }
                }
            }
        }
    }

    fun DIV.appendEntry(url: String, enableHtmxSwitch: Boolean, name: String, icon: String, type: String) {
        // If we are appending a non-relative URL, then use it instead!
        val href = if (url.startsWith("https://") || url.startsWith("http://")) {
            url
        } else {
            "/${locale.path}$url"
        }
        a(href = href, classes = "entry") {
            // TODO - htmx-adventures: This actually works, but pages that rely on SpicyMorenitta's routing borks when we redirect in this way
            if (enableHtmxSwitch) {
                attributes["hx-select"] = "#right-sidebar-contents"
                attributes["hx-target"] = "#right-sidebar-contents"
                attributes["hx-get"] = "/${locale.path}$url"
                attributes["hx-indicator"] = "#right-sidebar-wrapper"
                attributes["hx-push-url"] = "true"
                // Fix bug when a user is rapidly clicking on multiple entries while they are loading, causing a htmx:swapError
                // Example: Click on entry1, then before it finishes loading, click on entry2, htmx will crash!
                // We use "replace" because we always want to honor the LAST click made by the user
                attributes["hx-sync"] = "#left-sidebar:replace"
                // show:top - Scroll to the top
                // settle:0ms - We don't want the settle animation beccause it is a full page swap
                // swap:0ms - We don't want the swap animation because it is a full page swap
                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                tsukiScript(code = DashboardView.JAVASCRIPT_CLOSE_LEFT_SIDEBAR_ON_CLICK)
            }

            // TODO - htmx-adventures: Is this useful?
            // if (selectedType == type)
            //     classes = classes + "selected-entry"

            i(classes = icon) {
                attributes["aria-hidden"] = "true"
            }

            text(" ")
            text(name)
        }
    }

    abstract fun DIV.generateRightSidebarContents()
}