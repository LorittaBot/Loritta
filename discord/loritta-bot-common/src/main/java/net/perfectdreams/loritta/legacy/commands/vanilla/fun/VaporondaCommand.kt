package net.perfectdreams.loritta.legacy.commands.vanilla.`fun`

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.escapeMentions
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.common.utils.text.VaporwaveUtils
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class VaporondaCommand : AbstractCommand("vaporonda", listOf("vaporwave"), category = CommandCategory.FUN) {
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