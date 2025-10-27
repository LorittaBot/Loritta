package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.placeholders.YouTubePostMessagePlaceholders
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholderGroup
import net.perfectdreams.loritta.placeholders.sections.YouTubePostPlaceholders
import net.perfectdreams.loritta.placeholders.toNewPlaceholderSystem

fun FlowContent.trackedYouTubeChannelEditor(
    i18nContext: I18nContext,
    guild: Guild,
    channelId: Long?,
    message: String?
) {
    val defaultYouTubeMessage = createMessageTemplate(
        "Padrão",
        "Novo vídeo no canal! {video.url}"
    )

    fieldWrappers {
        fieldWrapper {
            fieldTitle {
                text("Canal onde será enviado as mensagens")
            }

            channelSelectMenu(
                guild,
                channelId
            ) {
                attributes["loritta-config"] = "channelId"
                name = "channelId"
            }
        }

        val genericVideoId = "jhkGNIlNqXw"

        fieldWrapper {
            discordMessageEditor(
                guild,
                MessageEditorBootstrap.TestMessageTarget.QuerySelector("[name='channelId']"),
                listOf(defaultYouTubeMessage),
                YouTubePostPlaceholders.placeholders.map {
                    when (it) {
                        YouTubePostPlaceholders.VideoTitlePlaceholder -> {
                            createPlaceholderGroup(
                                it.placeholders,
                                null,
                                "paffendorf",
                                MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                            )
                        }
                        YouTubePostPlaceholders.GuildIconUrlPlaceholder -> {
                            createPlaceholderGroup(
                                it.placeholders,
                                null,
                                guild.iconUrl ?: "???",
                                MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                            )
                        }
                        YouTubePostPlaceholders.GuildSizePlaceholder -> {
                            createPlaceholderGroup(
                                it.placeholders,
                                null,
                                guild.memberCount.toString(),
                                MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                            )
                        }
                        YouTubePostPlaceholders.VideoUrlPlaceholder -> {
                            createPlaceholderGroup(
                                it.placeholders,
                                null,
                                "https://youtu.be/$genericVideoId",
                                MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                            )
                        }
                        YouTubePostPlaceholders.GuildNamePlaceholder -> {
                            createPlaceholderGroup(
                                it.placeholders,
                                null,
                                guild.name,
                                MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                            )
                        }
                        YouTubePostPlaceholders.VideoIdPlaceholder -> {
                            createPlaceholderGroup(
                                it.placeholders,
                                null,
                                genericVideoId,
                                MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                            )
                        }
                        YouTubePostPlaceholders.VideoThumbnailPlaceholder -> {
                            createPlaceholderGroup(
                                it.placeholders,
                                null,
                                "https://img.youtube.com/vi/$genericVideoId/maxresdefault.jpg",
                                MessageEditorMessagePlaceholderGroup.RenderType.TEXT
                            )
                        }
                    }
                },
                message ?: defaultYouTubeMessage.content
            ) {
                this.name = "message"
                this.attributes["loritta-config"] = "message"
            }
        }
    }
}