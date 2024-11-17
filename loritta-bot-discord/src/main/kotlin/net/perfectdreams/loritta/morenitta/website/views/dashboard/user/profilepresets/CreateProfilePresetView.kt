package net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.profilepresets.PostCreateProfilePresetRoute
import net.perfectdreams.loritta.morenitta.website.utils.tsukiScript
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.ProfileDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class CreateProfilePresetView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    val activeProfileDesignId: String,
    val activeBackgroundId: String
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
    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.Title)

    override fun DIV.generateRightSidebarContents() {
        div {
            a(classes = "discord-button no-background-theme-dependent-dark-text", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/profile-presets") {
                htmxGetAsHref()
                attributes["hx-push-url"] = "true"
                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                attributes["hx-select"] = "#right-sidebar-contents"
                attributes["hx-target"] = "#right-sidebar-contents"

                htmxDiscordLikeLoadingButtonSetup(
                    i18nContext
                ) {
                    this.text(i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.PresetCreation.GoBack))
                }
            }

            hr {}

            div {
                style = "text-align: center;"

                img(
                    classes = "canvas-preview-profile-design",
                    src = "/api/v1/users/@me/profile?type=${activeProfileDesignId}&background=${activeBackgroundId}"
                ) {
                    style = "width: 400px; aspect-ratio: 4/3;"
                }
            }

            form {
                hiddenInput(name = "activeProfileDesignId") {
                    value = activeProfileDesignId
                }
                hiddenInput(name = "activeBackgroundId") {
                    value = activeBackgroundId
                }

                div(classes = "field-wrappers") {
                    div(classes = "field-wrapper") {
                        div(classes = "field-title") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.PresetCreation.ProfilePresetName))
                        }

                        div(classes = "field-description") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.PresetCreation.ProfilePresetDescription))
                        }

                        input(InputType.text) {
                            maxLength = PostCreateProfilePresetRoute.MAX_PRESET_LENGTH.toString()
                            name = "presetName"
                        }
                    }

                    /* text("Opcionais")
                checkBoxInput {
                    name = "aboutMe"
                    value = "test2"
                }
                checkBoxInput {
                    name = "activeBadge"
                    value = "test"
                } */

                    div(classes = "field-wrapper") {
                        button(classes = "discord-button success") {
                            attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/profile-presets/create"
                            attributes["hx-push-url"] = "true"
                            // show:top - Scroll to the top
                            // settle:0ms - We don't want the settle animation beccause it is a full page swap
                            // swap:0ms - We don't want the swap animation because it is a full page swap
                            attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                            attributes["hx-select"] = "#right-sidebar-contents"
                            attributes["hx-target"] = "#right-sidebar-contents"

                            id = "create-preset-button"
                            type = ButtonType.submit
                            disabled = true

                            htmxDiscordLikeLoadingButtonSetup(
                                i18nContext
                            ) {
                                this.text(i18nContext.get(I18nKeysData.Website.Dashboard.ProfilePresets.CreatePreset))
                            }

                            tsukiScript(code = """
                                             var input = selectFirst("[name='presetName']")
                                             var button = self
                                             input.on("input", e => {
                                                 button.disabled = input.value.trim() === '';
                                             })
                                        """.trimIndent())
                        }
                    }
                }
            }
        }
    }
}