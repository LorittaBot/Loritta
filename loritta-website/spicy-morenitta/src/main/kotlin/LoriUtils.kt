
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.utils.locale.BaseLocale
import utils.LegacyBaseLocale
import utils.LorittaProfile
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Json

lateinit var legacyLocale: LegacyBaseLocale
lateinit var locale: BaseLocale
var selfProfile: LorittaProfile? = null

val loriUrl: String
	get() = "${window.location.protocol}//${window.location.host}/"

fun jq(query: String): JQuery {
	return jQuery(query as Any)
}

fun <T> Any.toJson(): T {
	return JSON.parse(JSON.stringify(this))
}

fun Any.toJson(): Json {
	return JSON.parse(JSON.stringify(this))
}

fun Any.stringify(): String {
	return JSON.stringify(this)
}

@ImplicitReflectionSerializer
fun loadEmbeddedLocale() {
	println("Loading locale from embedded data... (if available)")
	val localeJson = document.getElementById("base-locale-json")?.innerHTML
	val legacyLocaleJson = document.getElementById("locale-json")?.innerHTML

	if (localeJson != null) {
		println("Embedded locale found!")
		println("base-locale-json: $localeJson")
		// Já que Reflection não existe em Kotlin/JS, o Kotlin Serialization não suporta "Any?" em JavaScript.
		// A Lori envia o locale sem listas, apenas com strings, yay!
		val baseLocale = kotlinx.serialization.json.JSON.parse<BaseLocale>(localeJson)
		println("Parsed BaseLocale from embedded locale within body!")

		println("There is ${baseLocale.localeEntries.size} keys!")
		locale = baseLocale
	}

	if (legacyLocaleJson != null) {
		println("Embedded *legacy* locale found!")
		println("locale-json: $legacyLocaleJson")
		val asJson = JSON.parse<Json>(legacyLocaleJson)
		println("Converted it to JSON... $asJson")

		var alreadyInitalized = ::legacyLocale.isInitialized

		if (alreadyInitalized) {
			println("Locale already initalized, we aren't going to replace it then...")
		} else {
			legacyLocale = LegacyBaseLocale.create(asJson)
			println("Parsed LegacyBaseLocale from embedded locale within body!")

			println("Testing keys...")
			println(legacyLocale!!["KEYWORD_STREAMER"])
			println(legacyLocale!!["RAFFLE_YouEarned", 250])
			println(legacyLocale!!["LORITTA_ADDED_ON_SERVER", "MrPowerGamerBR", "PerfectDreams", "Apartamento da Loritta", 125, "${loriUrl}"])
		}
	} else {
		println("Couldn't find embedded data in body!")
	}
}

@ImplicitReflectionSerializer
fun oldMain(args: Array<String>) {
	println("LoriUtils! ^-^")

	document.addEventListener("DOMContentLoaded", {
		loadEmbeddedLocale()

		true
	})
}

fun testError() {
	val test: String? = null

	test!!
}