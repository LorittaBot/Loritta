package net.perfectdreams.spicymorenitta.trunfo

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import net.perfectdreams.spicymorenitta.utils.Audio
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.WebSocket
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass
import kotlin.js.Json

object Trunfo : Logging {
	var currentStatus: String = "UNKNOWN"
	lateinit var ws: WebSocket
	var currentPopup: HTMLElement? = null
	var player1Name: String = "???"
	var player1Avatar: String = "???"
	var player2Name: String = "???"
	var player2Avatar: String = "???"
	var isMyTurn: Boolean = false
	var errou = Audio("faustao_errou.mp3")
	var dogResidue = Audio("dog_residue.mp3")

	@JsName("connectToServer")
	fun connectToServer() {
		currentPopup?.remove()
		currentPopup = TrunfoGame.openPopup {
			img(src = "https://cdn.discordapp.com/emojis/621886615899471891.gif?v=1") {}

			div {
				+ "Conectando ao Matchmaking..."
			}
		}

		ws = WebSocket("wss://" +  window.location.hostname + "/ws")

		ws.onopen = {
			currentPopup?.remove()
			currentPopup = TrunfoGame.openPopup {
				img(src = "https://cdn.discordapp.com/emojis/621886615899471891.gif?v=1") {}

				div {
					+ "Autenticando..."
				}
			}

			ws.send(
					JSON.stringify(
							object {
								val status = "JOIN_MATCHMAKING"
							}
					)
			)
		}

		ws.onmessage = {
			val payload = it.data as String

			debug(payload)

			val json = JSON.parse<Json>(payload)

			// document.select<HTMLDivElement>("#connection-data").innerHTML = payload

			val _currentStatus = json["status"] as String?

			if (_currentStatus != null) {
				currentStatus = _currentStatus
			}

			// document.select<HTMLDivElement>("#status-data").innerHTML = currentStatus

			if (currentStatus == "PING") {
				debug("Ping received... pong!")
				ws.send(
						JSON.stringify(object {
							var status = "PONG"
						})
				)
			} else if (currentStatus == "UNAUTHORIZED") {
				currentPopup?.remove()
				currentPopup = TrunfoGame.openPopup {
					div {
						+ "Não autorizado, redirecionando..."
					}
				}

				window.location.replace("https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=identify+guilds+email+guilds.join&permissions=2080374975&response_type=code&redirect_uri=https://loritta.website/dashboard&state=eyJyZWRpcmVjdFVybCI6Imh0dHBzOi8vdHJ1bmZvLmxvcml0dGEud2Vic2l0ZS9pbmRleF9rb3RsaW4uaHRtbCJ9");
			} else if (currentStatus == "CLOSED") {
				currentPopup?.remove()
				currentPopup = TrunfoGame.openPopup {
					div {
						+ "Sala fechada, talvez o seu amiguchx tenha saido da sala..."
					}
				}
			} else if (currentStatus == "WAITING_FOR_PLAYERS") {
				currentPopup?.remove()
				currentPopup = TrunfoGame.openPopup {
					img(src = "https://cdn.discordapp.com/emojis/621886615899471891.gif?v=1") {}

					div {
						+ "Esperando por jogadores..."
					}
				}
			} else if (currentStatus == "YOU_WON") {
				document.select<HTMLElement>("#waiting-for-something").innerHTML = "Você venceu o jogo! Parabéns ^-^"
			} else if (currentStatus == "YOU_LOST") {
				document.select<HTMLElement>("#waiting-for-something").innerHTML = "Você perdeu o jogo... Mas obrigado por jogar! ;w;"
			} else if (currentStatus == "PLAYING") {
				currentPopup?.remove()
				debug(document.select<HTMLElement?>("#game"))

				if (document.select<HTMLElement?>("#game") == null)
					buildStage()

				document.select<HTMLElement>("#opponent-card").addClass("blurred")

				document.select<HTMLElement>("#your-card .age-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#opponent-card .age-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#your-card .height-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#opponent-card .height-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#your-card .weight-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#opponent-card .weight-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#your-card .power-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#opponent-card .power-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#your-card .fame-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#opponent-card .fame-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#your-card .intelligence-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#opponent-card .intelligence-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#your-card .cuteness-entry").removeClass("pop-out", "green", "red")
				document.select<HTMLElement>("#opponent-card .cuteness-entry").removeClass("pop-out", "green", "red")


				document.select<HTMLElement>("#waiting-for-something").innerHTML = "Esperando..."

				var isMyTurn = json["isMyTurn"]
				var howManyCards = json["howManyCards"]
				var howManyOpponentCards = json["howManyOpponentCards"]
				document.select<HTMLElement>("#player-1-card-count").innerHTML = howManyCards.toString()
				document.select<HTMLElement>("#player-2-card-count").innerHTML = howManyOpponentCards.toString()
				var card = json["currentCard"] as Json
				fillCardInfo(card, document.select<HTMLElement>("#your-card"))

				document.select<HTMLElement>("#opponent-card .header").style.backgroundImage = "url('https://via.placeholder.com/128')"

				document.select<HTMLElement>("#opponent-card .card-name").innerHTML = "Segredo uwu"
				document.select<HTMLElement>("#opponent-card .age-card").innerHTML = "???"
				document.select<HTMLElement>("#opponent-card .height-card").innerHTML = "???"
				document.select<HTMLElement>("#opponent-card .weight-card").innerHTML = "???"
				document.select<HTMLElement>("#opponent-card .power-card").innerHTML = "???"
				document.select<HTMLElement>("#opponent-card .cuteness-card").innerHTML = "???"
				document.select<HTMLElement>("#opponent-card .intelligence-card").innerHTML = "???"
				document.select<HTMLElement>("#opponent-card .fame-card").innerHTML = "???"

				toggleDisabledStatus(isMyTurn as Boolean)
			} else if (currentStatus == "SET_PLAYER_NAMES") {
				player1Name = (json["player1"] as String)
				player1Avatar = (json["player1Avatar"] as String)
				player2Name = (json["player2"] as String)
				player2Avatar = (json["player2Avatar"] as String)
			} else if (currentStatus == "SEND_ROUND_STATS") {
				var whatHappened = json["whatHappened"]

				if (whatHappened == "TIE") {
					document.select<HTMLElement>("#waiting-for-something").innerHTML = "Empate..."
				}
				if (whatHappened == "YOU_WON") {
					document.select<HTMLElement>("#waiting-for-something").innerHTML = "Você ganhou a rodada!"

					var card = json["opponentCard"] as Json
					fillCardInfo(card, document.select<HTMLElement>("#opponent-card"))

					var withWhatStats = json["withWhatStats"]
					console.log(withWhatStats)

					document.select<HTMLElement>("#opponent-card ." + withWhatStats + "-entry").addClass("pop-out green")
					document.select<HTMLElement>("#your-card ." + withWhatStats + "-entry").addClass("pop-out green")

					dogResidue.play()

					document.select<HTMLElement>("#opponent-card").removeClass("blurred")
				}
				if (whatHappened == "YOU_LOST") {
					document.select<HTMLElement>("#waiting-for-something").innerHTML = "Você perdeu a rodada..."

					var card = json["opponentCard"] as Json
					fillCardInfo(card, document.select<HTMLElement>("#opponent-card"))

					var withWhatStats = json["withWhatStats"]
					console.log(withWhatStats)

					document.select<HTMLElement>("#opponent-card ." + withWhatStats + "-entry").addClass("pop-out red")
					document.select<HTMLElement>("#your-card ." + withWhatStats + "-entry").addClass("pop-out red")

					errou.play()

					document.select<HTMLElement>("#opponent-card").removeClass("blurred")
				}
			}
		}

		ws.onclose = {
			window.alert("Conexão perdida...")
		}
	}

