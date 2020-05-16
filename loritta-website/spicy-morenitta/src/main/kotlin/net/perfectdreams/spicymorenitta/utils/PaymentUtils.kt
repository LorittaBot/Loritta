package net.perfectdreams.spicymorenitta.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import utils.TingleModal
import utils.TingleOptions
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Json

object PaymentUtils : Logging {
	/**
	 * Opens the payment selection modal
	 */
	fun openPaymentSelectionModal(meta: dynamic, url: String = "${loriUrl}api/v1/users/donate") {
		debug("Opening payment selection modal...")
		console.log(meta)

		val modal = TingleModal(
				TingleOptions(
						footer = true,
						cssClass = arrayOf("tingle-modal--overflow")
				)
		)

		modal.setContent(
				document.create.div {
					div(classes = "category-name") {
						+ "Selecione o método de pagamento!"
					}

					div {
						style = "text-align: center;"

						img(src = "https://i.imgur.com/wEUDTZG.png") {
							width = "250"
						}

						div(classes = "button-discord button-discord-info pure-button") {
							style = "font-size: 1.25em; margin: 5px;"
							+ "MercadoPago (Boleto, Cartão de Crédito e Saldo do MercadoPago)"

							onClickFunction = {
								meta.gateway = "MERCADOPAGO"

								sendPaymentRequest(meta, url)
							}
						}

						div(classes = "button-discord button-discord-info pure-button") {
							style = "font-size: 1.25em; margin: 5px;"
							+ "PicPay (Cartão de Crédito e Saldo do PicPay)"

							onClickFunction = {
								meta.gateway = "PICPAY"

								modal.close()

								openPicPayUserInformationModal(meta, url)
							}
						}

						p {
							+ "Se você deseja doar via PayPal, contate "
							code {
								+ "MrPowerGamerBR#4185"
							}
							+ " no meu servidor de suporte!"
						}
					}
				}
		)

		modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			modal.close()
		}

		modal.open()
	}

	/**
	 * Opens a modal asking for the user's personal information
	 */
	private fun openPicPayUserInformationModal(meta: dynamic, url: String) {
		val modal = TingleModal(
				TingleOptions(
						footer = true,
						cssClass = arrayOf("tingle-modal--overflow")
				)
		)

		modal.setContent(
				document.create.div {
					div(classes = "category-name") {
						+ "Informação pessoais para o PicPay"
					}

					div {
						style = "text-align: center;"

						div {
							+ "Nome"
						}
						input(InputType.text, classes = "first-name") {
							placeholder = "Loritta"
						}

						div {
							+ "Sobrenome"
						}
						input(InputType.text, classes = "last-name") {
							placeholder = "Morenitta"
						}

						div {
							+ "Email"
						}
						input(InputType.text, classes = "email") {
							placeholder = "me@loritta.website"
						}

						div {
							+ "CPF"
						}
						input(InputType.text, classes = "document") {
							placeholder = "111.222.333-45"
						}

						div {
							+ "Número de Telefone"
						}
						input(InputType.text, classes = "phone") {
							placeholder = "+11 40028922"
						}
						
						div {
							b {
								+ "Atenção: "
							}
							+ "As suas informações serão utilizadas pelo PicPay para o processamento de pagamentos. A Loritta não irá salvar os seus dados pessoais."
						}
					}
				}
		)

		modal.addFooterBtn("<i class=\"fas fa-dollar-sign\"></i> Pagar", "button-discord button-discord-info pure-button button-discord-modal") {
			meta.firstName = visibleModal.select<HTMLInputElement>(".first-name").value
			meta.lastName = visibleModal.select<HTMLInputElement>(".last-name").value
			meta.email = visibleModal.select<HTMLInputElement>(".email").value
			meta.document = visibleModal.select<HTMLInputElement>(".document").value
			meta.phone = visibleModal.select<HTMLInputElement>(".phone").value

			sendPaymentRequest(meta, url)
		}

		modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
			modal.close()
		}

		modal.open()
	}

	/**
	 * Sends a payment request to Loritta and, if valid, redirects the user
	 */
	private fun sendPaymentRequest(meta: dynamic, url: String) {
		println(JSON.stringify(meta))

		GlobalScope.launch {
			val response = HttpRequest.post(url, JSON.stringify(meta))

			val payload = JSON.parse<Json>(response.body)
			window.location.href = payload["redirectUrl"] as String
		}
	}
}