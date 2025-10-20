package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.TEXTAREA
import kotlinx.html.textArea
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.discord.DiscordEmoji
import net.perfectdreams.loritta.dashboard.discord.DiscordGuild
import net.perfectdreams.loritta.dashboard.discord.DiscordRole
import net.perfectdreams.loritta.dashboard.discordmessages.RenderableDiscordUser
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorMessagePlaceholder

fun FlowContent.discordMessageEditor(
    guild: Guild,
    target: MessageEditorBootstrap.TestMessageTarget,
    placeholders: List<MessageEditorMessagePlaceholder>,
    message: String,
    block: TEXTAREA.() -> (Unit)
) {
    textArea {
        attributes["bliss-component"] = "discord-message-editor"
        attributes["discord-message-editor-bootstrap"] = BlissHex.encodeToHexString(
            Json.encodeToString(
                MessageEditorBootstrap(
                    RenderableDiscordUser(
                        guild.selfMember.effectiveName,
                        guild.selfMember.effectiveAvatarUrl,
                        guild.selfMember.user.isBot,
                    ),
                    listOf(),
                    placeholders,
                    DiscordGuild(
                        guild.idLong,
                        guild.name,
                        guild.iconId,
                        guild.roles.map {
                            DiscordRole(
                                it.idLong,
                                it.name,
                                it.colorRaw
                            )
                        },
                        listOf(),
                        guild.emojis.map {
                            DiscordEmoji(
                                it.idLong,
                                it.name,
                                it.isAnimated
                            )
                        }
                    ),
                    target
                )
            )
        )

        block()

        text(message)
    }
}