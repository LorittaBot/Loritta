package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import org.jooby.Request
import org.jooby.Response

// Para evitar criações de objetos desncessários, nós podemos usar o NoVarsView, que não precisa de uma map com as variáveis para usar
// Usado para a APIs do website da Loritta que não precisam de autenticação
abstract class NoVarsRequireAuthView : NoVarsView() {
	override fun render(req: Request, res: Response): String {
		val header = req.header("Lori-Authentication")
		val auth = header.value("???")

		if (Loritta.config.websiteApiKeys.contains(auth)) {
			return renderProtected(req, res)
		} else {
			val response = JsonObject()
			response["api:message"] = "UNAUTHORIZED"
			return response.toString()
		}
	}

	abstract fun renderProtected(req: Request, res: Response): String
}