package net.perfectdreams.spicymorenitta.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.create
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.JsonObject
import kotlin.js.Json

object PaymentUtils : Logging {
	/**
	 * Sends a payment request to Loritta and, if valid, redirects the user
	 */
	fun requestAndRedirectToPaymentUrl(meta: JsonObject, url: String = "${loriUrl}api/v1/users/donate") {
		val modal = TingleModal(
			jsObject<TingleOptions> {
				footer = true
				closeMethods = arrayOf()
			}
		)

		modal.setContent(
				document.create.div {
					div(classes = "category-name") {
						+ locale["website.donate.beforeBuyingTerms.title"]
					}

					div {
						p {
							+ locale["website.donate.beforeBuyingTerms.youAgreeTo"]
						}
						
						ul {
							for (entry in locale.getList("website.donate.beforeBuyingTerms.terms")) {
								li {
									+ entry
								}
							}
						}
					}
				}
		)

		modal.addFooterBtn("<i class=\"fas fa-dollar-sign\"></i> ${locale["website.donate.beforeBuyingTerms.agree"]}", "button-discord button-discord-info pure-button button-discord-modal") {
			modal.close()

			SpicyMorenitta.INSTANCE.showLoadingScreen()

			GlobalScope.launch {
				debug("Requesting a PerfectPayments payment URL...")
				val response = http.post(url) {
					setBody(meta.toString())
				}

				if (response.status == HttpStatusCode.Forbidden) {
					// Needs to enable MFA
					SpicyMorenitta.INSTANCE.hideLoadingScreen()

					val modal = TingleModal(
						jsObject<TingleOptions> {
							footer = true
							closeMethods = arrayOf()
						}
					)

					modal.setContent(
							document.create.div {
								div(classes = "category-name") {
									+ locale["website.donate.mfaDisabled.title"]
								}

								div {
									style = "text-align: center;"

									p {
										for (entry in locale.getList("website.donate.mfaDisabled.description")) {
											+ entry
										}
									}
								}
							}
					)

					modal.addFooterBtn("<i class=\"fas fa-times\"></i> ${locale["website.donate.mfaDisabled.close"]}", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
						modal.close()
					}

					modal.open()
					modal.trackOverflowChanges(SpicyMorenitta.INSTANCE)
				} else {
					val text = response.bodyAsText()
					val payload = JSON.parse<Json>(text)

					window.location.href = payload["redirectUrl"] as String
				}
			}
		}

		modal.addFooterBtn("<i class=\"fas fa-times\"></i> ${locale["modules.levelUp.resetXp.cancel"]}", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			modal.close()
		}

		modal.open()
		modal.trackOverflowChanges(SpicyMorenitta.INSTANCE)
	}
}