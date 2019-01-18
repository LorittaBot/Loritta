package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure.*
import org.jooby.Jooby

class GuildRoute : Jooby() {
	init {
		use(ConfigureEconomyController::class.java)
		use(ConfigureMiscellaneousController::class.java)
		use(ConfigureMemberCounterController::class.java)
		use(ConfigureTimersController::class.java)
		use(ConfigureReactionRoleController::class.java)
		use(ConfigurePremiumKeyController::class.java)
		use(ConfigureCustomBadgeController::class.java)
		use(ConfigureDailyMultiplierController::class.java)
	}
}