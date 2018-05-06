package com.mrpowergamerbr.loritta.frontend.views

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Mutant
import org.jooby.Session
import org.jooby.WebSocket

object WebSocketHandler {
	fun onSocketConnected(ws: WebSocket, session: Session) {
		// owo, socket connected!
	}

	fun onMessageReceived(ws: WebSocket, mutant: Mutant, session: Session) {
		val json = jsonParser.parse(mutant.value()).obj

		val type = json["type"].string

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

			val userIdentification =  try {
				val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(session["discordAuth"].value())
				discordAuth.isReady(true)
				discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				ws.send(jsonObject("api:code" to LoriWebCodes.UNAUTHORIZED).toString())
				return
			}

			room.join(userIdentification.id)

			ws.set("currentTicTacToeRoom", roomId)
			// Caso não esteja, vamos enviar um payload com todas as informações da sala atual para o usuário!
			ws.send(
					GSON.toJson(
							room
					).toString()
			)
			return
		}
		if (type == "make_tictactoe_move") {
			val roomId = ws.get<String>("currentTicTacRoom")

			if (roomId == null) {
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
						)
				)
				return
			}

			if (!session.isSet("discordAuth")) {
				ws.send(jsonObject("api:code" to LoriWebCodes.UNAUTHORIZED).toString())
				return
			}

			val userIdentification =  try {
				val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(session["discordAuth"].value())
				discordAuth.isReady(true)
				discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				ws.send(jsonObject("api:code" to LoriWebCodes.UNAUTHORIZED).toString())
				return
			}

			val cellPosition = json["cell_position"].int

			val status = room.makeMove(cellPosition, userIdentification.id)

			ws.send(
					jsonObject(
							"api:code" to LoriWebCodes.MOVEMENT_DONE,
							"movement_status" to status.toString(),
							"playfield" to room.playfield,
							"player1" to room.player1,
							"player2" to room.player2,
							"currentPlayer" to room.currentPlayer,
							"initializedAt" to room.initializedAt
					).toString()
			)
			return
		}

		ws.send(
				jsonObject(
						"api:code" to LoriWebCodes.MISSING_PAYLOAD_HANDLER,
								"missing_payload_handler" to type
				)
		)
	}

	fun onSocketClose(ws: WebSocket, session: Session) {

	}

	fun onSocketError(ws: WebSocket, session: Session) {

	}
}