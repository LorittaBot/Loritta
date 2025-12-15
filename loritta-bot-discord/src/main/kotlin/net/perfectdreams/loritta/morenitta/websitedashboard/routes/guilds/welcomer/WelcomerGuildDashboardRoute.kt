package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.welcomer

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordComponent
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordEmbed
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
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
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val welcomerConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).welcomerConfig
        }

        val defaultJoinMessage = createMessageTemplate(
            "Padrão",
            "\uD83D\uDC49 {@user} entrou no servidor!"
        )

        val joinTemplates = listOf(
            defaultJoinMessage,
            createMessageTemplate(
                "Padrão, só que melhor",
                "<a:lori_happy:521721811298156558> | {@user} acabou de entrar na {guild}! Agora temos {guild.size} membros!"
            ),
            createMessageTemplate(
                "Lista de Informações",
                """{@user}, bem-vindo(a) ao {guild}! <a:lori_happy:521721811298156558>
                • Leia as #regras *(psiu, troque o nome do canal aqui na mensagem!)* para você poder conviver em harmonia! <:blobheart:467447056374693889>
                • Converse no canal de #bate-papo <:lori_pac:503600573741006863>
                • E é claro, divirta-se! <a:emojo_feriado:393751205308006421>

                Aliás, continue sendo incrível! (E eu sou muito fofa! :3)
            """.trimIndent()
            ),
            createMessageTemplate(
                "Embed Simples",
                DiscordMessage(
                    content = "{@user}",
                    embed = DiscordEmbed(
                        color = -9270822,
                        title = "👋 Seja bem-vindo(a)!",
                        description = "Olá {@user}! Seja bem-vindo(a) ao {guild}!"
                    )
                )
            ),
            createMessageTemplate(
                "Embed com Avatar",
                DiscordMessage(
                    content = "{@user}",
                    embed = DiscordEmbed(
                        color = -9270822,
                        title = "👋 Bem-vindo(a)!",
                        description = "Olá {@user}, espero que você se divirta no meu servidor! <:loritta:331179879582269451>",
                        author = DiscordEmbed.Author(
                            "{user.tag}",
                            iconUrl = "{user.avatar}"
                        ),
                        thumbnail = DiscordEmbed.EmbedUrl(
                            "{user.avatar}"
                        ),
                        footer = DiscordEmbed.Footer(
                            "ID do usuário: {user.id}"
                        )
                    )
                )
            ),
            createMessageTemplate(
                "Embed com Avatar e Imagem",
                DiscordMessage(
                    content = "{@user}",
                    embed = DiscordEmbed(
                        color = -9270822,
                        title = "👋 Bem-vindo(a)!",
                        description = "Olá {@user}, espero que você se divirta no meu servidor! <:loritta:331179879582269451>",
                        author = DiscordEmbed.Author(
                            "{user.tag}",
                            iconUrl = "{user.avatar}"
                        ),
                        thumbnail = DiscordEmbed.EmbedUrl(
                            "{user.avatar}"
                        ),
                        image = DiscordEmbed.EmbedUrl(
                            "https://media.giphy.com/media/GPQBFuG4ABACA/source.gif"
                        ),
                        footer = DiscordEmbed.Footer(
                            "ID do usuário: {user.id}"
                        )
                    )
                )
            ),
            createMessageTemplate(
                "Embed com Informações",
                DiscordMessage(
                    content = "{@user}",
                    embed = DiscordEmbed(
                        color = -14689638,
                        title = "{user} | Bem-vindo(a)!",
                        description = "<:lori_hug:515328576611155968> Olá, seja bem-vindo(a) ao {guild}!",
                        fields = listOf(
                            DiscordEmbed.Field(
                                "\uD83D\uDC4B Sabia que...",
                                "Atualmente temos {guild.size} membros no servidor?",
                                true
                            ),
                            DiscordEmbed.Field(
                                "🛡 Tag do Usuário",
                                "`{user.tag}` ({user.id})",
                                true
                            ),
                            DiscordEmbed.Field(
                                "\uD83D\uDCDB Precisando de ajuda?",
                                "Caso você tenha alguma dúvida ou problema, chame a nossa equipe!",
                                true
                            ),
                            DiscordEmbed.Field(
                                "\uD83D\uDC6E Evite punições!",
                                "Leia as nossas #regras para evitar ser punido no servidor!",
                                true
                            )
                        ),
                        thumbnail = DiscordEmbed.EmbedUrl(
                            "{user.avatar}"
                        ),
                        footer = DiscordEmbed.Footer(
                            "{guild} • © Todos os direitos reservados."
                        )
                    )
                )
            ),
            createMessageTemplate(
                "Kit Social Influencer™",
                DiscordMessage(
                    content = "{@user}",
                    embed = DiscordEmbed(
                        color = -2342853,
                        title = "{user} | Bem-vindo(a)!",
                        description = "Salve {@user}! Você acabou de entrar no servidor do {guild}, aqui você poderá se interagir com fãs do {guild}, conversar sobre suas coisas favoritas e muito mais!",
                        fields = listOf(
                            DiscordEmbed.Field(
                                "\uD83D\uDCE2 Fique atento!",
                                "Novos vídeos do {guild} serão anunciados no #vídeos-novos!",
                                true
                            )
                        ),
                        thumbnail = DiscordEmbed.EmbedUrl(
                            "{user.avatar}"
                        ),
                        footer = DiscordEmbed.Footer(
                            "{guild} • © Todos os direitos reservados."
                        )
                    ),
                    components = listOf(
                        DiscordComponent.DiscordActionRow(
                            components = listOf(
                                DiscordComponent.DiscordButton(
                                    label = "YouTube",
                                    style = 5,
                                    url = "https://www.youtube.com/@Loritta"
                                ),
                                DiscordComponent.DiscordButton(
                                    label = "Bluesky",
                                    style = 5,
                                    url = "https://bsky.app/profile/loritta.website"
                                ),
                                DiscordComponent.DiscordButton(
                                    label = "Instagram",
                                    style = 5,
                                    url = "https://www.instagram.com/lorittabot/"
                                ),
                                DiscordComponent.DiscordButton(
                                    label = "TikTok",
                                    style = 5,
                                    url = "https://www.tiktok.com/@lorittamorenittabot"
                                ),
                                DiscordComponent.DiscordButton(
                                    label = "Nosso Website",
                                    style = 5,
                                    url = "https://loritta.website/"
                                )
                            )
                        )
                    )
                )
            )
        )

        val defaultLeaveMessage = createMessageTemplate(
            "Padrão",
            "\uD83D\uDC48 {user.name} saiu do servidor..."
        )

        val leaveTemplates = listOf(
            defaultLeaveMessage,
            createMessageTemplate(
                "Padrão, só que melhor",
                "<a:bongo_lori_triste:524894216510373888> | {user} saiu do {guild}... espero que algum dia ele volte...",
            ),
            createMessageTemplate(
                "Embed Simples",
                DiscordMessage(
                    content = "",
                    embed = DiscordEmbed(
                        color = -6250077,
                        title = "Tchau...",
                        description = "{user} saiu do {guild}... espero que algum dia ele volte..."
                    )
                )
            ),
            createMessageTemplate(
                "Embed com Avatar",
                DiscordMessage(
                    content = "",
                    embed = DiscordEmbed(
                        color = -6250077,
                        title = "😭 #chateada!",
                        description = "⚰ **{user}** saiu do servidor... <:lori_triste:370344565967814659>",
                        author = DiscordEmbed.Author(
                            "{user.tag}",
                            iconUrl = "{user.avatar}"
                        ),
                        thumbnail = DiscordEmbed.EmbedUrl(
                            "{user.avatar}"
                        ),
                        footer = DiscordEmbed.Footer(
                            "ID do usuário: {user.id}"
                        )
                    )
                )
            ),
            createMessageTemplate(
                "Embed com Avatar e Imagem",
                DiscordMessage(
                    content = "{@user}",
                    embed = DiscordEmbed(
                        color = -9270822,
                        title = "😭 #chateada!",
                        description = "⚰ **{user}** saiu do servidor... <:lori_triste:370344565967814659>",
                        author = DiscordEmbed.Author(
                            "{user.tag}",
                            iconUrl = "{user.avatar}"
                        ),
                        thumbnail = DiscordEmbed.EmbedUrl(
                            "{user.avatar}"
                        ),
                        image = DiscordEmbed.EmbedUrl(
                            "https://i.imgur.com/RUIaWW3.png"
                        ),
                        footer = DiscordEmbed.Footer(
                            "ID do usuário: {user.id}"
                        )
                    )
                )
            )
        )

        val joinMessagePlaceholders = JoinMessagePlaceholders.placeholders.map {
            when (it) {
                JoinMessagePlaceholders.UserMentionPlaceholder -> createUserMentionPlaceholderGroup(
                    i18nContext,
                    it,
                    session.userId,
                    session.cachedUserIdentification.username,
                    session.cachedUserIdentification.globalName
                )

                JoinMessagePlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.username,
                    session.cachedUserIdentification.globalName
                )

                JoinMessagePlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.discriminator
                )

                JoinMessagePlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.username
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
                    session.cachedUserIdentification.username,
                    session.cachedUserIdentification.globalName
                )

                LeaveMessagePlaceholders.UserNamePlaceholder -> createUserNamePlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.username,
                    session.cachedUserIdentification.globalName
                )

                LeaveMessagePlaceholders.UserDiscriminatorPlaceholder -> createUserDiscriminatorPlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.discriminator
                )

                LeaveMessagePlaceholders.UserTagPlaceholder -> createUserTagPlaceholderGroup(
                    i18nContext,
                    it,
                    session.cachedUserIdentification.username
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
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.WELCOMER)
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
                                            text(i18nContext.get(DashboardI18nKeysData.Welcomer.JoinSection.Toggle))
                                        },
                                        null,
                                        welcomerConfig?.tellOnJoin ?: false,
                                        "tellOnJoin",
                                        true
                                    ) {
                                        fieldWrappers {
                                            fieldWrapper {
                                                fieldInformationBlock {
                                                    fieldTitle {
                                                        text(i18nContext.get(DashboardI18nKeysData.Welcomer.JoinSection.ChannelTitle))
                                                    }
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
                                                fieldInformationBlock {
                                                    fieldTitle {
                                                        text(i18nContext.get(DashboardI18nKeysData.Welcomer.JoinSection.DeleteAfterTitle))
                                                    }
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

                                            discordMessageEditor(
                                                i18nContext,
                                                guild,
                                                { text(i18nContext.get(DashboardI18nKeysData.Welcomer.JoinSection.MessageEditorTitle)) },
                                                null,
                                                MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='channelJoinId']"),
                                                joinTemplates,
                                                joinMessagePlaceholders,
                                                welcomerConfig?.joinMessage ?: defaultJoinMessage.content,
                                                "joinMessage"
                                            ) {
                                                attributes["loritta-config"] = "joinMessage"
                                            }
                                        }
                                    }

                                    toggleableSection(
                                        {
                                            text(i18nContext.get(DashboardI18nKeysData.Welcomer.LeaveSection.Toggle))
                                        },
                                        null,
                                        welcomerConfig?.tellOnRemove ?: false,
                                        "tellOnRemove",
                                        true
                                    ) {
                                        fieldWrappers {
                                            fieldWrapper {
                                                fieldInformationBlock {
                                                    fieldTitle {
                                                        text(i18nContext.get(DashboardI18nKeysData.Welcomer.LeaveSection.ChannelTitle))
                                                    }
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
                                                fieldInformationBlock {
                                                    fieldTitle {
                                                        text(i18nContext.get(DashboardI18nKeysData.Welcomer.LeaveSection.DeleteAfterTitle))
                                                    }
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

                                            discordMessageEditor(
                                                i18nContext,
                                                guild,
                                                { text(i18nContext.get(DashboardI18nKeysData.Welcomer.LeaveSection.MessageEditorTitle)) },
                                                null,
                                                MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='channelRemoveId']"),
                                                leaveTemplates,
                                                leaveMessagePlaceholders,
                                                welcomerConfig?.removeMessage ?: defaultLeaveMessage.content,
                                                "removeMessage"
                                            ) {
                                                attributes["loritta-config"] = "removeMessage"
                                            }

                                            fieldWrapper {
                                                toggleableSection(
                                                    {
                                                        text(i18nContext.get(DashboardI18nKeysData.Welcomer.BanSection.Toggle))
                                                    },
                                                    null,
                                                    welcomerConfig?.tellOnBan ?: false,
                                                    "tellOnBan",
                                                    true
                                                ) {
                                                    fieldWrappers {
                                                        discordMessageEditor(
                                                            i18nContext,
                                                            guild,
                                                            { text(i18nContext.get(DashboardI18nKeysData.Welcomer.BanSection.MessageEditorTitle)) },
                                                            null,
                                                            MessageEditorBootstrap.TestMessageTarget.QuerySelector("[loritta-config='channelRemoveId']"),
                                                            listOf(),
                                                            leaveMessagePlaceholders,
                                                            welcomerConfig?.bannedMessage ?: "",
                                                            "bannedMessage"
                                                        ) {
                                                            attributes["loritta-config"] = "bannedMessage"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    toggleableSection(
                                        {
                                            text(i18nContext.get(DashboardI18nKeysData.Welcomer.PrivateJoinSection.Toggle))
                                        },
                                        {
                                            text(i18nContext.get(DashboardI18nKeysData.Welcomer.PrivateJoinSection.Description))
                                        },
                                        welcomerConfig?.tellOnPrivateJoin ?: false,
                                        "tellOnPrivateJoin",
                                        true
                                    ) {
                                        fieldWrappers {
                                            discordMessageEditor(
                                                i18nContext,
                                                guild,
                                                { text(i18nContext.get(DashboardI18nKeysData.Welcomer.PrivateJoinSection.MessageEditorTitle)) },
                                                null,
                                                MessageEditorBootstrap.TestMessageTarget.SendDirectMessage,
                                                listOf(),
                                                joinMessagePlaceholders,
                                                welcomerConfig?.joinPrivateMessage ?: "",
                                                "joinPrivateMessage"
                                            ) {
                                                attributes["loritta-config"] = "joinPrivateMessage"
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