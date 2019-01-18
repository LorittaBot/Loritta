package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.DonateController
import com.mrpowergamerbr.loritta.website.requests.routes.page.ExtrasController
import com.mrpowergamerbr.loritta.website.requests.routes.page.FanArtsController
import com.mrpowergamerbr.loritta.website.requests.routes.page.HomeController
import com.mrpowergamerbr.loritta.website.requests.routes.page.extras.ExtrasViewerController
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.UserDashboardController
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.UserProfileController
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.UserReputationController
import org.jooby.Jooby

class UserRoute : Jooby() {
	init {
		use(HomeController::class.java)
		use(FanArtsController::class.java)
		use(ExtrasController::class.java)
		use(ExtrasViewerController::class.java)
		use(DonateController::class.java)

		use(UserProfileController::class.java)
		use(UserDashboardController::class.java)
		use(UserReputationController::class.java)
	}
}