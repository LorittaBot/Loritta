package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup
import net.perfectdreams.loritta.placeholders.sections.BlueskyPostPlaceholders

fun FlowContent.trackedBlueskyProfileEditor(
    i18nContext: I18nContext,
    guild: Guild,
    channelId: Long?,
    message: String?
) {
    val defaultPostMessage = createMessageTemplate(
        "Padrão",
        "Nova postagem no Bluesky! {post.url}"
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
            listOf(),
            BlueskyPostPlaceholders.placeholders.map {
                when (it) {
                    BlueskyPostPlaceholders.GuildIconUrlPlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            guild.iconUrl ?: "???",
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                    BlueskyPostPlaceholders.GuildNamePlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            guild.name,
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                    BlueskyPostPlaceholders.GuildSizePlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            guild.memberCount.toString(),
                            MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                        )
                    }
                    BlueskyPostPlaceholders.PostUrlPlaceholder -> {
                        createPlaceholderGroup(
                            it,
                            null,
                            "https://bsky.app/profile/loritta.website/post/3l34ux7btja24",
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