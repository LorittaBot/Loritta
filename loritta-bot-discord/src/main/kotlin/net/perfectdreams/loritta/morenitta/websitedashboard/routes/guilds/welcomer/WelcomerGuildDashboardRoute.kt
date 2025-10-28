package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.welcomer

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
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
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.placeholders.sections.JoinMessagePlaceholders
import net.perfectdreams.loritta.placeholders.sections.LeaveMessagePlaceholders
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings

class WelcomerGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/welcomer") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val welcomerConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).welcomerConfig
        }

        val defaultJoinMessage = createMessageTemplate(
            "Padrão",
            "\uD83D\uDC49 {@user} entrou no servidor!"
        )

        val defaultLeaveMessage = createMessageTemplate(
            "Padrão",
            "\uD83D\uDC48 {user.name} saiu do servidor..."
        )

        val joinMessagePlaceholders = JoinMessagePlaceholders.placeholders.map {
            when (it) {
                JoinMessagePlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(
                    i18nContext,
                    it,
                    session.userId,
                    session.username,
                    session.globalName
                )

                JoinMessagePlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(
                    i18nContext,
                    it,
                    session.username,
                    session.globalName
                )

                JoinMessagePlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(
                    i18nContext,
                    it,
                    session.discriminator
                )

                JoinMessagePlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(
                    i18nContext,
                    it,
                    session.discriminator
                )

                JoinMessagePlaceholders.GuildIconUrlPlaceholder -> createGuildIconUrlPlaceholderGroup(i18nContext, it, guild)
                JoinMessagePlaceholders.GuildNamePlaceholder -> createGuildNamePlaceholderGroup(i18nContext, it, guild)
                JoinMessagePlaceholders.GuildSizePlaceholder -> createGuildSizePlaceholderGroup(i18nContext, it, guild)
                JoinMessagePlaceholders.UserAvatarUrlPlaceholder -> createUserAvatarUrlPlaceholderGroup(i18nContext, it, session)
                JoinMessagePlaceholders.UserIdPlaceholder -> createUserIdPlaceholderGroup(i18nContext, it, session.userId)
            }
        }

        val leaveMessagePlaceholders = LeaveMessagePlaceholders.placeholders.map {
            when (it) {
                LeaveMessagePlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(
                    i18nContext,
                    it,
                    session.userId,
                    session.username,
                    session.globalName
                )

                LeaveMessagePlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(
                    i18nContext,
                    it,
                    session.username,
                    session.globalName
                )

                LeaveMessagePlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(
                    i18nContext,
                    it,
                    session.discriminator
                )

                LeaveMessagePlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(
                    i18nContext,
                    it,
                    session.discriminator
                )

                LeaveMessagePlaceholders.GuildIconUrlPlaceholder -> createGuildIconUrlPlaceholderGroup(i18nContext, it, guild)
                LeaveMessagePlaceholders.GuildNamePlaceholder -> createGuildNamePlaceholderGroup(i18nContext, it, guild)
                LeaveMessagePlaceholders.GuildSizePlaceholder -> createGuildSizePlaceholderGroup(i18nContext, it, guild)
                LeaveMessagePlaceholders.UserAvatarUrlPlaceholder -> createUserAvatarUrlPlaceholderGroup(i18nContext, it, session)
                LeaveMessagePlaceholders.UserIdPlaceholder -> createUserIdPlaceholderGroup(i18nContext, it, session.userId)
            }
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Welcomer.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.WELCOMER)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        userPremiumPlan,
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
                                                    listOf(defaultJoinMessage),
                                                    joinMessagePlaceholders,
                                                    welcomerConfig?.joinMessage ?: defaultJoinMessage.content
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
                                                    listOf(defaultLeaveMessage),
                                                    leaveMessagePlaceholders,
                                                    welcomerConfig?.removeMessage ?: defaultLeaveMessage.content
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
                                                                leaveMessagePlaceholders,
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
                                                    joinMessagePlaceholders,
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
    }
}