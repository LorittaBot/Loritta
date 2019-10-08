package net.perfectdreams.loritta.trunfo

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import org.jooby.WebSocket
import kotlin.concurrent.thread

class Game {
	var player1: Player? = null
	var player2: Player? = null
	var currentPlayer: Player? = null
	var status = Status.UNKNOWN
	val availableCards = mutableListOf(
			Card(
					"Loritta Morenitta",
					"https://cdn.discordapp.com/emojis/626942886432473098.png?v=1",

					170,
					50,
					16,
					75,
					0,
					0,
					97
			),
			Card(
					"Pantufa",
					"https://cdn.discordapp.com/emojis/626942886583205898.png?v=1",


					166,
					45,
					15,
					65,
					0,
					0,
					40
			),
			Card(
					"Gabriela",
					"https://cdn.discordapp.com/icons/602640402830589954/bae9a004d70279a54280de9ebc9ad65e.png?size=256",

					174,
					66,
					17,
					55,
					0,
					0,
					50
			),
			Card(
					"Dokyo Inuyama",
					"https://cdn.discordapp.com/emojis/603389181808607262.png?v=1",

					168,
					50,
					13,
					75,
					0,
					0,
					65
			),
			Card(
					"Gessy",
					"https://cdn.discordapp.com/emojis/593907632784408644.png?v=1",

					180,
					52,
					14,
					70,
					0,
					0,
					70
			),
			Card(
					"Kaike Carlos",
					"https://cdn.discordapp.com/avatars/123231508625489920/4cd2ed17d932605a6c9cef5fdf13e96d.png?size=256",

					148,
					28,
					17,
					53,
					0,
					0,
					37
			),
			Card(
					"Tobias",
					"https://cdn.discordapp.com/emojis/450476856303419432.png?v=1",

					30,
					4,
					6,
					78,
					0,
					0,
					67
			),
			Card(
					"Gato da Raça Meninas de 14 Anos",
					"https://cdn.discordapp.com/emojis/585536245891858470.png?v=1",

					25,
					5,
					14,
					35,
					0,
					0,
					74
			),
			Card(
					"Ricardo Milos",
					"https://cdn.discordapp.com/emojis/606162761386557464.gif?v=1",

					172,
					86,
					42,
					90,
					0,
					0,
					90
			),
			Card(
					"Sheldo",
					"https://cdn.discordapp.com/attachments/617182204212150316/630525989176213514/unknown.png",

					120,
					20,
					10,
					58,
					0,
					0,
					27
			),
			Card(
					"Moletom da Lori",
					"https://cdn.discordapp.com/emojis/623670395907866625.png?v=1",

					43,
					1,
					3,
					78,
					0,
					0,
					100
			),
			Card(
					"Gessy após ter soltado um barro",
					"https://cdn.discordapp.com/emojis/590716264528347161.png?v=1",

					180,
					48,
					14,
					85,
					0,
					0,
					63
			),
			Card(
					"Urso sem nome",
					"https://cdn.discordapp.com/attachments/297732013006389252/630561850521681920/urso.png",

					180,
					85,
					23,
					53,
					0,
					0,
					20
			),
			Card(
					"Chocoholic",
					"https://cdn.discordapp.com/attachments/358774895850815488/630561783807213579/Sweet_Land_Pet.png",

					75,
					7,
					3,
					67,
					0,
					0,
					57
			)
	)

	fun processSelection(str: String) {
		val attr = when (str) {
			"height" -> Card::height
			"weight" -> Card::weight
			"age" -> Card::age
			"power" -> Card::power
			"fame" -> Card::fame
			else -> throw RuntimeException("oof, $str is invalid")
		}

		val oppositePlayer = getOppositePlayer()

		val currentPlayer = currentPlayer!!
		val currentCard = currentPlayer!!.cards.first()
		val currentCard2 = oppositePlayer.cards.first()

		val cardAttr = attr.call(currentCard)
		val cardAttr2 = attr.call(currentCard2)

		if (cardAttr == cardAttr2) {
			// empate!
			// oh no, empate!
			currentPlayer.cards.remove(currentCard)
			currentPlayer.cards.add(currentCard)

			oppositePlayer.cards.remove(currentCard2)
			oppositePlayer.cards.add(currentCard2)

			this.currentPlayer = null
			send(
					currentPlayer,
					jsonObject(
							"status" to Status.SEND_ROUND_STATS.toString(),
							"whatHappened" to "TIE"
					)
			)
			send(
					oppositePlayer,
					jsonObject(
							"status" to Status.SEND_ROUND_STATS.toString(),
							"whatHappened" to "TIE"
					)
			)
		} else if (cardAttr > cardAttr2) {
			// ganhamos, yay!
			currentPlayer.cards.add(currentCard2)
			oppositePlayer.cards.remove(currentCard2)

			currentPlayer.cards.remove(currentCard)
			currentPlayer.cards.add(currentCard)

			send(
					currentPlayer,
					jsonObject(
							"status" to Status.SEND_ROUND_STATS.toString(),
							"whatHappened" to "YOU_WON",
							"withWhatStats" to str,
							"opponentCard" to convertCardToJsonObject(currentCard2)
					)
			)
			send(
					oppositePlayer,
					jsonObject(
							"status" to Status.SEND_ROUND_STATS.toString(),
							"whatHappened" to "YOU_LOST",
							"withWhatStats" to str,
							"opponentCard" to convertCardToJsonObject(currentCard)
					)
			)
		} else if (cardAttr2 > cardAttr) {
			// frick, perdemos!
			oppositePlayer.cards.add(currentCard)
			currentPlayer.cards.remove(currentCard)

			oppositePlayer.cards.remove(currentCard2)
			oppositePlayer.cards.add(currentCard2)

			this.currentPlayer = oppositePlayer

			send(
					currentPlayer,
					jsonObject(
							"status" to Status.SEND_ROUND_STATS.toString(),
							"whatHappened" to "YOU_LOST",
							"withWhatStats" to str,
							"opponentCard" to convertCardToJsonObject(currentCard2)
					)
			)
			send(
					oppositePlayer,
					jsonObject(
							"status" to Status.SEND_ROUND_STATS.toString(),
							"whatHappened" to "YOU_WON",
							"withWhatStats" to str,
							"opponentCard" to convertCardToJsonObject(currentCard)
					)
			)
		}

		thread {
			Thread.sleep(3_000)
			start()
		}
	}

