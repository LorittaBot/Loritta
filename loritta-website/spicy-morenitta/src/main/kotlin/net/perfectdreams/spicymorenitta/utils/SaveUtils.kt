package net.perfectdreams.spicymorenitta.utils

import LoriDashboard
import io.ktor.client.request.patch
import io.ktor.client.statement.readText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.TingleModal
import net.perfectdreams.spicymorenitta.utils.TingleOptions
import kotlin.js.Json
import kotlin.js.json

external val guildId: String

object SaveUtils {
	fun prepareSave(type: String, extras: ((payload: Json) -> Unit), showSaveScreen: Boolean = true, onFinish: ((HttpResponse) -> (Unit))? = null, endpoint: String = "${loriUrl}api/v1/guilds/$guildId/config") {
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
			val response = http.patch<io.ktor.client.statement.HttpResponse>(endpoint) {
				body = JSON.stringify(json)
			}
			val body = response.readText()

			LoriDashboard.hideLoadingBar()

			if (response.status.value !in 200..299) {
				LoriDashboard.configErrorSfx.play()

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
					+"Oopsie Woopsie! ${response.status.value} ${response.status.description}"
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
									+ "Erro ao salvar configuração!"
								}
								p {
									+"Infelizmente ninguém é perfeito... e pelo visto você encontrou um problema no meu website... Tente recarregar a página e veja se o problema persiste, caso persista, entre no meu <a href='${loriUrl}support'>servidor de suporte</a> e envie o código abaixo junto com uma pequena explicação sobre o que você estava tentando fazer no momento que deu o erro! As configurações possivelmente não foram salvas, então salve tudo que você modificou antes de sair da página!"
								}
								p {
									+ "Desculpe pela inconveniência..."
								}
								pre {
									style = "word-wrap: break-word; white-space: pre-wrap;"
									+ body
								}
							}
						}
					}
				}

				modal.setContent(stringBuilder.toString())
				modal.open()
			} else {
				onFinish?.invoke(HttpResponse(response.status.value,  body))

				LoriDashboard.configSavedSfx.play()
			}
		}
	}
}