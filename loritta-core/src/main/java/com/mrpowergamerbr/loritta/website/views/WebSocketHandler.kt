package com.mrpowergamerbr.loritta.website.views

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.tictactoe.TicTacToeRoom
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import org.jooby.Mutant
import org.jooby.Session
import org.jooby.WebSocket

object WebSocketHandler {
	fun onSocketConnected(ws: WebSocket, session: Session) {
		println("Socket connected to my WebSocket!")
		// owo, socket connected!
	}

	fun onMessageReceived(ws: WebSocket, mutant: Mutant, session: Session) {
		println(mutant.value())

		val json = jsonParser.parse(mutant.value()).obj

		val type = json["type"].string

		try {
			if (type == "join_tictactoe_game") {
				val roomId = json["roomId"].string

				val room = loritta.ticTacToeServer.rooms.entries.firstOrNull { it.key == roomId }?.value

				// Se a sala não existe, vamos retornar um erro para o usuário
				if (room == null) {
					ws.send(
							jsonObject(
									"api:code" to LoriWebCodes.INVALID_ROOM
							).toString()
					)
					return
				}

				// Se existe, vamos verificar se a sala não está cheia...
				if (room.player1 != null && room.player2 != null) {
					ws.send(
							jsonObject(
									"api:code" to LoriWebCodes.ROOM_IS_FULL
							).toString()
					)
					return
				}

				if (!session.isSet("discordAuth")) {
					ws.send(jsonObject("api:code" to LoriWebCodes.UNAUTHORIZED).toString())
					return
				}

				val userIdentification = try {
					val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(session["discordAuth"].value())
					discordAuth.isReady(true)
					discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
				} catch (e: Exception) {
					ws.send(jsonObject("api:code" to LoriWebCodes.UNAUTHORIZED).toString())
					return
				}

				ws.set("clientIdTicTacToe", userIdentification.id)

				// Se existe, vamos verificar se o jogador já não está na sala
				if (room.player1 == userIdentification.id || room.player2 == userIdentification.id) {
					ws.send(
							jsonObject(
									"api:code" to LoriWebCodes.ALREADY_INSIDE_THIS_ROOM
							).toString()
					)
					return
				}

				room.join(userIdentification.id, ws)

				ws.set("currentTicTacToeRoom", roomId)

				println("owo what is the current room? ${ws.get<String>("currentTicTacToeRoom")}")
				if (room.gameStatus == TicTacToeRoom.GameStatus.CONTINUE) {
					val player1Json = room.getRoomOverview(room.player1WebSocket!!, session).toString()
					val player2Json = room.getRoomOverview(room.player2WebSocket!!, session).toString()

					println("Sending ${player1Json} to Player 1")
					println("Sending ${player2Json} to Player 2")

					room.player1WebSocket!!.send(
							player1Json
					)

					room.player2WebSocket!!.send(
							player2Json
					)
				} else {
					// Caso não esteja, vamos enviar um payload com todas as informações da sala atual para o usuário!
					ws.send(
							room.getRoomOverview(ws, session).toString()
					)
				}
				return
			}
			if (type == "tictactoe_debug") {
				ws.attributes().forEach { t, u ->
					println("attr: $t - $u")
				}

				println("Debug info:")
				val roomId = ws.get<String>("currentTicTacToeRoom")
				println("Room ID: $roomId")
				val clientId = ws.get<String>("clientIdTicTacToe")
				println("Client ID: $clientId")
			}
			if (type == "make_tictactoe_move") {
				println("Received TicTacToe movement!")
				val roomId = ws.get<String>("currentTicTacToeRoom")
				val clientId = ws.get<String>("clientIdTicTacToe")
				println("RoomID: ${roomId}!")
				println("Moved by: ${clientId}"
				)
				if (roomId == null) {
					println("Invalid room!")
					ws.send(
							jsonObject(
									"api:code" to LoriWebCodes.INVALID_ROOM
							).toString()
					)
					return
				}

				val room = loritta.ticTacToeServer.rooms.entries.firstOrNull { it.key == roomId }?.value

				if (room == null) {
					ws.send(
							jsonObject(
									"api:code" to LoriWebCodes.INVALID_ROOM
							).toString()
					)
					return
				}

				val cellPosition = json["cell_position"].int

				val status = room.makeMove(cellPosition, ws.get("clientIdTicTacToe"))

				if (status == TicTacToeRoom.MovementStatus.SUCCESS) {
					val player1Json = room.getRoomOverview(room.player1WebSocket!!, session).toString()
					val player2Json = room.getRoomOverview(room.player2WebSocket!!, session).toString()

					println("Sending ${player1Json} to Player 1")
					println("Sending ${player2Json} to Player 2")

					room.player1WebSocket!!.send(
							player1Json
					)

					room.player2WebSocket!!.send(
							player2Json
					)
				} else {
					println("oh whoops, broken movement!")
					ws.send(
							jsonObject(
									"api:code" to LoriWebCodes.MOVEMENT_DONE,
									"movement_status" to status.name,
									"playfield" to gson.toJsonTree(room.playfield),
									"player1" to room.player1,
									"player2" to room.player2,
									"currentPlayer" to room.currentPlayer,
									"initializedAt" to room.initializedAt
							).toString()
					)
				}

				return
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

		ws.send(
				jsonObject(
						"api:code" to LoriWebCodes.MISSING_PAYLOAD_HANDLER,
						"missing_payload_handler" to type
				).toString()
		)
	}

	fun onSocketClose(ws: WebSocket, session: Session) {
		val roomId = ws.ifGet<String>("currentTicTacToeRoom")
		if (roomId.isPresent) {
			val discordId = ws.get<String>("clientIdTicTacToe")
			val room = loritta.ticTacToeServer.rooms.entries.firstOrNull { it.key == roomId.get() }?.value ?: return

			if (room.player2 == null && room.player1 == discordId) {
				room.player1 = null
				room.player1WebSocket = null
				return
			}

			if (room.player2 != null && room.player1 == discordId) {
				if (room.gameStatus == TicTacToeRoom.GameStatus.CONTINUE) {
					// Avise para o outro player que a partida acabou por desistência...
				}
				room.player1 = null
				room.player1WebSocket = null
				room.player2 = null
				room.player2WebSocket = null
				loritta.ticTacToeServer.rooms.remove(roomId.get())
			}
		}
	}

	fun onSocketError(ws: WebSocket, session: Session) {

	}
}