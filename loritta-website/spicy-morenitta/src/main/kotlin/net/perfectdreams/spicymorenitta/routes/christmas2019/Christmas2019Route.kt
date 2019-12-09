package net.perfectdreams.spicymorenitta.routes.christmas2019

import io.ktor.client.request.get
import io.ktor.client.response.readText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.routes.BaseRoute
import kotlin.browser.window

class Christmas2019Route(val m: SpicyMorenitta) : BaseRoute("/christmas2019") {
	companion object {
		private const val LOCALE_PREFIX = "modules.levelUp"
	}

	override fun onUnload() {
	}

	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		GlobalScope.launch {
			println("Christmas!!!")

			val stats = http.get<io.ktor.client.response.HttpResponse>("${window.location.origin}/api/v1/loritta/christmas2019") {}

			val parse = JSON.nonstrict.parse<ChristmasPayload>(stats.readText())

			console.log(parse)

			Christmas2019App().start()
		}
	}

	@Serializable
	class ChristmasPayload(
			val points: Int,
			val alreadyReceived: Array<String>,
			val nextDrop: String
	)
}