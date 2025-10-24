package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.style
import kotlinx.html.textInput
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.placeholders.TwitchStreamOnlineMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.YouTubePostMessagePlaceholders
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholder

fun FlowContent.trackedTwitchChannelEditor(
    i18nContext: I18nContext,
    guild: Guild,
    channelId: Long?,
    message: String
) {
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

        fieldWrapper {
            discordMessageEditor(
                guild,
                MessageEditorBootstrap.TestMessageTarget.QuerySelector("[name='channelId']"),
                TwitchStreamOnlineMessagePlaceholders.placeholders.map {
                    when (it) {
                        TwitchStreamOnlineMessagePlaceholders.GuildIconUrlPlaceholder -> listOf()
                        TwitchStreamOnlineMessagePlaceholders.GuildNamePlaceholder -> it.names.map {
                            MessageEditorMessagePlaceholder(
                                it.placeholder.name,
                                guild.name,
                                guild.name,
                                MessageEditorMessagePlaceholder.RenderType.TEXT
                            )
                        }
                        TwitchStreamOnlineMessagePlaceholders.GuildSizePlaceholder -> listOf()
                        TwitchStreamOnlineMessagePlaceholders.StreamGamePlaceholder -> listOf()
                        TwitchStreamOnlineMessagePlaceholders.StreamTitlePlaceholder -> listOf()
                        TwitchStreamOnlineMessagePlaceholders.StreamUrlPlaceholder -> listOf()
                    }
                }.flatten(),
                message
            ) {
                this.name = "message"
                this.attributes["loritta-config"] = "message"
            }
        }
    }
}