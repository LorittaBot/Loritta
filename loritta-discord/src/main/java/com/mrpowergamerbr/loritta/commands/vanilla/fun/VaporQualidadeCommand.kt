package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.escapeMentions
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.utils.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.misc.VaporwaveUtils
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class VaporQualidadeCommand : AbstractCommand("vaporqualidade", category = CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.vaporquality.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.vaporquality.examples")

	// TODO: Fix Usage
	// TODO: Fix Detailed Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val qualidade = VaporwaveUtils.vaporwave(context.args.joinToString(" ").toCharArray().joinToString(" ")).toUpperCase()
					.escapeMentions()

			context.reply(
                    LorittaReply(message = qualidade, prefix = "✍")
			)
		} else {
			this.explain(context)
		}
	}
}