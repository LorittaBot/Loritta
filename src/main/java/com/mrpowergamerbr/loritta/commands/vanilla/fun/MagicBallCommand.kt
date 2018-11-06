package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.WebhookUtils
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage

class MagicBallCommand : AbstractCommand("vieirinha", listOf("8ball", "magicball", "eightball"), CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.vieirinha.description }
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.TEXT) {
				optional = false
			}
		}
	}

	override fun getExamples(locale: BaseLocale): List<String> {
		return locale.format { commands.vieirinha.examples }
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val temmie = WebhookUtils.getOrCreateWebhook(context.event.textChannel!!, "Vieirinha")

			context.sendMessage(temmie, DiscordMessage.builder()
					.username("Vieirinha")
					.content(context.getAsMention(true) + locale.commands.vieirinha.responses.getRandom())
					.avatarUrl("http://i.imgur.com/rRtHdti.png")
					.build())
		} else {
			context.explain()
		}
	}
}
