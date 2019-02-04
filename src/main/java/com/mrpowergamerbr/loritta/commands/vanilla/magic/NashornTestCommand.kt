package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class NashornTestCommand : AbstractCommand("nashorn", category = CommandCategory.MAGIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Executa códigos em JavaScript usando a sandbox de comandos da Loritta"
	}

	override fun canHandle(context: CommandContext): Boolean {
		return context.userHandle.id in Loritta.config.subOwnerIds || context.userHandle.id == Loritta.config.ownerId
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val javaScript = context.args.joinToString(" ")

		val nashornCmd = NashornCommand("teste", javaScript)

		nashornCmd.run(context, locale)
	}
}