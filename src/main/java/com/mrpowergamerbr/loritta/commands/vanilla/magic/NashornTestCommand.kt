package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class NashornTestCommand : CommandBase("nashorn") {
	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC
	}

	override fun onlyOwner(): Boolean {
		return true
	}

	override fun getDescription(): String {
		return "Executa códigos em JavaScript usando a sandbox de comandos da Loritta"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val javaScript = context.args.joinToString(" ")

		val nashornCmd = NashornCommand("teste", javaScript)

		nashornCmd.run(context, locale)
	}
}