package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.punishmentlog

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup.RenderType
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.placeholders.sections.PunishmentMessagePlaceholders
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll
import kotlin.collections.map

class PunishmentLogGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/punishment-log") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val (moderationLogConfig, punishmentMessages) = website.loritta.transaction {
            val moderationLogConfig = website.loritta.getOrCreateServerConfig(guild.idLong).moderationConfig

            val punishmentMessages = ModerationPunishmentMessagesConfig.selectAll()
                .where {
                    ModerationPunishmentMessagesConfig.guild eq guild.idLong
                }
                .toList()

            Pair(moderationLogConfig, punishmentMessages)
        }

        val placeholders = PunishmentMessagePlaceholders.placeholders.map {
            when (it) {
                PunishmentMessagePlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(i18nContext, it, session.userId, session.cachedUserIdentification.username, session.cachedUserIdentification.globalName)
                PunishmentMessagePlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(i18nContext, it, session.cachedUserIdentification.username, session.cachedUserIdentification.globalName)
                PunishmentMessagePlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(i18nContext, it, session.cachedUserIdentification.discriminator)
                PunishmentMessagePlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(i18nContext, it, session.cachedUserIdentification.username)

                PunishmentMessagePlaceholders.GuildIconUrlPlaceholder -> createGuildIconUrlPlaceholderGroup(i18nContext, it, guild)
                PunishmentMessagePlaceholders.GuildNamePlaceholder -> createGuildNamePlaceholderGroup(i18nContext, it, guild)
                PunishmentMessagePlaceholders.GuildSizePlaceholder -> createGuildSizePlaceholderGroup(i18nContext, it, guild)
                PunishmentMessagePlaceholders.UserAvatarUrlPlaceholder -> createUserAvatarUrlPlaceholderGroup(i18nContext, it, session)
                PunishmentMessagePlaceholders.UserIdPlaceholder -> createUserIdPlaceholderGroup(i18nContext, it, session.userId)

                PunishmentMessagePlaceholders.PunishmentReasonPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "You're gonna have a bad time",
                    RenderType.TEXT
                )
                PunishmentMessagePlaceholders.PunishmentTypePlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "Banido",
                    RenderType.TEXT
                )
                PunishmentMessagePlaceholders.StaffAvatarUrlPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    session.getEffectiveAvatarUrl(),
                    RenderType.TEXT
                )
                PunishmentMessagePlaceholders.StaffDiscriminatorPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    session.cachedUserIdentification.discriminator.padStart(4, '0'),
                    RenderType.TEXT
                )
                PunishmentMessagePlaceholders.StaffIdPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    session.userId.toString(),
                    RenderType.TEXT
                )
                PunishmentMessagePlaceholders.StaffMentionPlaceholder -> createPlaceholderGroup(
                    it.placeholders,
                    null,
                    "@${session.cachedUserIdentification.globalName ?: session.cachedUserIdentification.username}",
                    "<@${session.userId}>",
                    RenderType.TEXT
                )
                PunishmentMessagePlaceholders.StaffNamePlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    session.cachedUserIdentification.globalName ?: session.cachedUserIdentification.username,
                    RenderType.TEXT
                )
                PunishmentMessagePlaceholders.StaffTagPlaceholder -> createPlaceholderGroup(
                    it,
                    null,
                    "@${session.cachedUserIdentification.username}",
                    RenderType.TEXT
                )
            }
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.PunishmentLog.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.PUNISHMENT_LOG)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            heroWrapper {
                                heroText {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.PunishmentLog.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.PunishmentLog.Description.key)
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

                            sectionConfig {
                                fieldWrappers {
                                    fieldWrapper {
                                        toggleableSection(
                                            {
                                                text("Enviar punição via mensagem direta para quem foi punido")
                                            },
                                            null,
                                            moderationLogConfig?.sendPunishmentViaDm ?: false,
                                            "sendPunishmentViaDirectMessage",
                                            true,
                                            null
                                        )
                                    }

                                    fieldWrapper {
                                        toggleableSection(
                                            {
                                                text("Enviar punições para um canal de punições")
                                            },
                                            null,
                                            moderationLogConfig?.sendPunishmentToPunishLog ?: false,
                                            "sendPunishmentToPunishLog",
                                            true
                                        ) {
                                            fieldWrappers {
                                                fieldWrapper {
                                                    fieldInformationBlock {
                                                        fieldTitle {
                                                            text("Canal de punições")
                                                        }
                                                    }

                                                    channelSelectMenu(
                                                        guild,
                                                        moderationLogConfig?.punishLogChannelId
                                                    ) {
                                                        attributes["loritta-config"] = "punishLogChannelId"
                                                        name = "punishLogChannelId"
                                                    }
                                                }

                                                discordMessageEditor(
                                                    i18nContext,
                                                    guild,
                                                    { text( "Mensagem que será mostrada quando alguém for punido") },
                                                    null,
                                                    MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                    listOf(),
                                                    placeholders,
                                                    moderationLogConfig?.punishLogMessage ?: "",
                                                    "punishLogMessage"
                                                ) {
                                                    attributes["loritta-config"] = "punishLogMessage"
                                                }

                                                fieldWrapper {
                                                    fieldInformationBlock {
                                                        fieldTitle {
                                                            text("Mensagem específicas para cada punição")
                                                        }

                                                        fieldDescription {
                                                            text("Você pode escolher mensagens diferentes para cada tipo de punição, assim colocando o seu charme em cada uma delas!")
                                                        }
                                                    }

                                                    fieldWrappers {
                                                        for (punishmentAction in PunishmentAction.entries) {
                                                            val partName = punishmentAction.name
                                                                .replace("_", "")
                                                                .lowercase()
                                                                .replaceFirstChar { it.uppercase() }

                                                            val punishmentMessage = punishmentMessages.firstOrNull {
                                                                it[ModerationPunishmentMessagesConfig.punishmentAction] == punishmentAction
                                                            }

                                                            fieldWrapper {
                                                                toggleableSection(
                                                                    {
                                                                        text(punishmentAction.name)
                                                                    },
                                                                    null,
                                                                    punishmentMessage != null,
                                                                    "enableMessageOverride$partName",
                                                                    true
                                                                ) {
                                                                    discordMessageEditor(
                                                                        i18nContext,
                                                                        guild,
                                                                        { text( "Mensagem") },
                                                                        null,
                                                                        MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                                        listOf(),
                                                                        placeholders,
                                                                        punishmentMessage?.get(ModerationPunishmentMessagesConfig.punishLogMessage) ?: "",
                                                                        "punishLogMessage$partName"
                                                                    ) {
                                                                        attributes["loritta-config"] = "punishLogMessage$partName"
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
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
                                "/punishment-log"
                            )
                        }
                    )
                }
            )
        }
    }
}