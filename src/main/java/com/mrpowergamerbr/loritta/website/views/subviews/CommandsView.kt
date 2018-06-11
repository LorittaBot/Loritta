package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.utils.loritta
import org.jooby.Request
import org.jooby.Response

class CommandsView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/commands" || path == "/comandos" || path == "/commands/count"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		if (path == "/commands/count") {
			var aux = ""
			for ((index, command) in loritta.commandManager.commandMap.sortedByDescending { it.executedCount }.withIndex()) {
				aux += "${index + 1}. ${command::class.java.simpleName} — ${command.executedCount} vezes<br>"
			}

			return aux;
		}

		return evaluate("commands.html", variables)
	}
}