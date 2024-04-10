package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.*
import kotlin.streams.toList

class EmojiCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Emoji

        fun createDiscordCustomEmojiInfoEmbed(context: UnleashedContext, emoji: CustomEmoji) = Embed {
            val cachedEmoji = context.loritta.lorittaShards.getEmoteById(emoji.id)
            val canUse = cachedEmoji != null
            val emojiCreatedAt = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(emoji.timeCreated)

            val emojiTitle = if (canUse)
                emoji.asMention
            else
                "✨"

            color = Constants.DISCORD_BLURPLE.rgb
            title = "$emojiTitle ${context.i18nContext.get(I18N_PREFIX.Info.AboutEmoji)}"
            thumbnail = emoji.imageUrl
            field {
                name = "${Emotes.BookMark} ${context.i18nContext.get(I18N_PREFIX.Info.EmojiName)}"
                value = "`${emoji.name}`"
                inline = true
            }
            field {
                name = "${Emotes.Computer} ${context.i18nContext.get(I18N_PREFIX.Info.EmojiId)}"
                value = "`${emoji.id}`"
                inline = true
            }
            field {
                name = "${Emotes.Eyes} ${context.i18nContext.get(I18N_PREFIX.Info.Mention)}"
                value = "`${emoji.asMention}`"
                inline = true
            }
            field {
                name = "${Emotes.Date} ${context.i18nContext.get(I18N_PREFIX.Info.CreatedAt)}"
                value = emojiCreatedAt
                inline = true
            }
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.DISCORD) {
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Info.Label, I18N_PREFIX.Info.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("emojiinfo")
                add("emoji")
            }

            executor = EmojiInfoExecutor()
        }
    }

    inner class EmojiInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val emoji = string("emoji", I18N_PREFIX.Info.Options.Emoji)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val emoji = args[options.emoji]

            if (emoji.isValidSnowflake()) {
                val searchedEmoji = context.loritta.lorittaShards.getEmoteById(emoji)

                if (searchedEmoji != null) {
                    context.reply(false) {
                        embeds.plusAssign(createDiscordCustomEmojiInfoEmbed(context, searchedEmoji))

                        actionRow(
                            Button.link(
                                searchedEmoji.imageUrl + "?size=2048",
                                context.i18nContext.get(I18N_PREFIX.Info.OpenEmojiInBrowser)
                            )
                        )
                    }
                } else {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Info.EmojiNotFound(emoji)),
                            Emotes.LoriHm
                        )
                    }
                    return
                }

                val foundEmoji = context.guild.getEmojisByName(emoji, true).firstOrNull()
                if (foundEmoji != null) {
                    context.reply(false) {
                        embeds.plusAssign(createDiscordCustomEmojiInfoEmbed(context, foundEmoji))

                        actionRow(
                            Button.link(
                                foundEmoji.imageUrl + "?size=2048",
                                context.i18nContext.get(I18N_PREFIX.Info.OpenEmojiInBrowser)
                            )
                        )
                    }
                    return
                }
            } else {
                val isUnicodeEmoji = Constants.EMOJI_PATTERN.matcher(emoji).find()

                if (isUnicodeEmoji) {
                    val codePoints = emoji.codePoints().toList().map { LorittaUtils.toUnicode(it).substring(2) }

                    val result = codePoints.joinToString(separator = "-")
                    val emojiUrl = "https://abs.twimg.com/emoji/v2/72x72/$result.png"

                    val names = mutableListOf<String>()
                    emoji.codePoints().forEach {
                        val name = Character.getName(it)
                        if (name != null)
                            names.add(name)
                    }

                    context.reply(false) {
                        embed {
                            color = Constants.DISCORD_BLURPLE.rgb
                            title = "$emoji ${context.i18nContext.get(I18N_PREFIX.Info.AboutEmoji)}"
                            thumbnail = emojiUrl

                            if (names.isNotEmpty()) field {
                                name = "${Emotes.BookMark} ${context.i18nContext.get(I18N_PREFIX.Info.EmojiName)}"
                                value = "`${names.joinToString(" + ")}`"
                                inline = true
                            }

                            field {
                                name = "${Emotes.Eyes} ${context.i18nContext.get(I18N_PREFIX.Info.Mention)}"
                                value = "`$emoji`"
                                inline = true
                            }
                            field {
                                name = "${Emotes.Computer} Unicode"
                                value = "`${codePoints.joinToString("") { "\\$it" }}`"
                                inline = true
                            }
                        }

                        actionRow(
                            Button.link(
                                emojiUrl,
                                context.i18nContext.get(I18N_PREFIX.Info.OpenEmojiInBrowser)
                            )
                        )
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val arg0 = args.getOrNull(0)
            val firstEmote = context.mentions.customEmojis.firstOrNull()

            when (arg0) {
                null -> {
                    context.explain()
                    return null
                }
                firstEmote?.asMention -> return mapOf(options.emoji to firstEmote.id)
                else -> {
                    val emojiByName = context.guild.getEmojisByName(arg0, true).firstOrNull()

                    return if (emojiByName != null) {
                        mapOf(options.emoji to emojiByName.id)
                    } else {
                        mapOf(options.emoji to arg0)
                    }
                }
            }
        }
    }
}