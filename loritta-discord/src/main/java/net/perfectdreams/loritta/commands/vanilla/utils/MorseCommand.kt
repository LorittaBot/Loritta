package net.perfectdreams.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.fromMorse
import com.mrpowergamerbr.loritta.utils.toMorse
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import java.awt.Color

class MorseCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("morse"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.morse"
	}

	override fun command() = create {
		usage {
			argument(ArgumentType.TEXT) {}
		}

		examples {
			+ "loritta"
		}

		localizedDescription("$LOCALE_PREFIX.description")

		executesDiscord {
			val context = this

			if (context.args.isNotEmpty()) {
				val message = context.args.joinToString(" ")

				val toMorse = message.toUpperCase().toMorse()
				val fromMorse = message.fromMorse()

				if (toMorse.trim().isEmpty()) {
					context.reply(
							LorittaReply(
									locale["$LOCALE_PREFIX.fail"],
									Constants.ERROR
							)
					)
					return@executesDiscord
				}

				val embed = EmbedBuilder()

				embed.setTitle(if (fromMorse.isNotEmpty()) "\uD83D\uDC48\uD83D\uDCFB ${locale["commands.utils.morse.toFrom"]}" else "\uD83D\uDC49\uD83D\uDCFB ${locale["commands.utils.morse.fromTo"]}")
				embed.setDescription("*beep* *boop*```${if (fromMorse.isNotEmpty()) fromMorse else toMorse}```")
				embed.setColor(Color(153, 170, 181))

				context.sendMessage(context.getUserMention(true), embed.build())
			} else {
				context.explain()
			}
		}
	}
}