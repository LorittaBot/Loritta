package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class NashornTestCommand : AbstractCommand("nashorn", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Executa códigos em JavaScript usando a sandbox de comandos da Loritta"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val javaScript = context.args.joinToString(" ")

		val nashornCmd = NashornCommand("teste", javaScript)

		nashornCmd.run(context, locale)
	}
}