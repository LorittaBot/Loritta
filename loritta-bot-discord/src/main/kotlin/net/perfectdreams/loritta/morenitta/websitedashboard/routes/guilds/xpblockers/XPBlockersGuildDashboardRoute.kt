package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelListInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableRoleListInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configReset
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class XPBlockersGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-blockers") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val levelConfig = website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            serverConfig.levelConfig
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.XpBlockers.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.XP_BLOCKERS)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                configReset(i18nContext)
                            }

                            heroWrapper {
                                heroText {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.XpBlockers.Title))
                                    }

                                    p {
                                        text(i18nContext.get(DashboardI18nKeysData.XpBlockers.Description))
                                    }
                                }
                            }

                            hr {}

                            sectionConfig {
                                fieldWrappers {
                                    fieldWrapper {
                                        fieldInformationBlock {
                                            fieldTitle {
                                                text(i18nContext.get(DashboardI18nKeysData.XpBlockers.Roles.Title))
                                            }

                                            fieldDescription {
                                                text(i18nContext.get(DashboardI18nKeysData.XpBlockers.Roles.Description))
                                            }
                                        }

                                        configurableRoleListInput(
                                            i18nContext,
                                            guild,
                                            "roles",
                                            "roles",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-blockers/roles/add",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-blockers/roles/remove",
                                            levelConfig?.noXpRoles?.toSet() ?: setOf()
                                        )
                                    }

                                    fieldWrapper {
                                        fieldInformationBlock {
                                            fieldTitle {
                                                text(i18nContext.get(DashboardI18nKeysData.XpBlockers.Channels.Title))
                                            }

                                            fieldDescription {
                                                text(i18nContext.get(DashboardI18nKeysData.XpBlockers.Channels.Description))
                                            }
                                        }

                                        configurableChannelListInput(
                                            i18nContext,
                                            guild,
                                            "channels",
                                            "channels",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-blockers/channels/add",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-blockers/channels/remove",
                                            levelConfig?.noXpChannels?.toSet() ?: setOf()
                                        )
                                    }
                                }
                            }
                        },
                        {
                            genericSaveBar(
                                i18nContext,
                                false,
                                guild,
                                "/xp-blockers"
                            )
                        }
                    )
                }
            )
        }
    }
}