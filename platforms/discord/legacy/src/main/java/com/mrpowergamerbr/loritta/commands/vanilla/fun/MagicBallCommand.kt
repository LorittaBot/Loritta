package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.WebhookUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class MagicBallCommand : AbstractCommand("vieirinha", listOf("8ball", "magicball", "eightball"), CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.vieirinha.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.vieirinha.examples")

	override fun getUsage(): CommandArguments {
		return arguments {
			argument(ArgumentType.TEXT) {
				optional = false
			}
		}
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

			context.sendMessage(temmie, WebhookMessageBuilder()
					.setUsername("Vieirinha")
					.setContent(context.getAsMention(true) + locale.getList("commands.command.vieirinha.responses").random())
					.setAvatarUrl("http://i.imgur.com/rRtHdti.png")
					.build())
		} else {
			context.explain()
		}
	}
}