	fun fillCardInfo(json: Json, element: Element) {
		element.select<HTMLElement>(".header").style.backgroundImage = "url('${json["imageUrl"]}')"

		element.select<HTMLElement>(".card-name").innerHTML = json["name"] as String
		element.select<HTMLElement>(".age-card").innerHTML = (json["age"] as Int).toString()
		element.select<HTMLElement>(".height-card").innerHTML = (json["height"] as Int).toString()
		element.select<HTMLElement>(".weight-card").innerHTML = (json["weight"] as Int).toString()
		element.select<HTMLElement>(".power-card").innerHTML = (json["power"] as Int).toString()
		element.select<HTMLElement>(".cuteness-card").innerHTML = (json["cuteness"] as Int).toString()
		element.select<HTMLElement>(".intelligence-card").innerHTML = (json["intelligence"] as Int).toString()
		element.select<HTMLElement>(".fame-card").innerHTML = (json["fame"] as Int).toString()
	}

	fun toggleDisabledStatus(isMyTurn: Boolean) {
		this.isMyTurn = isMyTurn

		/* document.select<HTMLInputElement>("#height").disabled = !isMyTurn
		document.select<HTMLInputElement>("#age").disabled = !isMyTurn
		document.select<HTMLInputElement>("#weight").disabled = !isMyTurn
		document.select<HTMLInputElement>("#power").disabled = !isMyTurn
		document.select<HTMLInputElement>("#fame").disabled = !isMyTurn
		document.select<HTMLButtonElement>("#send-selection").disabled = !isMyTurn */

		if (isMyTurn) {
			document.select<HTMLElement>("#is-what-now").innerHTML = "Escolha um atributo que você ache que seja maior que a carta do seu oponente!"
		} else {
			document.select<HTMLElement>("#is-what-now").innerHTML = "Agora é a vez do seu oponente... torça que ele escolha um atributo que tenha um valor menor que o da sua carta!"
		}
	}

