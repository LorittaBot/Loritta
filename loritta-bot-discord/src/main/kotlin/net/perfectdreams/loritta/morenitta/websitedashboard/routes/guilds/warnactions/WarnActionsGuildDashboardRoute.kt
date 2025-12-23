package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configReset
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class WarnActionsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/warn-actions") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val warnActions = website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            val moderationConfig = serverConfig.moderationConfig

            if (moderationConfig != null) {
                WarnActions.selectAll()
                    .where {
                        WarnActions.config eq moderationConfig.id
                    }
                    .toList()
            } else listOf()
        }

        val actions = warnActions.map {
            WarnAction(
                it[WarnActions.warnCount],
                it[WarnActions.punishmentAction],
                it[WarnActions.metadata]?.let {
                    val obj = Json.parseToJsonElement(it) as JsonObject
                    obj["time"]?.jsonPrimitive?.content
                }
            )
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.WarnActions.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.WARN_ACTIONS)
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
                                        text(i18nContext.get(DashboardI18nKeysData.WarnActions.Title))
                                    }

                                    p {
                                        text(i18nContext.get(DashboardI18nKeysData.WarnActions.Description))
                                    }
                                }
                            }

                            hr {}

                            sectionConfig {
                                fieldWrappers {
                                    fieldWrapper {
                                        controlsWithButton {
                                            inlinedControls {
                                                handleI18nString(
                                                    i18nContext,
                                                    I18nKeys.Website.Dashboard.WarnActions.AddRuleInline,
                                                    appendAsFormattedText(i18nContext, mapOf())
                                                ) {
                                                    when (it) {
                                                        "countInput" -> {
                                                            TextReplaceControls.ComposableFunctionResult {
                                                                numberInput {
                                                                    attributes["warn-action-add-element"] = "true"
                                                                    name = "count"
                                                                    style = "width: 100px;"
                                                                    value = "1"
                                                                    min = "1"
                                                                }
                                                            }
                                                        }
                                                        "actionSelect" -> {
                                                            TextReplaceControls.ComposableFunctionResult {
                                                                fancySelectMenu {
                                                                    attributes["warn-action-add-element"] = "true"
                                                                    name = "action"
                                                                    option {
                                                                        // Use i18n titles for display labels
                                                                        label = i18nContext.get(DashboardI18nKeysData.PunishmentLog.ActionTitles.Kick)
                                                                        value = "KICK"
                                                                    }
                                                                    option {
                                                                        label = i18nContext.get(DashboardI18nKeysData.PunishmentLog.ActionTitles.Ban)
                                                                        value = "BAN"
                                                                    }
                                                                    option {
                                                                        label = i18nContext.get(DashboardI18nKeysData.PunishmentLog.ActionTitles.Mute)
                                                                        value = "MUTE"
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        "timeInput" -> {
                                                            TextReplaceControls.ComposableFunctionResult {
                                                                growInputWrapper {
                                                                    textInput {
                                                                        attributes["warn-action-add-element"] = "true"
                                                                        attributes["bliss-disable-when"] = "[name='action'] != \"MUTE\""
                                                                        attributes["bliss-coerce-to-null-if-blank"] = "true"
                                                                        name = "time"
                                                                        placeholder = i18nContext.get(DashboardI18nKeysData.WarnActions.TimePlaceholder)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        else -> TextReplaceControls.AppendControlAsIsResult
                                                    }
                                                }
                                            }

                                            discordButton(ButtonStyle.SUCCESS) {
                                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeys.Website.LocalePathId)}/guilds/${guild.idLong}/warn-actions/add"
                                                attributes["bliss-include-json"] = "[warn-action-add-element]"
                                                attributes["bliss-swap:200"] = "body (innerHTML) -> #warn-actions (innerHTML)"
                                                text(i18nContext.get(DashboardI18nKeysData.WarnActions.AddButtonLabel))
                                            }
                                        }

                                        div {
                                            id = "warn-actions"

                                            configurableWarnList(
                                                i18nContext,
                                                guild,
                                                actions
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        {
                            genericSaveBar(
                                i18nContext,
                                false,
                                guild,
                                "/warn-actions"
                            )
                        }
                    )
                }
            )
        }
    }
}