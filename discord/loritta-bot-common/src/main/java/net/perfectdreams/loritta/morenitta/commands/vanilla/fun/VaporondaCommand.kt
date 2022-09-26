package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.text.VaporwaveUtils
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class VaporondaCommand : AbstractCommand("vaporonda", listOf("vaporwave"), category = net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.vaporwave.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.vaporwave.examples")

	// TODO: Fix Usage
	// TODO: Fix Detailed Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "text vaporwave")

		if (context.args.isNotEmpty()) {
			val vaporwave = VaporwaveUtils.vaporwave(context.args.joinToString(" "))
					.escapeMentions()

			context.reply(
                    LorittaReply(message = vaporwave, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}