	fun join(name: String, ws: WebSocket) {
		ws.onMessage {
			println("Received message within Game.kt! ${it.value()}")

			val parsed = jsonParser.parse(it.value())
			val selected = parsed["selected"].string

			processSelection(selected)
		}

		val cardsForNewPlayer = mutableListOf<Card>()

		repeat(7) {
			val card = availableCards.random()

			availableCards.remove(card)

			cardsForNewPlayer.add(card)
		}

		if (player1 == null) {
			player1 = Player(name, ws, cardsForNewPlayer)
		} else if (player2 == null) {
			player2 = Player(name, ws, cardsForNewPlayer)
		}

		if (player1 != null && player2 != null) {
			start()
			return
		}

		if (player1 == null || player2 == null) {
			updateStatus(Status.WAITING_FOR_PLAYERS)
		}
	}

	fun start() {
		broadcast(
				jsonObject(
						"status" to Status.SET_PLAYER_NAMES.toString(),
						"player1" to player1!!.name,
						"player2" to player2!!.name
				)
		)

		updateStatus(Status.PLAYING)

		if (this.currentPlayer == null) {
			currentPlayer = listOf(player1, player2).random()
		}
		val currentPlayer = currentPlayer!!

		val oppositePlayer = getOppositePlayer()

		if (currentPlayer.cards.isEmpty()) {
			send(
					currentPlayer,
					jsonObject(
							"status" to Status.YOU_LOST.toString()
					)
			)
			send(
					oppositePlayer,
					jsonObject(
							"status" to Status.YOU_WON.toString()
					)
			)
			return
		}

		if (oppositePlayer.cards.isEmpty()) {
			send(
					currentPlayer,
					jsonObject(
							"status" to Status.YOU_WON.toString()
					)
			)
			send(
					oppositePlayer,
					jsonObject(
							"status" to Status.YOU_LOST.toString()
					)
			)
			return
		}

		val currentCard = currentPlayer.cards.first()
		send(
				currentPlayer,
				jsonObject(
						"isMyTurn" to true,
						"howManyCards" to currentPlayer.cards.size,
						"howManyOpponentCards" to oppositePlayer.cards.size,

						"currentCard" to convertCardToJsonObject(currentCard)
				)
		)

		val currentCard2 = oppositePlayer.cards.first()
		send(
				oppositePlayer,
				jsonObject(
						"isMyTurn" to false,
						"howManyCards" to oppositePlayer.cards.size,
						"howManyOpponentCards" to currentPlayer.cards.size,

						"currentCard" to convertCardToJsonObject(currentCard2)
				)
		)
	}

	fun broadcast(json: JsonObject) {
		val str = gson.toJson(json)

		player1?.ws?.send(
				str
		)
		player2?.ws?.send(
				str
		)
	}

	fun send(player: Player, json: JsonObject) {
		val str = gson.toJson(json)

		try {
			player?.ws?.send(
					str
			)
		} catch (e: Exception) {
			println("Looks like something went wrong!")
			e.printStackTrace()

			try {
				player1?.ws?.send(
						jsonObject(
								"status" to Status.CLOSED.toString()
						)
				)
			} catch (e: Exception) {}

			try {
				player2?.ws?.send(
						jsonObject(
								"status" to Status.CLOSED.toString()
						)
				)
			} catch (e: Exception) {}
		}
	}

	fun updateStatus(status: Game.Status) {
		this.status = status

		broadcast(
				jsonObject(
						"status" to status.toString()
				)
		)
	}

	fun getOppositePlayer(): Player {
		if (currentPlayer == player1)
			return player2!!

		return player1!!
	}

	fun convertCardToJsonObject(card: Card) = jsonObject(
			"name" to card.name,
			"imageUrl" to card.imageUrl,
			"age" to card.age,
			"weight" to card.weight,
			"height" to card.height,
			"power" to card.power,
			"fame" to card.fame
	)

	fun cleanUp() {
		try {
			player1?.ws?.send(
					jsonObject(
							"status" to Status.CLOSED.toString()
					)
			)
		} catch (e: Exception) {}

		try {
			player2?.ws?.send(
					jsonObject(
							"status" to Status.CLOSED.toString()
					)
			)
		} catch (e: Exception) {}
	}

	enum class Status {
		UNKNOWN,
		WAITING_FOR_PLAYERS,
		SET_PLAYER_NAMES,
		SEND_ROUND_STATS,
		PLAYING,
		YOU_WON,
		YOU_LOST,
		CLOSED,
		FINISHED
	}
}