package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord

import com.vdurmont.emoji.EmojiManager
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.EmojiCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordRegexes
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.common.utils.LorittaColors
import kotlin.streams.toList

class EmojiInfoExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val emoji = string("emoji", EmojiCommand.I18N_PREFIX.Info.Options.Emoji)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val emojiContent = args[options.emoji]

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
                    color = LorittaColors.DiscordBlurple.toKordColor()

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
                    color = LorittaColors.DiscordBlurple.toKordColor()

                    thumbnailUrl = emojiUrl

                    field {
                        name = "${Emotes.BookMark} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.EmojiName)
                        value = emojiName

                        inline = true
                    }

                    field {
                        name = "${Emotes.LoriId} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.EmojiId)
                        value = emojiId.toString()

                        inline = true
                    }

                    field {
                        name = "${Emotes.Eyes} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.Mention)
                        value = "`$emojiMention`"

                        inline = true
                    }

                    field {
                        name = "${Emotes.LoriCalendar} " + context.i18nContext.get(EmojiCommand.I18N_PREFIX.Info.CreatedAt)
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