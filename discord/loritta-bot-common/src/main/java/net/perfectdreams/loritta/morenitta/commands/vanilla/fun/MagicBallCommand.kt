package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.WebhookUtils
import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.api.commands.CommandArguments
import net.perfectdreams.loritta.common.api.commands.arguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class MagicBallCommand : AbstractCommand("vieirinha", listOf("8ball", "magicball", "eightball"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
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