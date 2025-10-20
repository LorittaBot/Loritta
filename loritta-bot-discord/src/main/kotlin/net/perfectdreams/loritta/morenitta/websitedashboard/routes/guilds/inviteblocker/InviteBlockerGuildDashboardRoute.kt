package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelList
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.loadingSpinnerImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class InviteBlockerGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/invite-blocker") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val inviteBlockerConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).inviteBlockerConfig
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.InviteBlocker.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.INVITE_BLOCKER)
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
                                                text("Bloqueador de Convites")
                                            }

                                            p {
                                                text("Bloqueador de Convites")
                                            }
                                        }
                                    }

                                    hr {}

                                    div {
                                        id = "section-config"

                                        toggleableSection(
                                            {
                                                text("Ativar Bloqueador de Convites")
                                            },
                                            null,
                                            inviteBlockerConfig?.enabled ?: false,
                                            "enabled",
                                            true
                                        ) {
                                            fieldWrappers {
                                                fieldWrapper {
                                                    toggle(
                                                        inviteBlockerConfig?.whitelistServerInvites ?: false,
                                                        "allowServerInvites",
                                                        true,
                                                        {
                                                            text("Permitir compartilhar convites do servidor atual")
                                                        }
                                                    )
                                                }

                                                fieldWrapper {
                                                    toggle(
                                                        inviteBlockerConfig?.deleteMessage ?: false,
                                                        "deleteMessageOnInvite",
                                                        true,
                                                        {
                                                            text("Deletar a mensagem do usuário quando um invite for detectado")
                                                        }
                                                    )
                                                }

                                                fieldWrapper {
                                                    fieldTitle {
                                                        text("Canais aonde são permitidos enviar convites")
                                                    }

                                                    div {
                                                        style = "display: flex; flex-gap: 0.5em;"
                                                        select {
                                                            style = "flex-grow: 1;"

                                                            name = "channelId"

                                                            for (channel in guild.channels) {
                                                                if (channel is GuildMessageChannel) {
                                                                    option {
                                                                        label = channel.name
                                                                        value = channel.id
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        discordButton(ButtonStyle.SUCCESS) {
                                                            id = "add-channel-button"
                                                            attributes["bliss-indicator"] = "this"
                                                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/invite-blocker/channels/add"
                                                            attributes["bliss-include-json"] = "[name='channelId'],[name='channels[]']"
                                                            attributes["bliss-swap:200"] = "body (innerHTML) -> #channels (innerHTML)"
                                                            attributes["bliss-sync"] = "#add-channel-button"

                                                            div {
                                                                text("Adicionar")
                                                            }

                                                            div(classes = "loading-text-wrapper") {
                                                                loadingSpinnerImage()

                                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                                                            }
                                                        }
                                                    }

                                                    div {
                                                        id = "channels"

                                                        configurableChannelList(
                                                            i18nContext,
                                                            guild,
                                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/invite-blocker/channels/remove",
                                                            inviteBlockerConfig?.whitelistedChannels?.toSet() ?: setOf()
                                                        )
                                                    }
                                                }

                                                toggleableSection(
                                                    {
                                                        text("Enviar uma mensagem ao usuário quando ele enviar um invite")
                                                    },
                                                    null,
                                                    inviteBlockerConfig?.tellUser ?: false,
                                                    "sendMessageOnInvite",
                                                    true,
                                                ) {
                                                    discordMessageEditor(
                                                        guild,
                                                        MessageEditorBootstrap.TestMessageTarget.Unavailable,
                                                        listOf(),
                                                        inviteBlockerConfig?.warnMessage ?: ""
                                                    ) {
                                                        name = "message"
                                                        attributes["loritta-config"] = "message"
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
                                    "/invite-blocker"
                                )
                            }
                        }
                    )
                }
        )
    }
}