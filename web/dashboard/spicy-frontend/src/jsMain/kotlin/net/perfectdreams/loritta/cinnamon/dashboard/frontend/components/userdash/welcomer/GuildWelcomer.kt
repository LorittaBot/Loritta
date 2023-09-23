package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.welcomer

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ConfigureGuildWelcomerScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Toast
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.WelcomerViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.common.utils.embeds.DiscordComponent
import net.perfectdreams.loritta.common.utils.embeds.DiscordEmbed
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.dom.*

@Composable
fun GuildWelcomer(
    m: LorittaDashboardFrontend,
    screen: ConfigureGuildWelcomerScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val userInfo = LocalUserIdentification.current
    val configViewModel = viewModel { WelcomerViewModel(m, it, guildViewModel) }

    ResourceChecker(
        i18nContext,
        guildViewModel.guildInfoResource,
        configViewModel.configResource
    ) { guild, welcomerResponse ->
        val defaultJoinTemplate = LorittaMessageTemplate.LorittaRawMessageTemplate(
            "Padrão",
            "\uD83D\uDC49 {@user} entrou no servidor!"
        )
        val defaultRemoveTemplate = LorittaMessageTemplate.LorittaRawMessageTemplate(
            "Padrão",
            "\uD83D\uDC48 {user} saiu do servidor..."
        )
        val welcomerConfig = welcomerResponse.welcomerConfig ?: GuildWelcomerConfig(
            false,
            null,
            defaultJoinTemplate.content,
            null,

            false,
            null,
            defaultJoinTemplate.content,
            null,

            false,
            null,

            false,
            null
        )

        HeroBanner {
            HeroImage {
                WelcomerWebAnimation()
            }

            HeroText {
                H1 {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.Welcomer.Title))
                }

                P {
                    Text("Anuncie quem está entrando e saindo do seu servidor da maneira que você queria! Envie mensagens para novatos via mensagem direta com informações sobre o seu servidor para não encher o chat com informações repetidas e muito mais!")
                }
            }
        }

        Hr {}

        var mutableWelcomerConfig by remember { mutableStateOf(WelcomerViewModel.toMutableConfig(welcomerConfig)) }

        // The initial config state
        var startConfigState by remember { mutableStateOf(WelcomerViewModel.toDataConfig(mutableWelcomerConfig)) }

        ToggleableSections {
            ToggleableSection(
                "tell-on-join-section",
                "Ativar as mensagens quando alguém entrar",
                null,
                mutableWelcomerConfig._tellOnJoin,
            ) {
                FieldWrappers {
                    FieldWrapper {
                        FieldLabel("Canal onde será enviado as mensagens")

                        DiscordChannelSelectMenu(
                            m,
                            i18nContext,
                            guild.channels,
                            mutableWelcomerConfig.channelJoinId
                        ) {
                            mutableWelcomerConfig.channelJoinId = it.id
                        }
                    }

                    FieldWrapper {
                        FieldLabel("Segundos para deletar a mensagem (Deixe em 0 para nunca deletar)")

                        NumberInput(mutableWelcomerConfig.deleteJoinMessagesAfter) {
                            onInput {
                                mutableWelcomerConfig.deleteJoinMessagesAfter = it.value?.toLong()
                            }
                            min("0")
                            max("60")
                            value(mutableWelcomerConfig.deleteJoinMessagesAfter ?: 0)
                        }
                    }

                    FieldWrapper {
                        FieldLabel("Mensagem quando alguém entrar")

                        val templates = listOf(
                            defaultJoinTemplate,
                            LorittaMessageTemplate.LorittaRawMessageTemplate(
                                "Padrão, só que melhor",
                                "<a:lori_happy:521721811298156558> | {@user} acabou de entrar na {guild}! Agora temos {guild-size} membros!"
                            ),
                            LorittaMessageTemplate.LorittaRawMessageTemplate(
                                "Lista de Informações",
                                """{@user}, bem-vindo(a) ao {guild}! <a:lori_happy:521721811298156558>
• Leia as #regras *(psiu, troque o nome do canal aqui na mensagem!)* para você poder conviver em harmonia! <:blobheart:467447056374693889>
• Converse no canal de #bate-papo <:lori_pac:503600573741006863>
• E é claro, divirta-se! <a:emojo_feriado:393751205308006421>

Aliás, continue sendo incrível! (E eu sou muito fofa! :3)"""
                            ),
                            LorittaMessageTemplate.LorittaDiscordMessageTemplate(
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
                            LorittaMessageTemplate.LorittaDiscordMessageTemplate(
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
                            LorittaMessageTemplate.LorittaDiscordMessageTemplate(
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
                            LorittaMessageTemplate.LorittaDiscordMessageTemplate(
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
                                                "Atualmente temos {guild-size} membros no servidor?",
                                                true
                                            ),
                                            DiscordEmbed.Field(
                                                "🛡 Tag do Usuário",
                                                "`{user.tag}` ({user-id})",
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
                            LorittaMessageTemplate.LorittaDiscordMessageTemplate(
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
                                                    label = "Twitter",
                                                    style = 5,
                                                    url = "https://twitter.com/LorittaBot"
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

                        DiscordMessageEditor(
                            m,
                            i18nContext,
                            templates,
                            PlaceholderSectionType.JOIN_MESSAGE,
                            guild,
                            mutableWelcomerConfig.channelJoinId?.let {
                                TargetChannelResult.GuildMessageChannelTarget(
                                    it
                                )
                            }
                                ?: TargetChannelResult.ChannelNotSelected,
                            userInfo,
                            welcomerResponse.selfUser,
                            listOf(),
                            listOf(),
                            mutableWelcomerConfig.joinMessage
                                ?: ""
                        ) {
                            mutableWelcomerConfig.joinMessage = it
                        }
                    }
                }
            }

            ToggleableSection(
                "tell-on-leave-section",
                "Ativar as mensagens quando alguém sair",
                null,
                mutableWelcomerConfig._tellOnRemove,
            ) {
                FieldWrappers {
                    FieldWrapper {
                        FieldLabel("Canal onde será enviado as mensagens")

                        DiscordChannelSelectMenu(
                            m,
                            i18nContext,
                            guild.channels,
                            mutableWelcomerConfig.channelRemoveId
                        ) {
                            mutableWelcomerConfig.channelRemoveId = it.id
                        }
                    }

                    FieldWrapper {
                        FieldLabel("Segundos para deletar a mensagem (Deixe em 0 para nunca deletar)")

                        NumberInput(mutableWelcomerConfig.deleteRemoveMessagesAfter) {
                            onInput {
                                mutableWelcomerConfig.deleteRemoveMessagesAfter = it.value?.toLong()
                            }
                            min("0")
                            max("60")
                            value(mutableWelcomerConfig.deleteRemoveMessagesAfter ?: 0)
                        }
                    }

                    FieldWrapper {
                        FieldLabel("Mensagem quando alguém sair")

                        val templates = listOf(
                            defaultRemoveTemplate,
                            LorittaMessageTemplate.LorittaRawMessageTemplate(
                                "Padrão, só que melhor",
                                "<a:bongo_lori_triste:524894216510373888> | {user} saiu do {guild}... espero que algum dia ele volte...",
                            ),
                            LorittaMessageTemplate.LorittaDiscordMessageTemplate(
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
                            LorittaMessageTemplate.LorittaDiscordMessageTemplate(
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
                            LorittaMessageTemplate.LorittaDiscordMessageTemplate(
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

                        DiscordMessageEditor(
                            m,
                            i18nContext,
                            templates,
                            PlaceholderSectionType.LEAVE_MESSAGE,
                            guild,
                            mutableWelcomerConfig.channelRemoveId?.let {
                                TargetChannelResult.GuildMessageChannelTarget(
                                    it
                                )
                            }
                                ?: TargetChannelResult.ChannelNotSelected,
                            userInfo,
                            welcomerResponse.selfUser,
                            listOf(),
                            listOf(),
                            mutableWelcomerConfig.removeMessage
                                ?: ""
                        ) {
                            mutableWelcomerConfig.removeMessage =
                                it
                        }
                    }

                    Div(attrs = {
                        attr("style", "margin-top: 2em;")
                    }) {
                        ToggleableSection(
                            "tell-on-ban-section",
                            "Mostrar mensagem diferenciada ao ser banido",
                            null,
                            mutableWelcomerConfig._tellOnBan,
                        ) {
                            FieldWrappers {
                                FieldWrapper {
                                    FieldLabel("Mensagem quando alguém for banido")

                                    DiscordMessageEditor(
                                        m,
                                        i18nContext,
                                        null,
                                        PlaceholderSectionType.LEAVE_MESSAGE,
                                        guild,
                                        mutableWelcomerConfig.channelRemoveId?.let {
                                            TargetChannelResult.GuildMessageChannelTarget(
                                                it
                                            )
                                        }
                                            ?: TargetChannelResult.ChannelNotSelected,
                                        userInfo,
                                        welcomerResponse.selfUser,
                                        listOf(),
                                        listOf(),
                                        mutableWelcomerConfig.bannedMessage
                                            ?: ""
                                    ) {
                                        mutableWelcomerConfig.bannedMessage =
                                            it
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ToggleableSection(
                "tell-on-dm",
                "Ativar as mensagens enviadas nas mensagens diretas do usuário quando alguém entrar",
                "Útil caso você queria mostrar informações básicas sobre o servidor para um usuário mas não quer que fique cheio de mensagens inúteis toda hora que alguém entra.",
                mutableWelcomerConfig._tellOnPrivateJoin,
            ) {
                FieldWrappers {
                    FieldWrapper {
                        FieldLabel("Mensagem quando alguém entrar (via mensagem direta)")

                        DiscordMessageEditor(
                            m,
                            i18nContext,
                            null,
                            PlaceholderSectionType.JOIN_MESSAGE,
                            guild,
                            TargetChannelResult.DirectMessageTarget,
                            userInfo,
                            welcomerResponse.selfUser,
                            listOf(),
                            listOf(),
                            mutableWelcomerConfig.joinPrivateMessage
                                ?: ""
                        ) {
                            mutableWelcomerConfig.joinPrivateMessage =
                                it
                        }
                    }
                }
            }
        }

        Hr {}

        var isSaving by remember { mutableStateOf(false) }

        SaveBar(
            m,
            i18nContext,
            startConfigState != WelcomerViewModel.toDataConfig(mutableWelcomerConfig),
            isSaving,
            onReset = {
                mutableWelcomerConfig = WelcomerViewModel.toMutableConfig(startConfigState)
            },
            onSave = {
                GlobalScope.launch {
                    isSaving = true

                    m.globalState.showToast(Toast.Type.INFO, "Salvando configuração...")
                    val config = WelcomerViewModel.toDataConfig(mutableWelcomerConfig)
                    val dashResponse =
                        m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.UpdateGuildWelcomerConfigResponse>(
                            guild.id,
                            DashGuildScopedRequest.UpdateGuildWelcomerConfigRequest(config),
                            onSuccess = {
                                m.globalState.showToast(Toast.Type.SUCCESS, "Configuração salva!")
                                m.soundEffects.configSaved.play(1.0)
                                isSaving = false
                                startConfigState = config
                                m.globalState.activeSaveBar = false
                            },
                            onError = {
                                m.soundEffects.configError.play(1.0)
                                isSaving = false
                                m.globalState.activeSaveBar = false
                            }
                        )
                }
            }
        )
    }
}