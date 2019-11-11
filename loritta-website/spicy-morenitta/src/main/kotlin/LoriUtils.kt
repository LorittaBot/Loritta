
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.utils.locale.BaseLocale
import utils.LegacyBaseLocale
import utils.LorittaProfile
import utils.TingleModal
import utils.TingleOptions
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

	window.onerror = { message: dynamic, file: String, line: Int, col: Int, error: Any? ->
		if (!message.unsafeCast<String>().contains("adsbygoogle")) {
			// Ao dar um erro, nós iremos mostrar uma mensagem legal para o usuário, para que seja mais fácil resolver problema
			println("Erro detectado! Abrindo modal...")

			println("Message: " + message)
			println("file: " + file)
			println("line: " + line)
			println("col: " + col)
			println("error: " + error)
			println(error.asDynamic().stack)


			val content = """Error: ${message}
			|User Agent: ${window.navigator.userAgent}
			|URL: ${window.location.href}
			|User ID: ${selfProfile?.userId ?: "Unknown"}
			|Stack:
			|${error.asDynamic().stack}
		""".trimMargin()

			val modal = TingleModal(
					TingleOptions(
							footer = true,
							cssClass = arrayOf("tingle-modal--overflow")
					)
			)

			modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
				modal.close()
			}

			val stringBuilder = StringBuilder()
			stringBuilder.appendHTML(false).div(classes = "category-name") {
				+"Oopsie Woopsie! $error"
			}.appendHTML().div {
				div("pure-g vertically-centered-content") {
					style = "text-align: left;"
					div("pure-u-1 pure-u-md-1-4") {
						style = "overflow: hidden;"
						img(alt = "Loritta Pobre", src = "${loriUrl}assets/img/loritta_pobre.png", classes = "animated fadeInUp") {
							style = "width: 100%;"
						}
					}
					div("pure-u-1 pure-u-md-3-4") {
						div("sectionText") {
							h3("sectionHeader") {
								+legacyLocale?.get("ERROR_SomethingWentWrong") ?: "Alguma coisa deu errada..."
							}
							p {
								unsafe {
									raw(legacyLocale?.get("ERROR_WhatShouldIDo")
											?: "Infelizmente ninguém é perfeito... e pelo visto você encontrou um problema no meu website... Tente recarregar a página e veja se o problema persiste, caso persista, entre no meu <a href='${loriUrl}support'>servidor de suporte</a> e envie o código abaixo junto com uma pequena explicação sobre o que você estava tentando fazer no momento que deu o erro!")
								}
							}
							p { +legacyLocale?.get("ERROR_SorryForTheInconvenience") ?: "Desculpe pela inconveniência..." }
							pre {
								style = "word-wrap: break-word; white-space: pre-wrap;"
								+window.btoa(content)
							}
						}
					}
				}
			}

			modal.setContent(stringBuilder.toString())
			modal.open()
		}
		false
	}
}

fun testError() {
	val test: String? = null

	test!!
}