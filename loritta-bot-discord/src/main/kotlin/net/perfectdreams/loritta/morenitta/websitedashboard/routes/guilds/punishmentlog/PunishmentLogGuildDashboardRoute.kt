package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.punishmentlog

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

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

        call.respondHtml(
            createHTML(false)
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.PunishmentLog.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.PUNISHMENT_LOG)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
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
                                                            fieldTitle {
                                                                text("Canal de punições")
                                                            }

                                                            channelSelectMenu(
                                                                guild,
                                                                moderationLogConfig?.punishLogChannelId
                                                            ) {
                                                                attributes["loritta-config"] = "punishLogChannelId"
                                                                name = "punishLogChannelId"
                                                            }
                                                        }

                                                        fieldWrapper {
                                                            fieldTitle {
                                                                text("Mensagem que será mostrada quando alguém for punido")
                                                            }

                                                            discordMessageEditor(
                                                                guild,
                                                                MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                                listOf(),
                                                                listOf(),
                                                                moderationLogConfig?.punishLogMessage ?: ""
                                                            ) {
                                                                attributes["loritta-config"] = "punishLogMessage"
                                                                name = "punishLogMessage"
                                                            }
                                                        }

                                                        fieldWrapper {
                                                            fieldTitle {
                                                                text("Mensagem específicas para cada punição")
                                                            }

                                                            fieldDescription {
                                                                text("Você pode escolher mensagens diferentes para cada tipo de punição, assim colocando o seu charme em cada uma delas!")
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
                                                                                guild,
                                                                                MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                                                listOf(),
                                                                                listOf(),
                                                                                punishmentMessage?.get(ModerationPunishmentMessagesConfig.punishLogMessage) ?: ""
                                                                            ) {
                                                                                attributes["loritta-config"] = "punishLogMessage$partName"
                                                                                name = "punishLogMessage$partName"
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
        )
    }
}