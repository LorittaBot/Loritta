package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import mu.KotlinLogging
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status

// Para evitar criações de objetos desncessários, nós podemos usar o NoVarsView, que não precisa de uma map com as variáveis para usar
// Usado para a APIs do website da Loritta que não precisam de autenticação
abstract class NoVarsRequireAuthView : NoVarsView() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun render(req: Request, res: Response, path: String): String {
		val header = req.header("Lori-Authentication")
		val auth = header.value("???")

		val validKey = Loritta.config.websiteApiKeys.filter {
			it.name == auth &&
					(it.allowed.contains("*") || it.allowed.contains(path))
		}.firstOrNull()

		logger.debug { "$auth está tentando acessar $path, utilizando key $validKey" }
		if (validKey != null) {
			return renderProtected(req, res, path)
		} else {
			logger.warn { "$auth foi rejeitado ao tentar acessar $path!" }
			val response = JsonObject()
			response["api:message"] = "UNAUTHORIZED"
			response["api:code"] = LoriWebCodes.UNAUTHORIZED
			res.status(Status.UNAUTHORIZED)
			return response.toString()
		}
	}

	abstract fun renderProtected(req: Request, res: Response, path: String): String
}