package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.style
import kotlinx.html.textInput
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap

fun FlowContent.trackedBlueskyProfileEditor(
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
        }

        fieldWrapper {
            discordMessageEditor(
                guild,
                MessageEditorBootstrap.TestMessageTarget.QuerySelector("[name='channelId']"),
                listOf(),
                message
            ) {
                this.name = "message"
                this.attributes["loritta-config"] = "message"
            }
        }
    }
}