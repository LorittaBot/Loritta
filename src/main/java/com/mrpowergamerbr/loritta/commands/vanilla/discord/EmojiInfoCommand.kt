package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Emote

class EmojiInfoCommand : AbstractCommand("emojiinfo", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.discord.emojiInfo.description }
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
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
				}
			}

			val guild = context.guild
			val foundEmote = guild.getEmotesByName(arg0, true).firstOrNull()
			if (foundEmote != null) {
				// Emoji do Discord (via nome)
				showDiscordEmoteInfo(context, foundEmote)
				return
			}
		} else {
			context.explain()
		}
	}

	suspend fun showDiscordEmoteInfo(context: CommandContext, emote: Emote) {
		val embed = EmbedBuilder()
		embed.setColor(Constants.DISCORD_BLURPLE)
		embed.setTitle("${emote.asMention} Sobre o Emoji")
		embed.setThumbnail(emote.imageUrl)
		embed.addField("\uD83D\uDD16 Nome do Emoji", "`${emote.name}", true)
		embed.addField("\uD83D\uDCBB ID do Emoji", "`${emote.id}", true)
		embed.addField("\uD83D\uDC40 Menção", "``${emote.asMention}`", true)
		embed.addField("\uD83D\uDCC5 Criado há", DateUtils.formatDateDiff(emote.creationTime.toInstant().toEpochMilli(), context.locale), true)
		embed.addField("\uD83D\uDD0E Avistado em", "`${emote.guild?.name}", true)

		context.sendMessage(context.getAsMention(true), embed.build())
	}
}