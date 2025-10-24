package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.welcomer

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.channelSelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class WelcomerGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/welcomer") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val welcomerConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).welcomerConfig
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Welcomer.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.WELCOMER)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    heroWrapper {
                                        div(classes = "hero-image") {
                                            div(classes = "welcomer-web-animation") {
                                                etherealGambiImg(src = "https://stuff.loritta.website/loritta-welcomer-heathecliff.png", sizes = "350px") {
                                                    style = "height: 100%; width: 100%;"
                                                }

                                                span(classes = "welcome-wumpus-message") {
                                                    text("Welcome, ")
                                                    span(classes = "discord-mention") {
                                                        text("@Wumpus")
                                                    }
                                                    text("!")

                                                    img(src = "https://cdn.discordapp.com/emojis/417813932380520448.png?v=1", classes = "discord-inline-emoji")
                                                }
                                            }
                                        }

                                        heroText {
                                            h1 {
                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Welcomer.Title))
                                            }

                                            p {
                                                text("Anuncie quem está entrando e saindo do seu servidor da maneira que você queria! Envie mensagens para novatos via mensagem direta com informações sobre o seu servidor para não encher o chat com informações repetidas e muito mais!")
                                            }
                                        }
                                    }

                                    hr {}

                                    sectionConfig {
                                        fieldWrappers {
                                            toggleableSection(
                                                {
                                                    text("Ativar as mensagens quando alguém entrar")
                                                },
                                                null,
                                                welcomerConfig?.tellOnJoin ?: false,
                                                "tellOnJoin",
                                                true
                                            ) {
                                                fieldWrappers {
                                                    fieldWrapper {
                                                        fieldTitle {
                                                            text("Canal onde será enviado as mensagens")
                                                        }

                                                        channelSelectMenu(
                                                            guild,
                                                            welcomerConfig?.channelJoinId
                                                        ) {
                                                            attributes["loritta-config"] = "channelJoinId"
                                                            name = "channelJoinId"
                                                        }
                                                    }

                                                    fieldWrapper {
                                                        fieldTitle {
                                                            text("Segundos para deletar a mensagem (Deixe em 0 para nunca deletar)")
                                                        }

                                                        numberInput {
                                                            name = "deleteJoinMessagesAfter"
                                                            attributes["loritta-config"] = "deleteJoinMessagesAfter"
                                                            value = welcomerConfig?.deleteJoinMessagesAfter?.toString() ?: "0"
                                                            min = "0"
                                                            max = "60"
                                                            step = "1"
                                                        }
                                                    }

                                                    fieldWrapper {
                                                        fieldTitle {
                                                            text("Mensagem quando alguém entrar")
                                                        }

                                                        discordMessageEditor(
                                                            guild,
                                                            MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='channelJoinId']"),
                                                            listOf(),
                                                            welcomerConfig?.joinMessage ?: ""
                                                        ) {
                                                            attributes["loritta-config"] = "joinMessage"
                                                            name = "joinMessage"
                                                        }
                                                    }
                                                }
                                            }

                                            toggleableSection(
                                                {
                                                    text("Ativar as mensagens quando alguém sair")
                                                },
                                                null,
                                                welcomerConfig?.tellOnRemove ?: false,
                                                "tellOnRemove",
                                                true
                                            ) {
                                                fieldWrappers {
                                                    fieldWrapper {
                                                        fieldTitle {
                                                            text("Canal onde será enviado as mensagens")
                                                        }

                                                        channelSelectMenu(
                                                            guild,
                                                            welcomerConfig?.channelRemoveId
                                                        ) {
                                                            attributes["loritta-config"] = "channelRemoveId"
                                                            name = "channelRemoveId"
                                                        }
                                                    }

                                                    fieldWrapper {
                                                        fieldTitle {
                                                            text("Segundos para deletar a mensagem (Deixe em 0 para nunca deletar)")
                                                        }

                                                        numberInput {
                                                            name = "deleteRemoveMessagesAfter"
                                                            attributes["loritta-config"] = "deleteRemoveMessagesAfter"
                                                            value = welcomerConfig?.deleteRemoveMessagesAfter?.toString() ?: "0"
                                                            min = "0"
                                                            max = "60"
                                                            step = "1"
                                                        }
                                                    }

                                                    fieldWrapper {
                                                        fieldTitle {
                                                            text("Mensagem quando alguém sair")
                                                        }

                                                        discordMessageEditor(
                                                            guild,
                                                            MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='channelRemoveId']"),
                                                            listOf(),
                                                            welcomerConfig?.removeMessage ?: ""
                                                        ) {
                                                            attributes["name"] = "removeMessage"
                                                            attributes["loritta-config"] = "removeMessage"
                                                        }
                                                    }

                                                    fieldWrapper {
                                                        toggleableSection(
                                                            {
                                                                text("Ativar as mensagens quando alguém sair")
                                                            },
                                                            null,
                                                            welcomerConfig?.tellOnBan ?: false,
                                                            "tellOnBan",
                                                            true
                                                        ) {
                                                            fieldWrappers {
                                                                fieldWrapper {
                                                                    fieldTitle {
                                                                        text("Mensagem quando alguém for banido")
                                                                    }

                                                                    discordMessageEditor(
                                                                        guild,
                                                                        MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='channelRemoveId']"),
                                                                        listOf(),
                                                                        welcomerConfig?.bannedMessage ?: ""
                                                                    ) {
                                                                        attributes["name"] = "bannedMessage"
                                                                        attributes["loritta-config"] = "bannedMessage"
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            toggleableSection(
                                                {
                                                    text("Ativar as mensagens enviadas nas mensagens diretas do usuário quando alguém entrar")
                                                },
                                                {
                                                    text("Útil caso você queria mostrar informações básicas sobre o servidor para um usuário mas não quer que fique cheio de mensagens inúteis toda hora que alguém entra.")
                                                },
                                                welcomerConfig?.tellOnPrivateJoin ?: false,
                                                "tellOnPrivateJoin",
                                                true
                                            ) {
                                                fieldWrappers {
                                                    fieldWrapper {
                                                        fieldTitle {
                                                            text("Mensagem quando alguém entrar (via mensagem direta)")
                                                        }

                                                        discordMessageEditor(
                                                            guild,
                                                            MessageEditorBootstrap.TestMessageTarget.SendDirectMessage,
                                                            listOf(),
                                                            welcomerConfig?.joinPrivateMessage ?: ""
                                                        ) {
                                                            attributes["name"] = "joinPrivateMessage"
                                                            attributes["loritta-config"] = "joinPrivateMessage"
                                                        }
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
                                    "/welcomer"
                                )
                            }
                        }
                    )
                }
        )
    }
}