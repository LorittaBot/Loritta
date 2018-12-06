package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure.ConfigureEconomyController
import com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure.ConfigureMemberCounterController
import com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure.ConfigureMiscellaneousController
import com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure.ConfigureTimersController
import org.jooby.Jooby

class GuildRoute : Jooby() {
	init {
		use(ConfigureEconomyController::class.java)
		use(ConfigureMiscellaneousController::class.java)
		use(ConfigureMemberCounterController::class.java)
		use(ConfigureTimersController::class.java)
	}
}