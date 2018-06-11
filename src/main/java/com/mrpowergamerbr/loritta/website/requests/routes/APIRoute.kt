package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks.IpCallbackController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks.MixerCallbackController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks.UserAgentCallbackController
import org.jooby.Jooby

class APIRoute : Jooby() {
	init {
		use(MixerCallbackController::class.java)
		use(IpCallbackController::class.java)
		use(UserAgentCallbackController::class.java)
	}
}