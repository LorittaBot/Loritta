package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.deviousfun.entities.Emote
import net.perfectdreams.loritta.deviousfun.DeviousEmbed
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.deviousfun.entities.DiscordEmote
import net.perfectdreams.loritta.deviousfun.entities.GuildEmoteFromMessage
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import kotlin.streams.toList
import net.perfectdreams.loritta.morenitta.LorittaBot

class EmojiInfoCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "emojiinfo",
    category = net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD
) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.emojiinfo.description")

    override fun getUsage(): CommandArguments {
        return arguments {
            argument(ArgumentType.EMOTE) {}
        }
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        if (context.rawArgs.isNotEmpty()) {
            OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "emoji info")

            val arg0 = context.rawArgs[0]
            val firstEmote = context.message.emotes.firstOrNull()
            if (firstEmote != null) {
                // Emoji do Discord (via menção)
                showDiscordEmoteInfo(context, firstEmote)
                return
            }

            if (arg0.isValidSnowflake()) {
                val emote = context.guildOrNull?.getEmoteById(arg0)
                if (emote != null) {
                    // Emoji do Discord (via ID)
                    showDiscordEmoteInfo(context, emote)
                    return
                } else {
                    context.reply(
                        LorittaReply(
                            locale["commands.command.emoji.notFoundId", "`$arg0`"],
                            Constants.ERROR
                        )
                    )
                    return
                }
            }

            val isUnicodeEmoji = Constants.EMOJI_PATTERN.matcher(arg0).find()

            if (isUnicodeEmoji) {
                val codePoints = arg0.codePoints().toList().map { LorittaUtils.toUnicode(it).substring(2) }

                val value = codePoints.joinToString(separator = "-")
                val emojiUrl = "https://twemoji.maxcdn.com/2/72x72/$value.png"

                val embed = EmbedBuilder()
                embed.setColor(Constants.DISCORD_BLURPLE)
                embed.setTitle("$arg0 ${context.locale["commands.command.emojiinfo.aboutEmoji"]}")
                embed.setThumbnail(emojiUrl)

                val names = mutableListOf<String>()
                arg0.codePoints().forEach {
                    val name = Character.getName(it)
                    if (name != null)
                        names.add(name)
                }

                if (names.isNotEmpty())
                    embed.addField(
                        "\uD83D\uDD16 ${context.locale["commands.command.emojiinfo.emojiName"]}",
                        "`${names.joinToString(" + ")}`",
                        true
                    )

                embed.addField("\uD83D\uDC40 ${context.locale["commands.command.emojiinfo.mention"]}", "`$arg0`", true)
                embed.addField("\uD83D\uDCBB Unicode", "`${codePoints.map { "\\$it" }.joinToString("")}`", true)
                embed.addField("⛓ Link", emojiUrl, true)

                context.sendMessage(context.getAsMention(true), embed.build())
            } else {
                context.explain()
            }
        } else {
            context.explain()
        }
    }

    suspend fun showDiscordEmoteInfo(context: CommandContext, emote: DiscordEmote) {
        context.sendMessage(context.getAsMention(true), getDiscordEmoteInfoEmbed(context, emote))
    }

    companion object {
        fun getDiscordEmoteInfoEmbed(context: CommandContext, emote: DiscordEmote): DeviousEmbed {
            val canUse = context.guildOrNull?.getEmoteById(emote.id) != null
            val emoteTitle = if (canUse)
                emote.asMention
            else
                "✨"
            val embed = EmbedBuilder()
            embed.setColor(Constants.DISCORD_BLURPLE)
            embed.setTitle("$emoteTitle ${context.locale["commands.command.emojiinfo.aboutEmoji"]}")
            embed.setThumbnail(emote.imageUrl)
            embed.addField(
                "\uD83D\uDD16 ${context.locale["commands.command.emojiinfo.emojiName"]}",
                "`${emote.name}`",
                true
            )
            embed.addField(
                "\uD83D\uDCBB ${context.locale["commands.command.emojiinfo.emojiId"]}",
                "`${emote.id}`",
                true
            )
            embed.addField(
                "\uD83D\uDC40 ${context.locale["commands.command.emojiinfo.mention"]}",
                "`${emote.asMention}`",
                true
            )
            embed.addField(
                "\uD83D\uDCC5 ${context.locale["commands.command.emojiinfo.emojiCreated"]}",
                DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(emote.timeCreated, context.locale),
                true
            )
            embed.addField("⛓ Link", emote.imageUrl + "?size=2048", true)
            return embed.build()
        }
    }
}