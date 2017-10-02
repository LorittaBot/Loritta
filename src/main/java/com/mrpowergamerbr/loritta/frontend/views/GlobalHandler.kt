package com.mrpowergamerbr.loritta.frontend.views

import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import com.mrpowergamerbr.loritta.frontend.views.subviews.HomeView
import org.jooby.Request
import org.jooby.Response

object GlobalHandler {
	fun render(req: Request, res: Response): String {
		val views = getViews()

		views.filter { it.handleRender(req, res) }
			.forEach { return it.render(req, res) }

		return "404"
	}

	private fun getViews(): List<AbstractView> {
		val views = mutableListOf<AbstractView>()
		views.add(HomeView())
		return views
	}
}