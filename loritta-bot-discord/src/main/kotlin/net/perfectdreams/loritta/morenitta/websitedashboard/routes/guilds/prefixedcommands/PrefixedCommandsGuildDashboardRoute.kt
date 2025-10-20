package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.prefixedcommands

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.prefixPreview
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggle
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class PrefixedCommandsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/prefixed-commands") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val serverConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong)
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.PrefixedCommands.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.PREFIXED_COMMANDS)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    div(classes = "hero-wrapper") {
                                        div(classes = "hero-text") {
                                            h1 {
                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.PrefixedCommands.Title))
                                            }

                                            for (str in i18nContext.language
                                                .textBundle
                                                .lists
                                                .getValue(I18nKeys.Website.Dashboard.PrefixedCommands.Description.key)
                                            ) {
                                                p {
                                                    handleI18nString(
                                                        str,
                                                        appendAsFormattedText(i18nContext, mapOf()),
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
                                                fieldTitle {
                                                    text("Prefixo da Loritta")
                                                }

                                                fieldDescription {
                                                    text("Prefixo é o texto que vem antes de um comando. Por padrão eu venho com o caractere +, mas você pode alterá-lo nesta opção.")
                                                }

                                                textInput {
                                                    attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/general/prefix-preview"
                                                    attributes["bliss-swap:200"] = "body (innerHTML) -> #prefix-preview (innerHTML)"
                                                    attributes["bliss-include-json"] = "[name='prefix']"
                                                    attributes["bliss-trigger"] = "input"
                                                    attributes["bliss-transform-text"] = "trim, no-spaces"
                                                    attributes["loritta-config"] = "prefix"
                                                    name = "prefix"

                                                    value = serverConfig.commandPrefix
                                                }

                                                div(classes = "message-preview-section") {
                                                    div(classes = "message-preview-wrapper") {
                                                        div(classes = "message-preview") {
                                                            div(classes = "discord-style") {
                                                                id = "prefix-preview"

                                                                prefixPreview(
                                                                    session,
                                                                    serverConfig.commandPrefix,
                                                                    website.loritta.lorittaShards.shardManager.shards.first().selfUser
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            fieldWrapper {
                                                toggle(
                                                    serverConfig.deleteMessageAfterCommand,
                                                    "deleteMessageAfterCommand",
                                                    true,
                                                    {
                                                        text(i18nContext.get(DashboardI18nKeysData.PrefixedCommands.DeleteMessageAfterCommand.Title))
                                                    },
                                                    {
                                                        text(i18nContext.get(DashboardI18nKeysData.PrefixedCommands.DeleteMessageAfterCommand.Description))
                                                    }
                                                )
                                            }

                                            fieldWrapper {
                                                toggle(
                                                    serverConfig.warnOnUnknownCommand,
                                                    "warnOnUnknownCommand",
                                                    true,
                                                    {
                                                        text(i18nContext.get(DashboardI18nKeysData.PrefixedCommands.WarnOnUnknownCommand.Title))
                                                    },
                                                    {
                                                        text(i18nContext.get(DashboardI18nKeysData.PrefixedCommands.WarnOnUnknownCommand.Description))
                                                    }
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
                                        "/prefixed-commands"
                                    )
                                }
                            )
                        }
                    )
                }
        )
    }
}