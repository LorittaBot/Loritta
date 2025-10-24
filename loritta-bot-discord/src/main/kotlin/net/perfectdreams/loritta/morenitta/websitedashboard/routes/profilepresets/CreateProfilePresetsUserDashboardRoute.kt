package net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets

import io.ktor.server.application.ApplicationCall
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.img
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.textInput
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.characterCounter
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.ColorTheme

class CreateProfilePresetsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profile-presets/create") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val result = website.loritta.transaction {
            val profile = website.loritta.getLorittaProfile(session.userId)
            val activeProfileDesignId = profile?.settings?.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID
            val activeBackgroundId = profile?.settings?.activeBackgroundInternalName?.value ?: Background.DEFAULT_BACKGROUND_ID

            return@transaction Result(activeProfileDesignId, activeBackgroundId)
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.ProfilePresets.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            userDashLeftSidebarEntries(website.loritta, i18nContext, UserDashboardSection.PROFILE_PRESETS)
                        },
                        {
                            goBackToPreviousSectionButton(
                                href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-presets",
                            ) {
                                text(i18nContext.get(DashboardI18nKeysData.ProfilePresets.PresetCreation.GoBack))
                            }

                            hr {}

                            div {
                                style = "text-align: center;"

                                img(classes = "canvas-preview-profile-design", src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-preview?type=${result.activeProfileDesignId}&background=${result.activeBackgroundId}") {
                                    style = "width: 400px; aspect-ratio: 4/3;"
                                }
                            }

                            fieldWrappers {
                                fieldWrapper {
                                    fieldTitle {
                                        text(i18nContext.get(DashboardI18nKeysData.ProfilePresets.PresetCreation.ProfilePresetName))
                                    }

                                    fieldDescription {
                                        text(i18nContext.get(DashboardI18nKeysData.ProfilePresets.PresetCreation.ProfilePresetDescription))
                                    }

                                    textInput {
                                        maxLength = "50"
                                        name = "presetName"
                                    }

                                    characterCounter("[name=presetName]")
                                }

                                fieldWrapper {
                                    discordButton(ButtonStyle.SUCCESS) {
                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-presets/create"
                                        attributes["bliss-include-json"] = "[name=presetName]"
                                        attributes["bliss-vals-json"] = buildJsonObject {
                                            put("activeProfileDesignId", result.activeProfileDesignId)
                                            put("activeBackgroundId", result.activeBackgroundId)
                                        }.toString()
                                        attributes["bliss-disable-when"] = "[name=presetName] == blank"
                                        attributes["bliss-swap:201"] = "body (innerHTML) -> #right-sidebar-contents (innerHTML)"
                                        attributes["bliss-push-url:201"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-presets"

                                        text(i18nContext.get(DashboardI18nKeysData.ProfilePresets.CreatePreset))
                                    }
                                }
                            }
                        }
                    )
                }
        )
    }

    private data class Result(
        val activeProfileDesignId: String,
        val activeBackgroundId: String
    )
}