package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks.*
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.economy.LoriTransferBalanceController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild.SendMessageGuildController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild.StoreItemsGuildController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild.UpdateServerConfigController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta.UsersController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user.CreateDonationPaymentController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user.ProfileImageController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user.SelfProfileController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user.UserReputationController
import org.jooby.Jooby

class APIRoute : Jooby() {
	init {
		// ===[ GUILDS ]===
		use(UpdateServerConfigController::class.java)
		use(SendMessageGuildController::class.java)
		use(StoreItemsGuildController::class.java)

		// ===[ CALLBACKS ]===
		use(MixerCallbackController::class.java)
		use(DiscordBotsCallbackController::class.java)
		use(PubSubHubbubCallbackController::class.java)
		use(LoriTransferBalanceController::class.java)
		use(UpdateAvailableCallbackController::class.java)
		use(MercadoPagoCallbackController::class.java)

		// ===[ USERS ]===
		use(UserReputationController::class.java)
		use(SelfProfileController::class.java)
		use(CreateDonationPaymentController::class.java)
		use(ProfileImageController::class.java)

		// ===[ LORITTA ]===
		use(UsersController::class.java)
	}
}