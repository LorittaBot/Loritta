package com.mrpowergamerbr.loritta.website.views.subviews.api

import org.jooby.Request
import org.jooby.Response

// Para evitar criações de objetos desncessários, nós podemos usar o NoVarsView, que não precisa de uma map com as variáveis para usar
// Usado para a APIs do website da Loritta que não precisam de autenticação
abstract class NoVarsView {
	abstract fun handleRender(req: Request, res: Response, path: String): Boolean

	abstract fun render(req: Request, res: Response, path: String): String
}