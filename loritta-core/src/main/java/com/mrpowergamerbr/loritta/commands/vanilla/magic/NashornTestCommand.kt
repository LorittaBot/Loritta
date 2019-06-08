package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory

class NashornTestCommand : AbstractCommand("nashorn", category = CommandCategory.MAGIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Executa códigos em JavaScript usando a sandbox de comandos da Loritta"
	}

	override fun canHandle(context: CommandContext): Boolean {
		return context.userHandle.id in loritta.config.loritta.subOwnerIds || loritta.config.isOwner(context.userHandle.id)
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val javaScript = context.args.joinToString(" ")

		val nashornCmd = NashornCommand("teste", javaScript)

		nashornCmd.run(context, locale)
	}
}