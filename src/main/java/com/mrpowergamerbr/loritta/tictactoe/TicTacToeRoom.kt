package com.mrpowergamerbr.loritta.tictactoe

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.utils.lorittaShards
import org.jooby.Session
import org.jooby.WebSocket

class TicTacToeRoom {
	val initializedAt = System.currentTimeMillis()
	var player1: String? = null
	var player2: String? = null
	var winner: String? = null
	@Transient
	var player1WebSocket: WebSocket? = null
	@Transient
	var player2WebSocket: WebSocket? = null
	var currentPlayer: String? = null
	var playfield = intArrayOf(
			0, 0, 0,
			0, 0, 0,
			0, 0, 0
	)
	var gameStatus = GameStatus.WAITING_FOR_PLAYERS

	fun join(userId: String, webSocket: WebSocket) {
		println("Player $userId connected to a room!")
		if (player1 == null) {
			player1WebSocket = webSocket
			player1 = userId
		} else {
			player2WebSocket = webSocket
			player2 = userId
		}

		if (player1 != null && player2 != null) {
			gameStatus = GameStatus.CONTINUE
			currentPlayer = if (RANDOM.nextBoolean()) {
				player1
			} else {
				player2
			}
		}
	}

	fun makeMove(position: Int, player: String): MovementStatus {
		if (gameStatus != GameStatus.CONTINUE)
			return MovementStatus.INVALID_STATE

		if (currentPlayer != player) {
			// Movimento inválido (player errado)
			return MovementStatus.WRONG_PLAYER
		}

		if (position !in 0..8) {
			// Movimento inválido (fora do tabuleiro)
			return MovementStatus.OUTSIDE_OF_PLAYFIELD
		}

		val cell = playfield[position]

		if (cell != 0) {
			// Movimento inválido (cédula já ocupada)
			return MovementStatus.ALREADY_OCCUPIED_CELL
		}

		val option = if (currentPlayer == player1) {
			1
		} else {
			2
		}

		playfield[position] = option

		var won = false
		// Agora vem a hora de verdade:tm:, verificar se alguém ganhou

		// X | X | X
		//   |   |
		//   |   |
		if (playfield[0] == option && playfield[1] == option && playfield[2] == option) {
			won = true
		}

		//   |   |
		// X | X | X
		//   |   |
		if (playfield[3] == option && playfield[4] == option && playfield[5] == option) {
			won = true
		}

		//   |   |
		//   |   |
		// X | X | X
		if (playfield[6] == option && playfield[7] == option && playfield[8] == option) {
			won = true
		}

		// X |   |
		// X |   |
		// X |   |
		if (playfield[0] == option && playfield[3] == option && playfield[6] == option) {
			won = true
		}

		//   | X |
		//   | X |
		//   | X |
		if (playfield[1] == option && playfield[4] == option && playfield[7] == option) {
			won = true
		}

		//   |   | X
		//   |   | X
		//   |   | X
		if (playfield[2] == option && playfield[5] == option && playfield[8] == option) {
			won = true
		}

		// X |   |
		//   | X |
		//   |   | X
		if (playfield[0] == option && playfield[4] == option && playfield[8] == option) {
			won = true
		}

		//   |   | X
		//   | X |
		// X |   |
		if (playfield[2] == option && playfield[4] == option && playfield[6] == option) {
			won = true
		}

		var draw = !playfield.contains(0)

		if (won) {
			gameStatus = GameStatus.CURRENT_PLAYER_WON
			winner = currentPlayer
		} else if (draw) {
			gameStatus = GameStatus.DRAW
		} else {
			if (currentPlayer == player1) {
				currentPlayer = player2
			} else {
				currentPlayer = player1
			}
		}

		return MovementStatus.SUCCESS
	}

	fun getRoomOverview(ws: WebSocket, session: Session): JsonObject {
		val gson = GsonBuilder().setPrettyPrinting().create()

		val roomAsJson = gson.toJsonTree(
				this
		).obj

		if (player1 != null) {
			val user = lorittaShards.retrieveUserById(player1)

			if (user != null) {
				roomAsJson["player1Metadata"] = jsonObject(
						"username" to user.name,
						"discriminator" to user.discriminator,
						"avatarUrl" to user.effectiveAvatarUrl
				)
			}
		}

		if (player2 != null) {
			val user = lorittaShards.retrieveUserById(player2)

			if (user != null) {
				roomAsJson["player2Metadata"] = jsonObject(
						"username" to user.name,
						"discriminator" to user.discriminator,
						"avatarUrl" to user.effectiveAvatarUrl
				)
			}
		}

		roomAsJson["api:code"] = LoriWebCodes.SUCCESS
		roomAsJson["api:type"] = "ROOM_OVERVIEW"
		roomAsJson["yourClientId"] = ws.get("clientIdTicTacToe")

		println(roomAsJson)

		return roomAsJson
	}

	enum class MovementStatus {
		WRONG_PLAYER,
		OUTSIDE_OF_PLAYFIELD,
		ALREADY_OCCUPIED_CELL,
		SUCCESS,
		INVALID_STATE
	}

	enum class GameStatus {
		WAITING_FOR_PLAYERS,
		CONTINUE,
		CURRENT_PLAYER_WON,
		DRAW
	}
}