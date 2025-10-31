package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup
import net.perfectdreams.loritta.placeholders.sections.TwitchStreamOnlinePlaceholders
import net.perfectdreams.switchtwitch.data.TwitchUser

fun FlowContent.trackedTwitchChannelEditor(
    i18nContext: I18nContext,
    guild: Guild,
    twitchUser: TwitchUser,
    channelId: Long?,
    message: String?
) {
    val defaultPostMessage = createMessageTemplate(
        "Padrão",
        "Venha assistir a minha live na Twitch! {stream.url}"
    )

    fieldWrappers {
        fieldWrapper {
            fieldInformationBlock {
                fieldTitle {
                    text("Canal onde será enviado as mensagens")
                }
            }

            channelSelectMenu(
                guild,
                channelId
            ) {
                attributes["loritta-config"] = "channelId"
                name = "channelId"
            }
        }

        discordMessageEditor(
            i18nContext,
            guild,
            { text("Mensagem") },
            null,
            MessageEditorBootstrap.TestMessageTarget.QuerySelector("[name='channelId']"),
            listOf(defaultPostMessage),
            TwitchStreamOnlinePlaceholders.placeholders.map {
                when (it) {
                    TwitchStreamOnlinePlaceholders.GuildIconUrlPlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            guild.iconUrl ?: "???",
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                    TwitchStreamOnlinePlaceholders.GuildNamePlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            guild.name,
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                    TwitchStreamOnlinePlaceholders.GuildSizePlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            guild.memberCount.toString(),
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                    TwitchStreamOnlinePlaceholders.StreamGamePlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            "Just Chatting",
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                    TwitchStreamOnlinePlaceholders.StreamTitlePlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            "Configurando a Loritta!",
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                    TwitchStreamOnlinePlaceholders.StreamUrlPlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            "https://twitch.tv/${twitchUser.login}",
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                }
            },
            message ?: defaultPostMessage.content,
            "message"
        ) {
            this.attributes["loritta-config"] = "message"
        }
    }
}