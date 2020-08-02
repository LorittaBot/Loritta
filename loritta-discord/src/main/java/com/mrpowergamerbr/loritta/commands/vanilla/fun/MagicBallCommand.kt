package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.WebhookUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class MagicBallCommand : AbstractCommand("vieirinha", listOf("8ball", "magicball", "eightball"), CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.entertainment.vieirinha.description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.TEXT) {
				optional = false
			}
		}
	}

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return locale.toNewLocale().getList("commands.entertainment.vieirinha.examples")
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val temmie = WebhookUtils.getOrCreateWebhook(context.event.textChannel!!, "Vieirinha")

			context.sendMessage(temmie, WebhookMessageBuilder()
					.setUsername("Vieirinha")
					.setContent(context.getAsMention(true) + locale.toNewLocale().getList("commands.entertainment.vieirinha.responses").random())
					.setAvatarUrl("http://i.imgur.com/rRtHdti.png")
					.build())
		} else {
			context.explain()
		}
	}
}