	fun buildStage() {
		document.body!!.append {
			div {
				id = "game"

				div {
					id = "top-bar"

					buildPlayerNavbar("1")
					buildPlayerNavbar("2")
				}

				div {
					id = "waiting-bar"
				}

				div(classes = "game-info") {
					h3 {
						id = "waiting-for-something"
					}
					p {
						id = "is-what-now"
					}
				}

				div(classes = "cards") {
					buildCard("your-card", true)
					buildCard("opponent-card", false)
				}
			}
		}
	}

	fun DIV.buildPlayerNavbar(playerCount: String) {
		div(classes = "player-entry") {
			img(src = if (playerCount == "1") player1Avatar else player2Avatar, classes = "bar-avatar") {}

			div(classes = "bar-name-and-stats") {
				div(classes = "bar-name") {
					id = "player-$playerCount-name"

					if (playerCount == "1") {
						+player1Name
					} else {
						+player2Name
					}
				}

				div(classes = "bar-stats") {
					span {
						id = "player-$playerCount-card-count"
						+ "X"
					}
					+ " cartas"
				}
			}
		}
	}

	fun DIV.buildCard(idName: String, setClick: Boolean) {
		div(classes = "card") {
			id = idName

			div(classes = "header") {}

			div(classes = "card-name") {
				+ "Nome da Carta"
			}

			div(classes = "entries") {
				div(classes = "entry height-entry") {
					if (setClick) {
						onClickFunction = {
							if (isMyTurn)
								sendSelectionData("height")
						}
					}

					div(classes = "name") {
						i(classes = "fas fa-sort-amount-up") {}

						+ " Altura"
					}
					div(classes = "value height-card") {
						+ "???"
					}
				}

				div(classes = "entry age-entry") {
					if (setClick) {
						onClickFunction = {
							if (isMyTurn)
								sendSelectionData("age")
						}
					}

					div(classes = "name") {
						i(classes = "fas fa-birthday-cake") {}

						+ " Idade"
					}
					div(classes = "value age-card") {
						+ "???"
					}
				}

				div(classes = "entry weight-entry") {
					if (setClick) {
						onClickFunction = {
							if (isMyTurn)
								sendSelectionData("weight")
						}
					}

					div(classes = "name") {
						i(classes = "fas fa-weight") {}

						+ " Peso"
					}
					div(classes = "value weight-card") {
						+ "???"
					}
				}

				div(classes = "entry power-entry") {
					if (setClick) {
						onClickFunction = {
							if (isMyTurn)
								sendSelectionData("power")
						}
					}

					div(classes = "name") {
						i(classes = "fas fa-fist-raised") {}

						+ " Poder"
					}
					div(classes = "value power-card") {
						+ "???"
					}
				}

				div(classes = "entry cuteness-entry") {
					if (setClick) {
						onClickFunction = {
							if (isMyTurn)
								sendSelectionData("cuteness")
						}
					}

					div(classes = "name") {
						i(classes = "fas fa-heart") {}

						+ " Fofura"
					}
					div(classes = "value cuteness-card") {
						+ "???"
					}
				}

				div(classes = "entry intelligence-entry") {
					if (setClick) {
						onClickFunction = {
							if (isMyTurn)
								sendSelectionData("intelligence")
						}
					}

					div(classes = "name") {
						i(classes = "fas fa-brain") {}

						+ " Inteligência"
					}
					div(classes = "value intelligence-card") {
						+ "???"
					}
				}

				div(classes = "entry fame-entry") {
					if (setClick) {
						onClickFunction = {
							if (isMyTurn)
								sendSelectionData("fame")
						}
					}

					div(classes = "name") {
						i(classes = "fas fa-star") {}

						+ " Fama"
					}
					div(classes = "value fame-card") {
						+ "???"
					}
				}
			}
		}
	}

	@JsName("sendSelectionData")
	fun sendSelectionData(selectedEntry: String) {
		ws.send(
				JSON.stringify(object {
					var status = "SELECTION"
					var selected = selectedEntry
				})
		)
	}
}