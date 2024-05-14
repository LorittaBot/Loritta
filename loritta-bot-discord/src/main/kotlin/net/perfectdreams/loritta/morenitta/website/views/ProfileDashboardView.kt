package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.components.Ads
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents.fillContentLoadingSection
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.closeModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.NitroPayAdSize
import net.perfectdreams.loritta.morenitta.website.utils.generateNitroPayAd
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import kotlin.random.Random

abstract class ProfileDashboardView(
    private val loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    private val legacyBaseLocale: LegacyBaseLocale,
    val userIdentification: LorittaJsonWebSession.UserIdentification,
    private val userPremiumPlan: UserPremiumPlans,
    val colorTheme: ColorTheme,
    private val selectedType: String,
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
                            id = "wrapper"

                            // List of modals
                            div {
                                id = "modal-list"
                            }

                            // List of toasts
                            div {
                                id = "toast-list"
                            }

                            nav(classes = "is-closed") {
                                id = "left-sidebar"

                                div(classes = "entries") {
                                    fun appendEntry(url: String, enableHtmxSwitch: Boolean, name: String, icon: String, type: String) {
                                        a(href = "/${locale.path}$url", classes = "entry") {
                                            // TODO - htmx-adventures: This actually works, but pages that rely on SpicyMorenitta's routing borks when we redirect in this way
                                            if (enableHtmxSwitch) {
                                                attributes["hx-select"] = "#right-sidebar-contents"
                                                attributes["hx-target"] = "#right-sidebar-contents"
                                                attributes["hx-get"] = "/${locale.path}$url"
                                                attributes["hx-indicator"] = "#right-sidebar-wrapper"
                                                attributes["hx-push-url"] = "true"
                                                // show:top - Scroll to the top
                                                // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                                // swap:0ms - We don't want the swap animation because it is a full page swap
                                                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                                                attributes["_"] = """
                                                            on click
                                                                remove .is-open from #left-sidebar
                                                                add .is-closed to #left-sidebar
                                                            end
                                                """.trimIndent()
                                            }

                                            if (selectedType == type)
                                                classes = classes + "selected-entry"

                                            i(classes = icon) {
                                                attributes["aria-hidden"] = "true"
                                            }

                                            text(" ")
                                            text(name)
                                        }
                                    }

                                    a(classes = "entry loritta-logo") {
                                        text("Loritta")
                                    }

                                    hr(classes = "divider") {}

                                    appendEntry("/dashboard", true, locale["website.dashboard.profile.sectionNames.yourServers"], "fa fa-cogs", "main")

                                    hr(classes = "divider") {}
                                    div(classes = "category") {
                                        + "Configurações do Usuário"
                                    }

                                    appendEntry("/dashboard/profiles", false, locale["website.dashboard.profile.sectionNames.profileLayout"], "far fa-id-card", "profile_list")
                                    appendEntry("/dashboard/backgrounds", false, "Backgrounds", "far fa-images", "background_list")
                                    appendEntry("/dashboard/ship-effects", true, locale["website.dashboard.profile.sectionNames.shipEffects"], "fas fa-heart", "ship_effects")

                                    hr(classes = "divider") {}
                                    div(classes = "category") {
                                        + "Miscelânea"
                                    }

                                    appendEntry("/daily", false, "Daily", "fas fa-money-bill-wave", "daily")
                                    appendEntry("/dashboard/daily-shop", true, locale["website.dailyShop.title"], "fas fa-store", "daily_shop")
                                    appendEntry("/dashboard/sonhos-shop", true, locale["website.dashboard.profile.sectionNames.sonhosShop"], "fas fa-shopping-cart", "bundles")
                                    appendEntry("/guidelines", false, locale["website.guidelines.communityGuidelines"], "fas fa-asterisk", "guidelines")
                                    a(classes = "entry") {
                                        openEmbeddedModalOnClick(
                                            i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.PocketLoritta),
                                            canBeClosedByClickingOutsideTheWindow = false,
                                            {
                                                div {
                                                    div {
                                                        style = "text-align: center;"

                                                        for (text in i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.DoYouWantSomeCompany)) {
                                                            p {
                                                                text(text)
                                                            }
                                                        }
                                                    }

                                                    div(classes = "loritta-spawner-wrapper") {
                                                        div(classes = "loritta-spawners") {
                                                            fun FlowContent.creatureSpawner(
                                                                fancyName: String,
                                                                internalName: String,
                                                                spritesFolder: String,
                                                                loadingButtonGif: String
                                                            ) {
                                                                div(classes = "loritta-spawner") {
                                                                    img(src = "https://stuff.loritta.website/pocket-loritta/$spritesFolder/repouso.png") {
                                                                        width = "128"
                                                                    }
                                                                    button(classes = "discord-button success") {
                                                                        attributes["hx-post"] =
                                                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/pocket-loritta/spawn"
                                                                        attributes["hx-vals"] = buildJsonObject {
                                                                            put("type", internalName)
                                                                        }.toString()
                                                                        attributes["hx-indicator"] =
                                                                            "find .htmx-discord-like-loading-button"
                                                                        attributes["hx-disabled-elt"] = "this"
                                                                        // Makes it a feel a bit better with the button not jumping all over the place when the text is replaced
                                                                        attributes["hx-on::before-request"] =
                                                                            "this.style.minWidth = this.getBoundingClientRect().width + \"px\""
                                                                        attributes["hx-on::after-request"] =
                                                                            "this.style.minWidth = null"

                                                                        div(classes = "htmx-discord-like-loading-button") {
                                                                            div {
                                                                                text(
                                                                                    i18nContext.get(
                                                                                        I18nKeysData.Website.Dashboard.LorittaSpawner.SpawnPlayer(
                                                                                            fancyName
                                                                                        )
                                                                                    )
                                                                                )
                                                                            }

                                                                            div(classes = "loading-text-wrapper") {
                                                                                img(src = loadingButtonGif)

                                                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            creatureSpawner("Loritta", "LORITTA", "lori-sprites", LoadingSectionComponents.LORITTA_LOADING_GIF)
                                                            creatureSpawner("Pantufa", "PANTUFA", "pantufa-sprites", LoadingSectionComponents.PANTUFA_LOADING_GIF)
                                                            creatureSpawner("Gabriela", "GABRIELA", "gabriela-sprites", LoadingSectionComponents.GABRIELA_LOADING_GIF)
                                                        }

                                                        button(classes = "discord-button danger") {
                                                            attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/pocket-loritta/clear"
                                                            attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                                                            attributes["hx-disabled-elt"] = "this"
                                                            // Makes it a feel a bit better with the button not jumping all over the place when the text is replaced
                                                            attributes["hx-on::before-request"] = "this.style.minWidth = this.getBoundingClientRect().width + \"px\""
                                                            attributes["hx-on::after-request"] = "this.style.minWidth = null"

                                                            div(classes = "htmx-discord-like-loading-button") {
                                                                div {
                                                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.CleanUp))
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
                                            listOf {
                                                classes += "no-background-theme-dependent-dark-text"

                                                closeModalOnClick()
                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
                                            }
                                        )
                                        i(classes = "fas fa-star") {
                                            attributes["aria-hidden"] = "true"
                                        }

                                        text(" ")
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.LorittaSpawner.PocketLoritta))
                                    }

                                    hr(classes = "divider") {}

                                    a {
                                        id = "logout-button"

                                        div(classes = "entry") {
                                            i(classes = "fas fa-sign-out-alt") {
                                                attributes["aria-hidden"] = "true"
                                            }

                                            + " "
                                            + locale["website.dashboard.profile.sectionNames.logout"]
                                        }
                                    }
                                }

                                div(classes = "user-info-wrapper") {
                                    div(classes = "user-info") {
                                        // TODO - htmx-adventures: Move this somewhere else
                                        val userAvatarId = userIdentification.avatar
                                        val avatarUrl = if (userAvatarId != null) {
                                            val extension = if (userAvatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                                                "gif"
                                            } else { "png" }

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
                                                text(userIdentification.globalName ?: userIdentification.username)
                                            }

                                            div(classes = "discriminator") {
                                                text("@${userIdentification.username}")
                                            }
                                        }

                                        div(classes = "discord-button no-background-theme-dependent-dark-text") {
                                            openEmbeddedModalOnClick(
                                                "Antes de começar...",
                                                true,
                                                {
                                                    div(classes = "theme-selector") {
                                                        div(classes = "theme-selector-lori") {
                                                            div(classes = "theme-selector-lori-inner") {
                                                                etherealGambiImg("https://stuff.loritta.website/loritta-matrix-choice-cookiluck.png", sizes = "500px") {}

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
                                                                attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/theme"
                                                                attributes["hx-swap"] = "none"
                                                                attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
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
                                                                attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/theme"
                                                                attributes["hx-swap"] = "none"
                                                                attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
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

                            nav {
                                id = "mobile-left-sidebar"

                                button(classes = "hamburger-button") {
                                    attributes["_"] = """
                                        on click
                                            toggle .is-open on #left-sidebar
                                            toggle .is-closed on #left-sidebar
                                        end
                                    """.trimIndent()
                                    type = ButtonType.button
                                    i(classes = "fa-solid fa-bars") {}
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
                                        val abTest = Random.nextBoolean()
                                        if (abTest) {
                                            generateNitroPayAd(
                                                "${adType.nitroPayId}-desktop",
                                                listOf(
                                                    NitroPayAdSize(
                                                        adType.size.width,
                                                        adType.size.height
                                                    )
                                                )
                                            )
                                        } else {
                                            ins(classes = "adsbygoogle") {
                                                classes += "adsbygoogle"
                                                style = "display: inline-block; width: ${adType.size.width}px; height: ${adType.size.height}px;"
                                                attributes["data-ad-client"] = "ca-pub-9989170954243288"
                                                attributes["data-ad-slot"] = adType.googleAdSenseId
                                            }
                                            script {
                                                unsafe {
                                                    raw("(adsbygoogle = window.adsbygoogle || []).push({});")
                                                }
                                            }
                                        }
                                    } else {
                                        aside {
                                            id = "loritta-snug"

                                            // Always 160px because we use the --sidebar-ad-width CSS variable for its size, so if that variable changes, we need to change it here too!
                                            etherealGambiImg(src = "https://stuff.loritta.website/loritta-snuggle.png", sizes = "160px") {
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

    abstract fun DIV.generateRightSidebarContents()
}