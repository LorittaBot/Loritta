package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import kotlin.streams.toList

class EmojiInfoCommand : AbstractCommand("emojiinfo", category = CommandCategory.DISCORD) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.emojiinfo.description")

	override fun getUsage(): CommandArguments {
		return arguments {
			argument(ArgumentType.EMOTE) {}
		}
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			val arg0 = context.rawArgs[0]
			val firstEmote = context.message.emotes.firstOrNull()
			if (arg0 == firstEmote?.asMention) {
				// Emoji do Discord (via menção)
				showDiscordEmoteInfo(context, firstEmote)
				return
			}

			if (arg0.isValidSnowflake()) {
				val emote = lorittaShards.getEmoteById(arg0)
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

			val guild = context.guild
			val foundEmote = guild.getEmotesByName(arg0, true).firstOrNull()
			if (foundEmote != null) {
				// Emoji do Discord (via nome)
				showDiscordEmoteInfo(context, foundEmote)
				return
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
					embed.addField("\uD83D\uDD16 ${context.locale["commands.command.emojiinfo.emojiName"]}", "`${names.joinToString(" + ")}`", true)

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

	suspend fun showDiscordEmoteInfo(context: CommandContext, emote: Emote) {
		context.sendMessage(context.getAsMention(true), getDiscordEmoteInfoEmbed(context, emote))
	}

	companion object {
		fun getDiscordEmoteInfoEmbed(context: CommandContext, emote: Emote): MessageEmbed {
			// Se o usuário usar um emoji de um servidor que a Lori NÃO compartilha, então ela não vai conseguir usar!
			// Por isto, iremos pegar se ela conhece o emoji a partir das shards
			val cachedEmote = lorittaShards.getEmoteById(emote.id)
			val canUse = cachedEmote != null
			// E vamos pegar a fonte da guild a partir do nosso emoji cacheado, já que ela pode conhecer em outra shard, mas não na atual!
			val sourceGuild = cachedEmote?.guild

			val emoteTitle = if (canUse)
				emote.asMention
			else
				"✨"
			val embed = EmbedBuilder()
			embed.setColor(Constants.DISCORD_BLURPLE)
			embed.setTitle("$emoteTitle ${context.locale["commands.command.emojiinfo.aboutEmoji"]}")
			embed.setThumbnail(emote.imageUrl)
			embed.addField("\uD83D\uDD16 ${context.locale["commands.command.emojiinfo.emojiName"]}", "`${emote.name}`", true)
			embed.addField("\uD83D\uDCBB ${context.locale["commands.command.emojiinfo.emojiId"]}", "`${emote.id}`", true)
			embed.addField("\uD83D\uDC40 ${context.locale["commands.command.emojiinfo.mention"]}", "`${emote.asMention}`", true)
			embed.addField("\uD83D\uDCC5 ${context.locale["commands.command.emojiinfo.emojiCreated"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(emote.timeCreated, context.locale), true)
			if (sourceGuild != null)
				embed.addField("\uD83D\uDD0E ${context.locale["commands.command.emojiinfo.seenAt"]}", "`${sourceGuild.name}`", true)
			embed.addField("⛓ Link", emote.imageUrl + "?size=2048", true)
			return embed.build()
		}
	}
}