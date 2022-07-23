package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import com.vdurmont.emoji.EmojiManager
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.EmojiCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.utils.DiscordRegexes
import kotlin.streams.toList

class EmojiInfoExecutor(val rest: RestClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val emoji = string("emoji", EmojiCommand.I18N_PREFIX.Info.Options.Emoji)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val emojiContent = args[Options.emoji]

        if (EmojiManager.isEmoji(emojiContent)) {
            context.sendMessage {
                val codePoints = emojiContent.codePoints().toList().map {
                    String.format("\\u%04x", it).substring(2)
                }

                val unicodeEmojiUrl = "https://twemoji.maxcdn.com/2/72x72/${
                    codePoints.joinToString("-")
                }.png"

                embed {
                    title = "$emojiContent " + context.i18nContext.get(
                        EmojiCommand.I18N_PREFIX.Info.AboutEmoji
                    )
                    color = Color(114, 137, 218) // TODO: Move this to an object

                    thumbnailUrl = unicodeEmojiUrl

                    val names = mutableListOf<String>()
                    emojiContent.codePoints().forEach {
                        val name = Character.getName(it)
                        if (name != null)
                            names.add(name)
                    }

                    if (names.isNotEmpty())
                        field {
                            name = "${Emotes.BookMark} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.EmojiName)
                            value = "`${names.joinToString(" + ")}`"

                            inline = true
                        }

                    field {
                        name = "${Emotes.Eyes} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.Mention)
                        value = "`${emojiContent}`"

                        inline = true
                    }

                    field {
                        name = "${Emotes.Computer} Unicode"
                        value = "`${codePoints.joinToString("") { "\\$it" }}`"

                        inline = true
                    }
                }

                actionRow {
                    linkButton(unicodeEmojiUrl) {
                        label = context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.OpenEmojiInBrowser)
                    }
                }
            }

            return
        }

        val discordEmoji = DiscordRegexes.DiscordEmote.find(emojiContent)

        if (discordEmoji != null) {
            // "If the group in the regular expression is optional and there were no match captured by that group,
            // corresponding item in groupValues is an empty string."
            val isAnimated = discordEmoji.groupValues[1].isNotEmpty()
            val emojiName = discordEmoji.groupValues[2]
            val emojiId = discordEmoji.groupValues[3].toLong()
            val emojiIdAsSnowflake = Snowflake(emojiId)
            val emojiMention = buildString {
                append("<")
                if (isAnimated)
                    append("a")
                append(":")
                append(emojiName)
                append(":")
                append(emojiId)
                append(">")
            }

            context.sendMessage {
                val emojiUrl = "https://cdn.discordapp.com/emojis/${discordEmoji.groupValues[3]}.${
                    if (isAnimated) "gif" else "png"
                }?size=2048"

                embed {
                    title = "$emojiMention " + context.i18nContext.get(
                        EmojiCommand.I18N_PREFIX.Info.AboutEmoji
                    )
                    color = Color(114, 137, 218) // TODO: Move this to an object

                    thumbnailUrl = emojiUrl

                    field {
                        name = "${Emotes.BookMark} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.EmojiName)
                        value = emojiName

                        inline = true
                    }

                    field {
                        name = "${Emotes.Computer} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.EmojiId)
                        value = emojiId.toString()

                        inline = true
                    }

                    field {
                        name = "${Emotes.Eyes} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.Mention)
                        value = "`$emojiMention`"

                        inline = true
                    }

                    field {
                        name = "${Emotes.Date} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.CreatedAt)
                        value = "<t:${emojiIdAsSnowflake.timestamp.toEpochMilliseconds() / 1000}:D>"

                        inline = true
                    }
                }

                actionRow {
                    linkButton(emojiUrl) {
                        label = context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.OpenEmojiInBrowser)
                    }
                }
            }

            return
        }

        context.failEphemerally {
            styled(
                context.i18nContext.get(
                    EmojiCommand.I18N_PREFIX.Info.EmojiNotFound(emojiContent.shortenAndStripCodeBackticks(100))
                ),
                Emotes.Error
            )
        }
    }
}