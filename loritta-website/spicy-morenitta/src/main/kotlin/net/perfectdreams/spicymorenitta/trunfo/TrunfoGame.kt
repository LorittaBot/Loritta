package net.perfectdreams.spicymorenitta.trunfo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.js.onClickFunction
import net.perfectdreams.spicymorenitta.utils.Audio
import net.perfectdreams.spicymorenitta.utils.onDOMReady
import org.w3c.dom.HTMLElement
import kotlin.browser.document

object TrunfoGame {
	var nintendoWfc = Audio("nintendo_wfc.mp3")

	fun start() {
		document.onDOMReady {
			Trunfo.currentPopup = openPopup {
				h1 {
					+"Lori's Super Trunfo™"
				}

				button {
					+"Conectar em uma sala!"

					onClickFunction = {
						nintendoWfc.volume = 0.03
						nintendoWfc.loop = true
						nintendoWfc.play()

						GlobalScope.launch {
							Trunfo.connectToServer()
						}
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