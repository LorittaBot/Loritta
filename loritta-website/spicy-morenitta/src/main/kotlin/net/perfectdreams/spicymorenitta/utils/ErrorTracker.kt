package net.perfectdreams.spicymorenitta.utils

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.locale.BaseLocale
import kotlin.browser.window

object ErrorTracker : Logging {
	private val isLocaleInitialized: Boolean
		get() = ::locale.isInitialized

	private val SOMETHING_WENT_WRONG: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.somethingWentWrong"] else "Something went wrong..."

	private val WHAT_SHOULD_I_DO: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.whatShouldIDo"] else "Nothing is perfect... and looks like you found an issue on my website. Try reloading the page, update your browser and, if the issue persists, join {0} and send the code below with a small explanation of what you were trying to do at the moment the issue happened, so we can analyze and fix the issue!"

	private val SORRY_FOR_THE_INCONVENIENCE: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.sorryForTheInconvenience"] else "Sorry for the inconvenience, I hope I can fix this issue soon!"

	private val MY_SUPPORT_SERVER: String
		get() = if (isLocaleInitialized) locale["website.errorTracker.mySupportServer"] else "my support server"

	fun start(m: SpicyMorenitta) {

		debug("Starting Error Tracker...")

		window.onerror = callback@{ message: dynamic, file: String, line: Int, col: Int, error: Any? ->
			if (message.unsafeCast<String>().contains("adsbygoogle")) // AdSense
				return@callback false

			// Ao dar um erro, nós iremos mostrar uma mensagem legal para o usuário, para que seja mais fácil resolver problema
			warn("Error detected! Opening modal...")

			warn("Message: $message")
			warn("File: $file")
			warn("Line: $line")
			warn("Column: $col")
			warn("Error: $error")

			val userIdentification = m.userIdentification

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
				this.append("Current Route: ${m.currentRoute ?: "Unknown"}")
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
				div("pure-g vertically-centered-content") {
					div {
						style = "text-align: center;"

						img(src = "https://loritta.website/assets/img/fanarts/l4.png") {
							width = "250"
						}

						h1 {
							+SOMETHING_WENT_WRONG
						}
						p {
							// Gambiarra, a gente só quer usar o buildAsHtml do BaseLocale
							// Ele nem usa nenhuma das entries, então vamos apenas criar um dummy locale e utilizá-lo
							// No futuro seria melhor mover o buildAsHtml para um código separado
							val locale = BaseLocale("dummy", mutableMapOf())
							locale.buildAsHtml(WHAT_SHOULD_I_DO, { control ->
								if (control == 0) {
									a(href = "/support") {
										+MY_SUPPORT_SERVER
									}
								}
							}, { str ->
								+str
							})
						}
						p {
							+SORRY_FOR_THE_INCONVENIENCE
						}
					}
					pre {
						style = "word-wrap: break-word; white-space: pre-wrap;"
						+content
					}
				}
			}

			modal.setContent(stringBuilder.toString())
			modal.open()
			modal.trackOverflowChanges(m)
		}
		false
	}
}