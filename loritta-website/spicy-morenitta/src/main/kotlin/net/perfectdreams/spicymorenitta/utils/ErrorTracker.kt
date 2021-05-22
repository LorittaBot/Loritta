package net.perfectdreams.spicymorenitta.utils

import io.ktor.client.request.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.locale.buildAsHtml
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams
import kotlin.js.Json

object ErrorTracker : Logging {
	private val isLocaleInitialized: Boolean
		get() = ::locale.isInitialized

	private val SOMETHING_WENT_WRONG: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.somethingWentWrong"] else "Something went wrong..."

	private val WHAT_SHOULD_I_DO: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.whatShouldIDo"] else "Nothing is perfect... and looks like you found an issue on my website. Try reloading the page, update your browser and, if the issue persists, join {0} and send the code below with a small explanation of what you were trying to do at the moment the issue happened, so we can analyze and fix the issue!"

	private val SORRY_FOR_THE_INCONVENIENCE: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.sorryForTheInconvenience"] else "Sorry for the inconvenience, I hope I can fix this issue soon!"

	private val ERROR_CODE_ID: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.errorCodeId"] else "Error Code ID: {0}"

	private val MY_SUPPORT_SERVER: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.mySupportServer"] else "my support server"

	var isAlreadySending = false

	fun start(m: SpicyMorenitta) {
		debug("Starting Error Tracker...")

		window.onerror = callback@{ message: dynamic, file: String, line: Int, col: Int, error: Any? ->
			// Ao dar um erro, nós iremos mostrar uma mensagem legal para o usuário, para que seja mais fácil resolver problema
			warn("Error detected! Opening modal...")

			if (message.unsafeCast<String>().contains("adsbygoogle")) { // AdSense
				warn("But looks like it is an AdSense error, we are going to ignore it because we don't need to track *that*")
				return@callback false
			}

			// Hack: We don't want to track errors from other scripts (like ads) so we will just ignore if the source doesn't contain "app.js"
			// (app.js = ourselves)
			if (error != null && !error.asDynamic().stack.unsafeCast<String>().contains("app.js")) {
				warn("But looks like it is didn't come from our script, we are going to ignore it because we don't need to track *that*")
				return@callback false
			}

			processException(m, message as String, file, line, col, error)
			false
		}
	}

	fun processException(m: SpicyMorenitta, message: String, file: String, line: Int, col: Int, error: Any?) {
		val selfScript = document.selectAll<HTMLScriptElement>("script").firstOrNull { it.src.contains("app.js") }

		val selfHash = selfScript?.let {
			try {
				URLSearchParams(URL(it.src).search.substring(1)).get("hash")
			} catch (e: Error) {
				warn("Something went wrong while trying to get the app hash")
			}
		}

		debug("Script hash: $selfHash")
		// Gambiarra, a gente só quer usar o buildAsHtml do BaseLocale
		// Ele nem usa nenhuma das entries, então vamos apenas criar um dummy locale e utilizá-lo
		// No futuro seria melhor mover o buildAsHtml para um código separado
		val locale = BaseLocale("dummy", mapOf(), mapOf())

		warn("Message: $message")
		warn("File: $file")
		warn("Line: $line")
		warn("Column: $col")
		warn("Error: $error")

		val userIdentification = m.userIdentification
		val currentRoute = m.currentRoute
		val currentRouteClazz = currentRoute?.let { it::class.simpleName }

		val content = buildString {
			this.append("Message: $message")
			this.append("\n")
			this.append("File: $file ($line;$col)")
			this.append("\n")
			this.append("User Agent: ${window.navigator.userAgent}")
			this.append("\n")
			this.append("URL: ${window.location.href} (Spicy Path: ${m.currentPath}; Locale ID: ${m.localeId}; Locale Initialized? ${isLocaleInitialized})")
			this.append("\n")
			this.append("User Identification: ")
			if (userIdentification != null)
				this.append("${userIdentification.username}#${userIdentification.discriminator} (${userIdentification.id})")
			else
				this.append("Unknown")
			this.append("\n")
			this.append("Current Route: ${currentRouteClazz ?: "Unknown"}")
			this.append("\n")
			this.append("\n")
			this.append("Stack:")
			this.append("\n")
			this.append("${error.asDynamic().stack}")
		}

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
		stringBuilder.appendHTML().div {
			div {
				style = "text-align: center;"

				img(src = "https://loritta.website/assets/img/fanarts/l4.png") {
					width = "250"
				}

				h1 {
					+SOMETHING_WENT_WRONG
				}
				p {
					locale.buildAsHtml(WHAT_SHOULD_I_DO, { control ->
						if (control == 0) {
							a(href = "/support") {
								+MY_SUPPORT_SERVER
							}
						}
					}, { str ->
						+ str
					})
				}
				p {
					+SORRY_FOR_THE_INCONVENIENCE
				}
				p {
					locale.buildAsHtml(ERROR_CODE_ID, { control ->
						if (control == 0) {
							span(classes = "error-code-id") {
								+"..."
							}
						}
					}, { str ->
						+ str
					})
				}
			}
			pre {
				style = "word-wrap: break-word; white-space: pre-wrap;"
				+content
			}
		}

		modal.setContent(stringBuilder.toString())
		modal.open()
		modal.trackOverflowChanges(m)

		if (!isAlreadySending) {
			debug("Sending stacktrace to Loritta...")
			// Para evitar que vários erros fiquem spammando o console
			m.launch {
				val body = http.post<String>("${window.location.origin}/api/v1/loritta/error/spicy") {
					body = JSON.stringify(
							object {
								val message: String = message
								val spicyHash = selfHash
								val file: String = file
								val line = line
								val column = col
								val userAgent: String = window.navigator.userAgent
								val url: String = window.location.href
								val spicyPath: String? = m.currentPath
								val localeId: String = m.localeId
								val isLocaleInitialized: Boolean = this@ErrorTracker.isLocaleInitialized
								val userId = userIdentification?.id
								val currentRoute: String? = currentRouteClazz
								val stack: String? = error.asDynamic().stack
							}
					)
				}
				val result = JSON.parse<Json>(body)
				debug("Stacktrace sent!")
				isAlreadySending = false
				visibleModal.select<HTMLSpanElement?>(".error-code-id")?.innerText = result["errorCodeId"]?.toString() ?: "Failed to send error"
			}
		} else {
			warn("Error detected, but client is already sending a stacktrace! ...bug?")
		}

		isAlreadySending = true
	}
}