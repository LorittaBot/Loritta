package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.WebhookUtils

class MagicBallCommand(loritta: LorittaBot) : AbstractCommand(loritta, "vieirinha", listOf("8ball", "magicball", "eightball"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
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
			val temmie = WebhookUtils.getOrCreateWebhook(loritta, context.event.textChannel!!, "Vieirinha")

			context.sendMessage(
				temmie,
				MessageCreateBuilder()
					.setContent(context.getAsMention(true) + locale.getList("commands.command.vieirinha.responses").random())
					.build(),
				"Vieirnha",
				"http://i.imgur.com/rRtHdti.png"
			)
		} else {
			context.explain()
		}
	}
}