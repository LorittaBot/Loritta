package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.*
import com.mrpowergamerbr.loritta.website.requests.routes.page.extras.ExtrasViewerController
import com.mrpowergamerbr.loritta.website.requests.routes.page.landingpages.BrazilianBotLandingPageController
import com.mrpowergamerbr.loritta.website.requests.routes.page.sponsors.SponsorsRedirectController
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.UserProfileController
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.UserReputationController
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.dashboard.ProfileListController
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.dashboard.ShipEffectsController
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.dashboard.UserDashboardController
import org.jooby.Jooby

class UserRoute : Jooby() {
	init {
		use(HomeController::class.java)
		use(FanArtsController::class.java)
		use(FanArtArtistController::class.java)
		use(ExtrasController::class.java)
		use(ExtrasViewerController::class.java)
		use(DonateController::class.java)
		use(SponsorsController::class.java)
		use(SponsorsRedirectController::class.java)
		use(BlogController::class.java)
		use(BlogPostController::class.java)

		// ===[ LANDING PAGES ]===
		use(BrazilianBotLandingPageController::class.java)

		// ===[ PROFILES ]===
		use(UserProfileController::class.java)
		use(UserDashboardController::class.java)
		use(ShipEffectsController::class.java)
		use(ProfileListController::class.java)
		use(UserReputationController::class.java)
	}
}