package net.perfectdreams.loritta.legacy.commands.vanilla.`fun`

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.WebhookUtils
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.api.commands.CommandArguments
import net.perfectdreams.loritta.legacy.api.commands.arguments
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

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

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "vieirinha")

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