
import kotlinx.browser.document
import kotlinx.browser.window
import utils.LegacyBaseLocale
import kotlin.js.Json

lateinit var legacyLocale: LegacyBaseLocale

val loriUrl: String
	get() = "${window.location.protocol}//${window.location.host}/"

fun jq(query: String): JQuery {
	return jQuery(query as Any)
}

fun loadEmbeddedLocale() {
	println("Loading locale from embedded data... (if available)")
	val legacyLocaleJson = document.getElementById("locale-json")?.innerHTML

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

fun stripHtmlTagsUsingDom(html: String): String {
    val div = document.createElement("div")
    div.innerHTML = html
    return div.textContent ?: ""
}