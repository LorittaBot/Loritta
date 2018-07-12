package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure.ConfigureEconomyController
import org.jooby.Jooby

class GuildRoute : Jooby() {
	init {
		use(ConfigureEconomyController::class.java)
	}
}