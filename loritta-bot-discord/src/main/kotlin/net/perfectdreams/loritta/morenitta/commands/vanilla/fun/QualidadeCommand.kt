package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class QualidadeCommand(loritta: LorittaBot) : AbstractCommand(loritta, "qualidade", category = net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.quality.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.quality.examples")
	// TODO: Fix Usage
	// TODO: Fix Detailed Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "text quality")

		if (context.args.isNotEmpty()) {
			val qualidade = context.args.joinToString(" ").toCharArray().joinToString(" ").uppercase()
					.escapeMentions()
			context.reply(
                    LorittaReply(message = qualidade, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}