package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.HomeController
import org.jooby.Jooby

class UserRoute : Jooby() {
	init {
		use(HomeController::class.java)
	}
}