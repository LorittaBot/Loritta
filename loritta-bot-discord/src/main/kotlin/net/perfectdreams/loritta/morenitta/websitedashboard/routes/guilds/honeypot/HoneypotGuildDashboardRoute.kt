package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.honeypot

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.HoneypotConfigs
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelListInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fancySelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformation
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configReset
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class HoneypotGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/honeypot") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val honeypotConfig = website.loritta.transaction {
            HoneypotConfigs.selectAll()
                .where { HoneypotConfigs.id eq guild.idLong }
                .firstOrNull()
        }

        val enabled = honeypotConfig?.get(HoneypotConfigs.enabled) ?: false
        val currentAction = honeypotConfig?.get(HoneypotConfigs.action) ?: PunishmentAction.PURGE_KICK
        val deleteDays = honeypotConfig?.get(HoneypotConfigs.deleteDays) ?: 1
        val reason = honeypotConfig?.get(HoneypotConfigs.reason) ?: ""
        val channels = honeypotConfig?.get(HoneypotConfigs.channels)?.toSet() ?: setOf()

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Honeypot.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.HONEYPOT)
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
                                        text(i18nContext.get(DashboardI18nKeysData.Honeypot.Title))
                                    }
                                    for (line in i18nContext.get(DashboardI18nKeysData.Honeypot.Description)) {
                                        p { text(line) }
                                    }
                                }
                            }

                            hr {}

                            div {
                                id = "section-config"

                                toggleableSection(
                                    {
                                        text(i18nContext.get(DashboardI18nKeysData.Honeypot.Enable.ToggleTitle))
                                    },
                                    null,
                                    enabled,
                                    "enabled",
                                    true
                                ) {
                                    fieldWrappers {
                                        fieldWrapper {
                                            fieldInformation({
                                                text(i18nContext.get(DashboardI18nKeysData.Honeypot.Action.SectionTitle))
                                            }) {
                                                text(i18nContext.get(DashboardI18nKeysData.Honeypot.Action.SectionDescription))
                                            }

                                            fancySelectMenu {
                                                attributes["save-bar-track"] = "true"
                                                attributes["loritta-config"] = "action"
                                                name = "action"

                                                option {
                                                    label = i18nContext.get(DashboardI18nKeysData.Honeypot.Action.PurgeKick)
                                                    value = PunishmentAction.PURGE_KICK.name
                                                    selected = currentAction == PunishmentAction.PURGE_KICK
                                                }
                                                option {
                                                    label = i18nContext.get(DashboardI18nKeysData.Honeypot.Action.Ban)
                                                    value = PunishmentAction.BAN.name
                                                    selected = currentAction == PunishmentAction.BAN
                                                }
                                                option {
                                                    label = i18nContext.get(DashboardI18nKeysData.Honeypot.Action.Kick)
                                                    value = PunishmentAction.KICK.name
                                                    selected = currentAction == PunishmentAction.KICK
                                                }
                                            }
                                        }

                                        fieldWrapper {
                                            fieldInformation({
                                                text(i18nContext.get(DashboardI18nKeysData.Honeypot.DeleteDays.SectionTitle))
                                            }) {
                                                text(i18nContext.get(DashboardI18nKeysData.Honeypot.DeleteDays.SectionDescription))
                                            }

                                            numberInput {
                                                min = "0"
                                                max = "7"
                                                step = "1"
                                                value = deleteDays.toString()

                                                attributes["save-bar-track"] = "true"
                                                attributes["loritta-config"] = "deleteDays"
                                                attributes["bliss-disable-when"] = "[name='action'] == \"KICK\""
                                                name = "deleteDays"
                                            }
                                        }

                                        fieldWrapper {
                                            fieldInformation({
                                                text(i18nContext.get(DashboardI18nKeysData.Honeypot.Reason.SectionLabel))
                                            })

                                            textInput {
                                                value = reason
                                                placeholder = i18nContext.get(DashboardI18nKeysData.Honeypot.Reason.Placeholder)

                                                attributes["save-bar-track"] = "true"
                                                attributes["loritta-config"] = "reason"
                                                attributes["bliss-coerce-to-null-if-blank"] = "true"
                                                name = "reason"
                                            }
                                        }

                                        fieldWrapper {
                                            fieldInformation({
                                                text(i18nContext.get(DashboardI18nKeysData.Honeypot.Channels.SectionTitle))
                                            })

                                            configurableChannelListInput(
                                                i18nContext,
                                                guild,
                                                "channels",
                                                "channels",
                                                "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/honeypot/channels/add",
                                                "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/honeypot/channels/remove",
                                                channels
                                            )
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
                            "/honeypot"
                        )
                    }
                }
            )
        }
    }
}
