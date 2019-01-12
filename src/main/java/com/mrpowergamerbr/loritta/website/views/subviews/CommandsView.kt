package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.LorittaCommand
import org.jooby.Request
import org.jooby.Response

class CommandsView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/commands" || path == "/comandos" || path == "/commands/count"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		if (path == "/commands/count") {
			var aux = ""
			val commands = loritta.legacyCommandManager.commandMap + loritta.commandManager.commands
			for ((index, command) in commands.sortedByDescending { if (it is AbstractCommand) it.executedCount else if (it is LorittaCommand) it.executedCount else throw UnsupportedOperationException() }.withIndex()) {
				val executedCount = if (command is AbstractCommand)
					command.executedCount
				else if (command is LorittaCommand)
					command.executedCount
				else throw UnsupportedOperationException()

				aux += "${index + 1}. ${command::class.java.simpleName} — $executedCount vezes<br>"
			}

			return aux
		}

		return evaluate("commands.html", variables)
	}
}