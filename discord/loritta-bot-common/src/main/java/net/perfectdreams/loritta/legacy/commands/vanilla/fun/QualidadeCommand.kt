package net.perfectdreams.loritta.legacy.commands.vanilla.`fun`

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.escapeMentions
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class QualidadeCommand : AbstractCommand("qualidade", category = CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.quality.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.quality.examples")
	// TODO: Fix Usage
	// TODO: Fix Detailed Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "text quality")

		if (context.args.isNotEmpty()) {
			val qualidade = context.args.joinToString(" ").toCharArray().joinToString(" ").toUpperCase()
					.escapeMentions()
			context.reply(
                    LorittaReply(message = qualidade, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}