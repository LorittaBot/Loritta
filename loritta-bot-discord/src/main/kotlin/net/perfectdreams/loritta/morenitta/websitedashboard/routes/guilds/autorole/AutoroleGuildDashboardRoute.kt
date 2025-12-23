package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableRoleListInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configReset
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class AutoroleGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/autorole") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val autoroleConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).autoroleConfig
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Autorole.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.AUTOROLE)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                configReset(i18nContext)
                            }

                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.Autorole.Title))
                                    }

                                    p {
                                        text(i18nContext.get(DashboardI18nKeysData.Autorole.Description))
                                    }
                                }
                            }

                            hr {}

                            sectionConfig {
                                toggleableSection(
                                    {
                                        text(i18nContext.get(DashboardI18nKeysData.Autorole.Enable.ToggleTitle))
                                    },
                                    null,
                                    autoroleConfig?.enabled ?: false,
                                    "enabled",
                                    true,
                                ) {
                                    fieldWrappers {
                                        fieldWrapper {
                                            fieldInformationBlock {
                                                fieldTitle {
                                                    text(i18nContext.get(DashboardI18nKeysData.Autorole.Roles.SectionTitle))
                                                }
                                            }

                                            configurableRoleListInput(
                                                i18nContext,
                                                guild,
                                                "roles",
                                                "roles",
                                                "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/autorole/roles/add",
                                                "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/autorole/roles/remove",
                                                autoroleConfig?.roles?.toSet() ?: setOf()
                                            )
                                        }

                                        fieldWrapper {
                                            toggle(
                                                autoroleConfig?.giveOnlyAfterMessageWasSent ?: false,
                                                "giveOnlyAfterMessageWasSent",
                                                true,
                                                {
                                                    text(i18nContext.get(DashboardI18nKeysData.Autorole.GiveAfterMessage.ToggleTitle))
                                                },
                                                {
                                                    text(i18nContext.get(DashboardI18nKeysData.Autorole.GiveAfterMessage.ToggleDescription))
                                                }
                                            )
                                        }

                                        fieldWrapper {
                                            fieldInformationBlock {
                                                fieldTitle {
                                                    text(i18nContext.get(DashboardI18nKeysData.Autorole.GiveRolesAfter.SectionTitle))
                                                }
                                            }

                                            numberInput {
                                                name = "giveRolesAfter"
                                                attributes["loritta-config"] = "giveRolesAfter"
                                                value = autoroleConfig?.giveRolesAfter?.toString() ?: "0"
                                                min = "0"
                                                max = "60"
                                                step = "1"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        genericSaveBar(
                            i18nContext,
                            false,
                            guild,
                            "/autorole"
                        )
                    }
                }
            )
        }
    }
}