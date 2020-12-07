package net.perfectdreams.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import kotlin.streams.toList

class EmojiInfoCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("emojiinfo"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord.emojiInfo"

		fun getDiscordEmoteInfoEmbed(context: DiscordCommandContext, emote: Emote): MessageEmbed {
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
			embed.setTitle("$emoteTitle ${context.locale["$LOCALE_PREFIX.aboutEmoji"]}")
			embed.setThumbnail(emote.imageUrl)
			embed.addField("\uD83D\uDD16 ${context.locale["$LOCALE_PREFIX.emojiName"]}", "`${emote.name}`", true)
			embed.addField("\uD83D\uDCBB ${context.locale["$LOCALE_PREFIX.emojiId"]}", "`${emote.id}`", true)
			embed.addField("\uD83D\uDC40 ${context.locale["$LOCALE_PREFIX.mention"]}", "`${emote.asMention}`", true)
			embed.addField("\uD83D\uDCC5 ${context.locale["$LOCALE_PREFIX.emojiCreated"]}", DateUtils.formatDateDiff(emote.timeCreated.toInstant().toEpochMilli(), context.locale), true)
			if (sourceGuild != null)
				embed.addField("\uD83D\uDD0E ${context.locale["$LOCALE_PREFIX.seenAt"]}", "`${sourceGuild.name}`", true)
			embed.addField("⛓ Link", emote.imageUrl + "?size=2048", true)
			return embed.build()
		}
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		usage {
			argument(ArgumentType.EMOTE) {}
		}

		executesDiscord {
			val context = this

			if (context.args.isNotEmpty()) {
				val arg0 = context.args[0]
				val firstEmote = context.discordMessage.emotes.firstOrNull()
				if (arg0 == firstEmote?.asMention) {
					// Emoji do Discord (via menção)
					showDiscordEmoteInfo(context, firstEmote)
					return@executesDiscord
				}

				if (arg0.isValidSnowflake()) {
					val emote = lorittaShards.getEmoteById(arg0)
					if (emote != null) {
						// Emoji do Discord (via ID)
						showDiscordEmoteInfo(context, emote)
						return@executesDiscord
					} else {
						context.reply(
								LorittaReply(
										locale["$LOCALE_PREFIX.notFoundId", "`$arg0`"],
										Constants.ERROR
								)
						)
						return@executesDiscord
					}
				}

				val guild = context.guild
				val foundEmote = guild.getEmotesByName(arg0, true).firstOrNull()
				if (foundEmote != null) {
					// Emoji do Discord (via nome)
					showDiscordEmoteInfo(context, foundEmote)
					return@executesDiscord
				}

				val isUnicodeEmoji = Constants.EMOJI_PATTERN.matcher(arg0).find()

				if (isUnicodeEmoji) {
					val codePoints = arg0.codePoints().toList().map { LorittaUtils.toUnicode(it).substring(2) }

					val value = codePoints.joinToString(separator = "-")
					val emojiUrl = "https://twemoji.maxcdn.com/2/72x72/$value.png"

					val embed = EmbedBuilder()
					embed.setColor(Constants.DISCORD_BLURPLE)
					embed.setTitle("$arg0 ${context.locale["commands.discord.emojiInfo.aboutEmoji"]}")
					embed.setThumbnail(emojiUrl)

					val names = mutableListOf<String>()
					arg0.codePoints().forEach {
						val name = Character.getName(it)
						if (name != null)
							names.add(name)
					}

					if (names.isNotEmpty())
						embed.addField("\uD83D\uDD16 ${context.locale["commands.discord.emojiInfo.emojiName"]}", "`${names.joinToString(" + ")}`", true)

					embed.addField("\uD83D\uDC40 ${context.locale["commands.discord.emojiInfo.mention"]}", "`$arg0`", true)
					embed.addField("\uD83D\uDCBB Unicode", "`${codePoints.map { "\\$it" }.joinToString("")}`", true)
					embed.addField("⛓ Link", emojiUrl, true)

					context.sendMessage(context.getUserMention(true), embed.build())
				} else {
					context.explain()
				}
			} else {
				context.explain()
			}
		}
	}

	suspend fun showDiscordEmoteInfo(context: DiscordCommandContext, emote: Emote) {
		context.sendMessage(context.getUserMention(true), getDiscordEmoteInfoEmbed(context, emote))
	}
}