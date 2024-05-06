package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
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
import java.time.OffsetDateTime
import kotlin.streams.toList

class EmojiCommand : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Emoji
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
            val emojiResolvable = args[options.emoji]
            val firstEmoji = context.mentions.customEmojis.firstOrNull()

            if (emojiResolvable == firstEmoji?.asMention) {
                sendEmojiMessage(context, firstEmoji)
                return
            } else if (emojiResolvable.isValidSnowflake()) {
                val emoji = context.loritta.lorittaShards.getEmoteById(emojiResolvable)

                if (emoji != null) {
                    sendEmojiMessage(context, emoji)
                } else {
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Info.EmojiNotFound(emojiResolvable))
                        )
                    }
                }
            } else if (Constants.EMOJI_PATTERN.matcher(emojiResolvable).find()) {
                val codePoints = emojiResolvable.codePoints().toList().map { LorittaUtils.toUnicode(it).substring(2) }

                val result = codePoints.joinToString(separator = "-")
                val emojiUrl = "https://abs.twimg.com/emoji/v2/72x72/$result.png"

                val names = mutableListOf<String>()
                emojiResolvable.codePoints().forEach {
                    val name = Character.getName(it)
                    if (name != null)
                        names.add(name)
                }

                context.reply(false) {
                    embed {
                        title = "$emojiResolvable ${context.i18nContext.get(I18N_PREFIX.Info.AboutEmoji)}"
                        color = Constants.DISCORD_BLURPLE.rgb
                        image = emojiUrl
                    }

                    actionRow(
                        Button.link(
                            emojiUrl,
                            context.i18nContext.get(I18N_PREFIX.Info.OpenEmojiInBrowser)
                        ),
                        context.loritta.interactivityManager
                            .buttonForUser(context.user, ButtonStyle.PRIMARY, "Ver Informações") { info ->
                                info.deferAndEditOriginal {
                                    embed {
                                        color = Constants.DISCORD_BLURPLE.rgb
                                        title = "$emojiResolvable ${context.i18nContext.get(I18N_PREFIX.Info.AboutEmoji)}"
                                        thumbnail = emojiUrl

                                        if (names.isNotEmpty()) field {
                                            name = "${Emotes.BookMark} ${context.i18nContext.get(I18N_PREFIX.Info.EmojiName)}"
                                            value = "`${names.joinToString(" + ")}`"
                                            inline = true
                                        }

                                        field {
                                            name = "${Emotes.Eyes} ${context.i18nContext.get(I18N_PREFIX.Info.Mention)}"
                                            value = "`$emojiResolvable`"
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
                    )
                }
            } else {
                val foundEmote = context.guild.getEmojisByName(emojiResolvable, true).firstOrNull()

                if (foundEmote != null) {
                    sendEmojiMessage(context, foundEmote)
                } else {
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Info.EmojiNotFound(emojiResolvable))
                        )
                    }
                }
            }
        }

        private suspend fun sendEmojiMessage(context: UnleashedContext, emoji: CustomEmoji) {
            val cachedEmoji = context.loritta.lorittaShards.getEmoteById(emoji.id)
            val emojiCreatedAt = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(emoji.timeCreated)

            context.reply(false) {
                embed {
                    title = "\uD83D\uDDBC ${if (cachedEmoji != null) emoji.asMention else "✨"}"
                    color = Constants.DISCORD_BLURPLE.rgb

                    image = "${emoji.imageUrl}?size=2048"
                }

                actionRow(
                    Button.link(
                        "${emoji.imageUrl}?size=2048",
                        context.i18nContext.get(I18N_PREFIX.Info.OpenEmojiInBrowser)
                    ),
                    context.loritta.interactivityManager
                        .buttonForUser(context.user, ButtonStyle.PRIMARY, "Ver Informações") {
                            try {
                                it.deferAndEditOriginal {
                                    embed {
                                        title = "${if (cachedEmoji != null) emoji.asMention else "✨"} ${context.i18nContext.get(I18N_PREFIX.Info.AboutEmoji)}"
                                        color = Constants.DISCORD_BLURPLE.rgb
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

                                    actionRow(
                                        Button.link(
                                            "${emoji.imageUrl}?size=2048",
                                            context.i18nContext.get(I18N_PREFIX.Info.OpenEmojiInBrowser)
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                it.reply(true) {
                                    styled(
                                        "Não foi possível pegar as informações desse emoji."
                                    )
                                }
                            }
                        }
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val arg0 = args.getOrNull(0)
            if (arg0 != null) return mapOf(options.emoji to arg0)

            return null
        }
    }
}