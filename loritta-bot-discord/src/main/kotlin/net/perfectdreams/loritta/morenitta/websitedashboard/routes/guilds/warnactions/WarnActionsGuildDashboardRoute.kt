package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.dashboard.EmbeddedToast
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

class WarnActionsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/warn-actions") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
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

        call.respondHtml(
            createHTML(false)
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.WarnActions.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.WARN_ACTIONS)
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
                                                text(i18nContext.get(DashboardI18nKeysData.WarnActions.Title))
                                            }

                                            p {
                                                text("Punições e etc")
                                            }
                                        }
                                    }

                                    hr {}

                                    sectionConfig {
                                        div {
                                            text("Ao chegar em ")
                                            numberInput {
                                                attributes["warn-action-add-element"] = "true"
                                                name = "count"
                                                style = "width: 100px;"
                                                value = "1"
                                                min = "1"
                                            }
                                            text(" avisos, ")
                                            select {
                                                attributes["warn-action-add-element"] = "true"
                                                name = "action"
                                                option {
                                                    label = "KICK"
                                                    value = "KICK"
                                                }
                                                option {
                                                    label = "BAN"
                                                    value = "BAN"
                                                }
                                                option {
                                                    label = "MUTE"
                                                    value = "MUTE"
                                                }
                                            }
                                            text(" o usuário por ")
                                            textInput {
                                                style = "width: 200px;"

                                                attributes["warn-action-add-element"] = "true"
                                                attributes["bliss-disable-when"] = "[name='action'] != \"MUTE\""
                                                attributes["bliss-coerce-to-null-if-blank"] = "true"
                                                name = "time"
                                            }
                                            discordButton(ButtonStyle.SUCCESS) {
                                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeys.Website.LocalePathId)}/guilds/${guild.idLong}/warn-actions/add"
                                                attributes["bliss-include-json"] = "[warn-action-add-element]"
                                                attributes["bliss-swap:200"] = "body (innerHTML) -> #warn-actions (innerHTML)"
                                                text("Adicionar")
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
        )
    }
}