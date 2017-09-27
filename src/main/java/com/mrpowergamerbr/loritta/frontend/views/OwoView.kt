package com.mrpowergamerbr.loritta.frontend.views

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext
import java.io.File

object OwoView {
	@JvmStatic
	fun render(context: RenderContext): Any {
		val lines = File(Loritta.FOLDER, "owo.txt").readLines()

		if (!lines.contains(context.request.header("X-Forwarded-For").value())) {
			File(Loritta.FOLDER, "owo.txt").appendText(context.request.header("X-Forwarded-For").value() + "\n")
		}

		return LorittaWebsite.engine.getTemplate("owo.html")
	}
}