package net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EmptySection.emptySection
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.ProfileDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import org.jetbrains.exposed.sql.ResultRow

class ProfilePresetsListView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    val profilePresets: List<ResultRow>
) : ProfileDashboardView(
    lorittaWebsite,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    "profile-presets"
) {
    companion object {
        const val MAX_PROFILE_PRESETS = 100
    }

    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.Title)

    override fun DIV.generateRightSidebarContents() {
        div {
            div(classes = "hero-wrapper") {
                // etherealGambiImg("https://stuff.loritta.website/loritta-daily-shop-allouette.png", classes = "hero-image", sizes = "(max-width: 900px) 100vw, 360px") {}

                div(classes = "hero-text") {
                    h1 {
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.Title))
                    }

                    for (line in i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.Description)) {
                        p {
                            text(line)
                        }
                    }
                }
            }

            hr {}

            div(classes = "cards-with-header") {
                div(classes = "card-header") {
                    div(classes = "card-header-info") {
                        div(classes = "card-header-title") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.YourProfilePresets))
                        }

                        div(classes = "card-header-description") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.Accounts(profilePresets.size)))
                        }
                    }

                    button(classes = "discord-button primary") {
                        if (MAX_PROFILE_PRESETS > profilePresets.size) {
                            attributes["hx-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/profile-presets/create"
                            attributes["hx-push-url"] = "true"
                            // show:top - Scroll to the top
                            // settle:0ms - We don't want the settle animation beccause it is a full page swap
                            // swap:0ms - We don't want the swap animation because it is a full page swap
                            attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                            attributes["hx-select"] = "#right-sidebar-contents"
                            attributes["hx-target"] = "#right-sidebar-contents"
                        } else {
                            disabled = true
                        }

                        type = ButtonType.button

                        htmxDiscordLikeLoadingButtonSetup(
                            i18nContext
                        ) {
                            this.text(i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.CreatePreset))
                        }
                    }
                }

                if (profilePresets.isNotEmpty()) {
                    div(classes = "loritta-items-wrapper") {
                        for (preset in profilePresets.sortedBy { it[UserCreatedProfilePresets.name] }) {
                            div(classes = "shop-item-entry rarity-rare") {
                                val buttons = mutableListOf<BUTTON.() -> (Unit)>(
                                    {
                                        this.defaultModalCloseButton(i18nContext)
                                    },
                                    {
                                        classes += "discord-button danger"

                                        type = ButtonType.button

                                        openEmbeddedModalOnClick(
                                            "Você tem certeza?",
                                            true,
                                            {
                                                div {
                                                    text("Você quer deletar meeeesmo?")
                                                }
                                            },
                                            listOf(
                                                {
                                                    this.defaultModalCloseButton(i18nContext)
                                                },
                                                {
                                                    attributes["hx-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/profile-presets/${preset[UserCreatedProfilePresets.id].value}"
                                                    attributes["hx-disabled-elt"] = "this"
                                                    attributes["hx-include"] = "[name='handle']"
                                                    // show:top - Scroll to the top
                                                    // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                                    // swap:0ms - We don't want the swap animation because it is a full page swap
                                                    attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                                                    attributes["hx-select"] = "#right-sidebar-contents"
                                                    attributes["hx-target"] = "#right-sidebar-contents"
                                                    attributes["hx-indicator"] =
                                                        "find .htmx-discord-like-loading-button"

                                                    this.classes += "danger"

                                                    htmxDiscordLikeLoadingButtonSetup(
                                                        i18nContext
                                                    ) {
                                                        this.text("Excluir")
                                                    }
                                                }
                                            )
                                        )

                                        text("Excluir")
                                    },
                                    {
                                        classes += "discord-button primary"
                                        attributes["hx-post"] =
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/profile-presets/${preset[UserCreatedProfilePresets.id].value}/apply"
                                        attributes["hx-swap"] = "none"
                                        attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                                        attributes["hx-disabled-elt"] = "this"

                                        type = ButtonType.button

                                        div(classes = "htmx-discord-like-loading-button") {
                                            text("Aplicar")
                                        }
                                    }
                                )

                                openEmbeddedModalOnClick(
                                    preset[UserCreatedProfilePresets.name],
                                    true,
                                    {
                                        div(classes = "loritta-item-preview-wrapper") {
                                            div(classes = "canvas-preview-wrapper-wrapper") {
                                                div(classes = "canvas-preview-wrapper") {
                                                    img(
                                                        classes = "canvas-preview-profile-design",
                                                        src = "/api/v1/users/@me/profile?type=${preset[UserCreatedProfilePresets.profileDesign]}&background=${preset[UserCreatedProfilePresets.background]}"
                                                    ) {
                                                        style = "width: 400px; aspect-ratio: 4/3;"
                                                    }
                                                }
                                            }

                                            div {
                                                style = "flex-grow: 1;"
                                                // TODO: Maybe add some stats here, like when the user last used the preset?
                                            }
                                        }
                                    },
                                    buttons
                                )

                                div {
                                    style = "position: relative;"

                                    div {
                                        style = "overflow: hidden; line-height: 0;"

                                        img {
                                            src =
                                                "/api/v1/users/@me/profile?type=${preset[UserCreatedProfilePresets.profileDesign]}&background=${preset[UserCreatedProfilePresets.background]}"

                                            // The aspect ratio makes the design not be wonky when the image is not loaded
                                            style = "width: 100%; height: auto; aspect-ratio: 4/3;"
                                        }
                                    }

                                    div(classes = "item-entry-information rarity-rare") {
                                        div(classes = "item-entry-title rarity-rare") {
                                            text(preset[UserCreatedProfilePresets.name])
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    div {
                        emptySection(i18nContext)
                    }
                }
            }
        }
    }
}