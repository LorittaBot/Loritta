package com.mrpowergamerbr.loritta.commands.nashorn

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory

/**
 * Comandos usando a Nashorn Engine
 */
class NashornCommand(label: String, val javaScriptCode: String) : AbstractCommand(label, category = CommandCategory.MISC) {
	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		context.reply(
				LoriReply(
						"Comandos personalizados estão desativados devido a problemas de segurança, desculpe pela inconveniência!"
				),
				LoriReply(
						"Custom commands are disabled due to security reasons, sorry for the inconvenience!"
				)
		)
	}
}