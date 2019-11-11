package net.perfectdreams.spicymorenitta.utils

import LoriDashboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import locale
import kotlin.js.Json
import kotlin.js.json

external val guildId: String

object SaveUtils {
	fun prepareSave(type: String, extras: ((payload: Json) -> Unit), showSaveScreen: Boolean = true, onFinish: ((HttpResponse) -> (Unit))? = null, endpoint: String = "${loriUrl}api/v1/guild/$guildId/config") {
		println("Preparing saving stuff...")

		val json = json()
		val config = json()

		json["type"] = type
		extras.invoke(config)

		json["config"] = config

		// if (showSaveScreen)
		// 	LoriDashboard.showLoadingBar("Salvando...")

		println("Sending save stuff... kthxbye!")
		println("Sending: " + JSON.stringify(json))
		println("Endpoint: $endpoint")

		LoriDashboard.showLoadingBar(locale["loritta.saving"] + "...")

		GlobalScope.launch {
			val response = HttpRequest.patch(endpoint, JSON.stringify(json))
			LoriDashboard.hideLoadingBar()

			onFinish?.invoke(response)

			LoriDashboard.configSavedSfx.play()
		}
	}
}