package com.mrpowergamerbr.loritta.frontend.views.subviews.api.tictactoe

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.json.XML
import java.net.InetAddress
import java.util.*

class APIStartTicTacToeGameView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/tictactoe/start"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		if (userIdentification == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		// TicTacToe é meio complicado, ao iniciar, ele envia um código para uma sala do TicTacToe junto da URL
		// /api/v1/tictactoe/start?r=SJQBSQS por exemplo
		val roomIdMutant = req.param("r")

		if (!roomIdMutant.isSet) { // Se o ?r está faltando, retorne MISSING_ROOM_ID
			return jsonObject("api:code" to LoriWebCodes.MISSING_ROOM_ID).toString()
		}

		val roomId = roomIdMutant.value()

		// A nossa sala DEVE ter sido iniciada pelo comando +jogodavelha ou por outra frontend
		val room = loritta.ticTacToeServer.rooms.getOrDefault(roomId, null) ?: return jsonObject("api:code" to LoriWebCodes.INVALID_ROOM).toString()

		if (room.player1 != null && room.player2 != null) {
			return jsonObject("api:code" to LoriWebCodes.ROOM_IS_FULL).toString()
		}

		if (room.player1 == null) {
			return jsonObject("api:code" to LoriWebCodes.ROOM_IS_FULL).toString()
		}

		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS

		return payload.toString()
	}
}