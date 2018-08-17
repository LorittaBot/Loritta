package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.ExtrasController
import com.mrpowergamerbr.loritta.website.requests.routes.page.FanArtsController
import com.mrpowergamerbr.loritta.website.requests.routes.page.HomeController
import com.mrpowergamerbr.loritta.website.requests.routes.page.extras.ExtrasViewerController
import org.jooby.Jooby

class UserRoute : Jooby() {
	init {
		use(HomeController::class.java)
		use(FanArtsController::class.java)
		use(ExtrasController::class.java)
		use(ExtrasViewerController::class.java)
	}
}