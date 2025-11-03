@file:JsExport
@file:NoLiveLiterals
import androidx.compose.runtime.NoLiveLiterals
import net.perfectdreams.spicymorenitta.locale
import kotlin.js.*

object SaveStuff {
	@JsName("prepareSave")
	fun prepareSave(type: String, extras: ((payload: Json) -> Unit)? = null, showSaveScreen: Boolean = true, endpoint: String = "${loriUrl}api/v1/guilds/$guildId/config/") {
		println("Preparing saving stuff...")

		val json = json()
		val config = json()

		val toBeSaved = jq("[data-internal-name]")

		json["type"] = type

		toBeSaved.each { index, _elem ->
			val elem = jQuery(_elem)
			println(elem.attr("type") + " - " + elem.attr("id") + " - " + elem.`val`())
			val type = elem.attr("type").unsafeCast<String>()
			when (type) {
				"checkbox" -> config.set(elem.attr("data-internal-name"), elem.`is`(":checked"))
				"number" -> config.set(elem.attr("data-internal-name"), elem.`val`().toString().toDoubleOrNull())
				else -> config.set(elem.attr("data-internal-name"), elem.`val`())
			}
		}

		if (extras != null)
			extras.invoke(config)

		json["config"] = config

		if (showSaveScreen)
			LoriDashboard.showLoadingBar(locale["loritta.saving"] + "...")

		println("Sending save stuff... kthxbye!")
		println("Sending: " + JSON.stringify(json))
		val dynamic = object{}.asDynamic()
		dynamic.url = endpoint
		dynamic.type = "PATCH"
		dynamic.dataType = "json"
		dynamic.data = JSON.stringify(json)
		dynamic.success = {
			println("Done!")
			LoriDashboard.hideLoadingBar()
			LoriDashboard.configSavedSfx.play()
		}
		dynamic.error = {
			println("Error!")
			LoriDashboard.hideLoadingBar()
			LoriDashboard.configErrorSfx.play()
		}

		jQuery.ajax(
				settings = dynamic
		)
	}
}