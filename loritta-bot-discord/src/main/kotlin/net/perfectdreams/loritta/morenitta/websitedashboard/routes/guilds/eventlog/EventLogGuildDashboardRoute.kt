package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.eventlog

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.channelSelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class EventLogGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/event-log") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val eventLogConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).eventLogConfig
        }

        fun FlowContent.eventLogTypeSection(
            title: String,
            description: String?,
            sectionFieldName: String,
            enabled: Boolean,
            channelId: Long?,
        ) {
            toggleableSection(
                {
                    text(title)
                },
                if (description != null) {
                    { text(description) }
                } else null,
                enabled,
                sectionFieldName,
                true
            ) {
                fieldWrappers {
                    fieldWrapper {
                        fieldInformationBlock {
                            fieldTitle { text(i18nContext.get(DashboardI18nKeysData.EventLog.ChannelWhereTheActionsWillBeSent)) }
                        }

                        channelSelectMenu(
                            guild,
                            channelId,
                            additionalOptions = {
                                option {
                                    this.label = i18nContext.get(DashboardI18nKeysData.EventLog.UseDefaultChannel)
                                    this.disabled = false
                                    if (channelId == null)
                                        this.selected = true
                                }
                            }
                        ) {
                            attributes["bliss-coerce-to-null-if-blank"] = "true"
                            attributes["loritta-config"] = "${sectionFieldName}LogChannelId"
                            name = "${sectionFieldName}LogChannelId"
                        }
                    }
                }
            }
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.EventLog.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.EVENT_LOG)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.EventLog.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.EventLog.Description.key)
                                    ) {
                                        p {
                                            handleI18nString(
                                                str,
                                                appendAsFormattedText(i18nContext, emptyMap()),
                                            ) {
                                                when (it) {
                                                    else -> TextReplaceControls.AppendControlAsIsResult
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            hr {}

                            div {
                                id = "section-config"

                                fieldWrappers {
                                    fieldWrapper {
                                        fieldInformationBlock {
                                            fieldTitle { text(i18nContext.get(DashboardI18nKeysData.EventLog.DefaultChannelWhereTheActionsWillBeSent)) }
                                        }

                                        channelSelectMenu(
                                            guild,
                                            eventLogConfig?.eventLogChannelId,
                                        ) {
                                            attributes["loritta-config"] = "eventLogChannelId"
                                            name = "eventLogChannelId"
                                        }
                                    }

                                    fieldWrapper {
                                        eventLogTypeSection(
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.MemberBanned.Title),
                                            null,
                                            "memberBanned",
                                            eventLogConfig?.memberBanned ?: false,
                                            eventLogConfig?.memberBannedLogChannelId
                                        )
                                    }

                                    fieldWrapper {
                                        eventLogTypeSection(
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.MemberUnbanned.Title),
                                            null,
                                            "memberUnbanned",
                                            eventLogConfig?.memberUnbanned ?: false,
                                            eventLogConfig?.memberUnbannedLogChannelId
                                        )
                                    }

                                    fieldWrapper {
                                        eventLogTypeSection(
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.MessageEdited.Title),
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.MessageEdited.Description),
                                            "messageEdited",
                                            eventLogConfig?.messageEdited ?: false,
                                            eventLogConfig?.messageEditedLogChannelId
                                        )
                                    }

                                    fieldWrapper {
                                        eventLogTypeSection(
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.MessageDeleted.Title),
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.MessageDeleted.Description),
                                            "messageDeleted",
                                            eventLogConfig?.messageDeleted ?: false,
                                            eventLogConfig?.messageDeletedLogChannelId
                                        )
                                    }

                                    fieldWrapper {
                                        eventLogTypeSection(
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.NicknameChanges.Title),
                                            null,
                                            "nicknameChanges",
                                            eventLogConfig?.nicknameChanges ?: false,
                                            eventLogConfig?.nicknameChangesLogChannelId
                                        )
                                    }

                                    fieldWrapper {
                                        eventLogTypeSection(
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.AvatarChanges.Title),
                                            null,
                                            "avatarChanges",
                                            eventLogConfig?.avatarChanges ?: false,
                                            eventLogConfig?.avatarChangesLogChannelId
                                        )
                                    }

                                    fieldWrapper {
                                        eventLogTypeSection(
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.VoiceChannelJoins.Title),
                                            null,
                                            "voiceChannelJoins",
                                            eventLogConfig?.voiceChannelJoins ?: false,
                                            eventLogConfig?.voiceChannelJoinsLogChannelId
                                        )
                                    }

                                    fieldWrapper {
                                        eventLogTypeSection(
                                            i18nContext.get(DashboardI18nKeysData.EventLog.Types.VoiceChannelLeaves.Title),
                                            null,
                                            "voiceChannelLeaves",
                                            eventLogConfig?.voiceChannelLeaves ?: false,
                                            eventLogConfig?.voiceChannelLeavesLogChannelId
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
                                "/event-log"
                            )
                        }
                    )
                }
            )
        }
    }
}