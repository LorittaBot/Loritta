package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.embeds.DiscordComponent
import net.perfectdreams.loritta.common.utils.embeds.DiscordEmbed
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.JoinMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.LeaveMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.extensions.getIconUrl
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor.lorittaDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardSaveBar.lorittaSaveBar
import net.perfectdreams.loritta.morenitta.website.components.DiscordChannelSelectMenu.discordChannelSelectMenu
import net.perfectdreams.loritta.morenitta.website.components.DiscordLikeToggles.toggleableSection
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class  GuildWelcomerView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    selectedType: String,
    private val guildWelcomerConfig: GuildWelcomerConfig
) : GuildDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    guild,
    selectedType
) {
    companion object {
        val defaultJoinTemplate = DashboardDiscordMessageEditor.createMessageTemplate(
            "Padrão",
            "\uD83D\uDC49 {@user} entrou no servidor!"
        )
        val defaultRemoveTemplate = DashboardDiscordMessageEditor.createMessageTemplate(
            "Padrão",
            "\uD83D\uDC48 {user.name} saiu do servidor..."
        )

        private val joinTemplates = listOf(
            defaultJoinTemplate,
            DashboardDiscordMessageEditor.createMessageTemplate(
                "Padrão, só que melhor",
                "<a:lori_happy:521721811298156558> | {@user} acabou de entrar na {guild}! Agora temos {guild.size} membros!"
            ),
            DashboardDiscordMessageEditor.createMessageTemplate(
                "Lista de Informações",
                """{@user}, bem-vindo(a) ao {guild}! <a:lori_happy:521721811298156558>
• Leia as #regras *(psiu, troque o nome do canal aqui na mensagem!)* para você poder conviver em harmonia! <:blobheart:467447056374693889>
• Converse no canal de #bate-papo <:lori_pac:503600573741006863>
• E é claro, divirta-se! <a:emojo_feriado:393751205308006421>

Aliás, continue sendo incrível! (E eu sou muito fofa! :3)"""
            ),
            DashboardDiscordMessageEditor.createMessageTemplate(
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
            DashboardDiscordMessageEditor.createMessageTemplate(
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
            DashboardDiscordMessageEditor.createMessageTemplate(
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
            DashboardDiscordMessageEditor.createMessageTemplate(
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
            DashboardDiscordMessageEditor.createMessageTemplate(
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

        private val removeTemplates = listOf(
            defaultRemoveTemplate,
            DashboardDiscordMessageEditor.createMessageTemplate(
                "Padrão, só que melhor",
                "<a:bongo_lori_triste:524894216510373888> | {user} saiu do {guild}... espero que algum dia ele volte...",
            ),
            DashboardDiscordMessageEditor.createMessageTemplate(
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
            DashboardDiscordMessageEditor.createMessageTemplate(
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
            DashboardDiscordMessageEditor.createMessageTemplate(
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
    }

    override fun DIV.generateRightSidebarContents() {
        val serializableGuild = WebsiteUtils.convertJDAGuildToSerializable(guild)
        val serializableSelfLorittaUser = WebsiteUtils.convertJDAUserToSerializable(guild.selfMember.user)

        div {
            div {
                id = "form-stuff-wrapper"

                div(classes = "hero-wrapper") {
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

                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Welcomer.Title))
                        }

                        p {
                            text("Anuncie quem está entrando e saindo do seu servidor da maneira que você queria! Envie mensagens para novatos via mensagem direta com informações sobre o seu servidor para não encher o chat com informações repetidas e muito mais!")
                        }
                    }
                }

                hr {}

                div {
                    id = "module-config-wrapper"
                    form {
                        id = "module-config"
                        attributes["loritta-synchronize-with-save-bar"] = "#save-bar"

                        div(classes = "toggleable-sections") {
                            toggleableSection(
                                "tellOnJoin",
                                "Ativar as mensagens quando alguém entrar",
                                null,
                                guildWelcomerConfig.tellOnJoin
                            ) {
                                div(classes = "field-wrappers") {
                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text("Canal onde será enviado as mensagens")
                                        }

                                        discordChannelSelectMenu(
                                            lorittaWebsite,
                                            i18nContext,
                                            "channelJoinId",
                                            guild.channels.filterIsInstance<GuildMessageChannel>(),
                                            guildWelcomerConfig.channelJoinId,
                                            null
                                        )
                                    }

                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text("Segundos para deletar a mensagem (Deixe em 0 para nunca deletar)")
                                        }

                                        numberInput {
                                            name = "deleteJoinMessagesAfter"
                                            min = "0"
                                            max = "60"
                                            value = guildWelcomerConfig.deleteJoinMessagesAfter?.toString() ?: "0"
                                        }
                                    }

                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text("Mensagem quando alguém entrar")
                                        }

                                        lorittaDiscordMessageEditor(
                                            i18nContext,
                                            "joinMessage",
                                            joinTemplates,
                                            PlaceholderSectionType.JOIN_MESSAGE,
                                            JoinMessagePlaceholders.placeholders.flatMap {
                                                when (it) {
                                                    JoinMessagePlaceholders.UserMentionPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        "<@${userIdentification.id}>",
                                                        "@${userIdentification.globalName ?: userIdentification.username}"
                                                    )

                                                    JoinMessagePlaceholders.UserNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.globalName ?: userIdentification.username
                                                    )

                                                    JoinMessagePlaceholders.UserDiscriminatorPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.discriminator
                                                    )

                                                    JoinMessagePlaceholders.UserTagPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        "@${userIdentification.username}"
                                                    )

                                                    JoinMessagePlaceholders.UserIdPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.id
                                                    )

                                                    JoinMessagePlaceholders.UserAvatarUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.effectiveAvatarUrl
                                                    )

                                                    JoinMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.name
                                                    )

                                                    JoinMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.memberCount.toString()
                                                    )

                                                    JoinMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.getIconUrl(512, ImageFormat.PNG) ?: ""
                                                    ) // TODO: Fix this!
                                                }
                                            },
                                            serializableGuild,
                                            serializableSelfLorittaUser,
                                            TestMessageTargetChannelQuery.QuerySelector("[name='channelJoinId']"),
                                            guildWelcomerConfig.joinMessage ?: ""
                                        )
                                    }
                                }
                            }

                            toggleableSection(
                                "tellOnRemove",
                                "Ativar as mensagens quando alguém sair",
                                null,
                                guildWelcomerConfig.tellOnRemove
                            ) {
                                div(classes = "field-wrappers") {
                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text("Canal onde será enviado as mensagens")
                                        }

                                        discordChannelSelectMenu(
                                            lorittaWebsite,
                                            i18nContext,
                                            "channelRemoveId",
                                            guild.channels.filterIsInstance<GuildMessageChannel>(),
                                            guildWelcomerConfig.channelRemoveId,
                                            null
                                        )
                                    }

                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text("Segundos para deletar a mensagem (Deixe em 0 para nunca deletar)")
                                        }

                                        numberInput {
                                            name = "deleteRemoveMessagesAfter"
                                            min = "0"
                                            max = "60"
                                            value = guildWelcomerConfig.deleteRemoveMessagesAfter?.toString() ?: "0"
                                        }
                                    }

                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text("Mensagem quando alguém sair")
                                        }

                                        lorittaDiscordMessageEditor(
                                            i18nContext,
                                            "removeMessage",
                                            removeTemplates,
                                            PlaceholderSectionType.LEAVE_MESSAGE,
                                            LeaveMessagePlaceholders.placeholders.flatMap {
                                                when (it) {
                                                    LeaveMessagePlaceholders.UserMentionPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        "<@${userIdentification.id}>",
                                                        "@${userIdentification.globalName ?: userIdentification.username}"
                                                    )

                                                    LeaveMessagePlaceholders.UserNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.globalName ?: userIdentification.username
                                                    )

                                                    LeaveMessagePlaceholders.UserDiscriminatorPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.discriminator
                                                    )

                                                    LeaveMessagePlaceholders.UserTagPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        "@${userIdentification.username}"
                                                    )

                                                    LeaveMessagePlaceholders.UserIdPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.id
                                                    )

                                                    LeaveMessagePlaceholders.UserAvatarUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.effectiveAvatarUrl
                                                    )

                                                    LeaveMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.name
                                                    )

                                                    LeaveMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.memberCount.toString()
                                                    )

                                                    LeaveMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.getIconUrl(512, ImageFormat.PNG) ?: ""
                                                    ) // TODO: Fix this!
                                                }
                                            },
                                            serializableGuild,
                                            serializableSelfLorittaUser,
                                            TestMessageTargetChannelQuery.QuerySelector("[name='channelRemoveId']"),
                                            guildWelcomerConfig.removeMessage ?: ""
                                        )
                                    }

                                    toggleableSection(
                                        "tellOnBan",
                                        "Mostrar mensagem diferenciada ao ser banido",
                                        null,
                                        guildWelcomerConfig.tellOnBan
                                    ) {
                                        div(classes = "field-wrapper") {
                                            div(classes = "field-title") {
                                                text("Mensagem quando alguém entrar")
                                            }

                                            lorittaDiscordMessageEditor(
                                                i18nContext,
                                                "bannedMessage",
                                                listOf(),
                                                PlaceholderSectionType.LEAVE_MESSAGE,
                                                LeaveMessagePlaceholders.placeholders.flatMap {
                                                    when (it) {
                                                        LeaveMessagePlaceholders.UserMentionPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            "<@${userIdentification.id}>",
                                                            "@${userIdentification.globalName ?: userIdentification.username}"
                                                        )

                                                        LeaveMessagePlaceholders.UserNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            userIdentification.globalName ?: userIdentification.username
                                                        )

                                                        LeaveMessagePlaceholders.UserDiscriminatorPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            userIdentification.discriminator
                                                        )

                                                        LeaveMessagePlaceholders.UserTagPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            "@${userIdentification.username}"
                                                        )

                                                        LeaveMessagePlaceholders.UserIdPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            userIdentification.id
                                                        )

                                                        LeaveMessagePlaceholders.UserAvatarUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            userIdentification.effectiveAvatarUrl
                                                        )

                                                        LeaveMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            guild.name
                                                        )

                                                        LeaveMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            guild.memberCount.toString()
                                                        )

                                                        LeaveMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                            it,
                                                            guild.getIconUrl(512, ImageFormat.PNG) ?: ""
                                                        ) // TODO: Fix this!
                                                    }
                                                },
                                                serializableGuild,
                                                serializableSelfLorittaUser,
                                                TestMessageTargetChannelQuery.QuerySelector("[name='channelRemoveId']"),
                                                guildWelcomerConfig.bannedMessage ?: ""
                                            )
                                        }
                                    }
                                }
                            }

                            toggleableSection(
                                "tellOnPrivateJoin",
                                "Ativar as mensagens enviadas nas mensagens diretas do usuário quando alguém entrar",
                                "Útil caso você queria mostrar informações básicas sobre o servidor para um usuário mas não quer que fique cheio de mensagens inúteis toda hora que alguém entra.",
                                guildWelcomerConfig.tellOnPrivateJoin
                            ) {
                                div(classes = "field-wrappers") {
                                    div(classes = "field-wrapper") {
                                        div(classes = "field-title") {
                                            text("Mensagem quando alguém entrar (via mensagem direta)")
                                        }

                                        lorittaDiscordMessageEditor(
                                            i18nContext,
                                            "joinPrivateMessage",
                                            listOf(),
                                            PlaceholderSectionType.JOIN_MESSAGE,
                                            JoinMessagePlaceholders.placeholders.flatMap {
                                                when (it) {
                                                    JoinMessagePlaceholders.UserMentionPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        "<@${userIdentification.id}>",
                                                        "@${userIdentification.globalName ?: userIdentification.username}"
                                                    )

                                                    JoinMessagePlaceholders.UserNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.globalName ?: userIdentification.username
                                                    )

                                                    JoinMessagePlaceholders.UserDiscriminatorPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.discriminator
                                                    )

                                                    JoinMessagePlaceholders.UserTagPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        "@${userIdentification.username}"
                                                    )

                                                    JoinMessagePlaceholders.UserIdPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.id
                                                    )

                                                    JoinMessagePlaceholders.UserAvatarUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        userIdentification.effectiveAvatarUrl
                                                    )

                                                    JoinMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                                        it,
                                                        guild.name
                                                    )

                                                    JoinMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(it, guild.memberCount.toString())
                                                    JoinMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(it, guild.getIconUrl(512, ImageFormat.PNG) ?: "") // TODO: Fix this!
                                                }
                                            },
                                            serializableGuild,
                                            serializableSelfLorittaUser,
                                            TestMessageTargetChannelQuery.SendDirectMessage,
                                            guildWelcomerConfig.joinPrivateMessage ?: ""
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                hr {}

                lorittaSaveBar(
                    i18nContext,
                    false,
                    {}
                ) {
                    attributes["hx-patch"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/welcomer"
                }
            }
        }
    }
}