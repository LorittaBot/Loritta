package net.perfectdreams.spicymorenitta.trunfo

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import net.perfectdreams.spicymorenitta.utils.Audio
import net.perfectdreams.spicymorenitta.utils.onDOMReady
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document

object TrunfoGame {
	var nintendoWfc = Audio("nintendo_wfc.mp3")

	fun start() {
		document.onDOMReady {
			Trunfo.currentPopup = openPopup {
				h1 {
					+"Lori's Super Trunfo™"
				}

				div {
					+"Seu Nome: "
					input {
						id = "player-name-input"
					}
				}

				button {
					+"Conectar em uma sala!"

					onClickFunction = {
						nintendoWfc.volume = 0.03
						nintendoWfc.loop = true
						nintendoWfc.play()

						val playerName = document.select<HTMLInputElement>("#player-name-input").value

						Trunfo.connectToServer(playerName)
					}
				}
			}
		}
	}

	fun openPopup(html: DIV.() -> (Unit)): HTMLElement {
		val div = document.create.div(classes = "popup-wrapper") {
			div(classes = "popup") {
				html.invoke(this)
			}
		}

		document.body!!.append(div)
		return div
	}
}