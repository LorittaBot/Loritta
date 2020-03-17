package net.perfectdreams.loritta.trunfo

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.inject.Injector
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import org.jooby.Jooby
import org.jooby.Kooby
import org.jooby.Status
import org.jooby.internal.SessionManager
import org.jooby.mongodb.MongoSessionStore
import org.jooby.mongodb.Mongodb
import java.io.File
import kotlin.concurrent.thread

class Trunfo : Kooby({
	port(9999) // Porta do website
	assets("/**", File("/home/servers/trunfo/static/").toPath()).onMissing(0)
	use(Mongodb()) // Usar extensão do MongoDB para o Jooby
	session(MongoSessionStore::class.java) // Usar session store para o MongoDB do Jooby

	get("/api/v1/users/@me") { req, res ->
		val discordAuthSession = req.session()["discordAuth"]

		if (discordAuthSession.isSet) {
			val discordAuth = gson.fromJson<TemmieDiscordAuth>(discordAuthSession.value())

			try {
				discordAuth.isReady(true)
				val userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro

				res.send(
						gson.toJson(
								jsonObject(
										"name" to userIdentification.username,
										"id" to userIdentification.id,
										"avatarUrl" to userIdentification.avatar
								)
						)
				)
			} catch (e: Exception) {
				req.session().unset("discordAuth")
				res.status(Status.UNAUTHORIZED)
				res.send("{}")
			}
		} else {
			res.status(Status.UNAUTHORIZED)
			res.send("{}")
		}
	}

	ws("/ws") { handler, ws ->
		println("Logged in! Yay!!")

		val _field = Jooby::class.java.getDeclaredField("injector")
		_field.isAccessible = true

		val injector = _field.get(this) as Injector
		val sm = injector.getProvider(SessionManager::class.java).get()

		val session = sm.get(handler, null)

		ws.onClose {
			println("oh no, closed???")

			val playerInGame = games.firstOrNull { it.player1?.ws == ws || it.player2?.ws == ws }

			println("Player was in game $playerInGame, let's clean up and remove it")

			if (playerInGame != null) {
				playerInGame.cleanUp()
				games.remove(playerInGame)
			}
		}

		ws.onMessage {
			println("omg i received something too ewe: ${it.value()}")

			val json = jsonParser.parse(it.value())
			if (json["status"].string == "JOIN_MATCHMAKING") {
				// try discord auth
				if (session != null) {
					val discordAuthSession = session["discordAuth"]

					if (discordAuthSession.isSet) {
						val discordAuth = gson.fromJson<TemmieDiscordAuth>(discordAuthSession.value())

						try {
							discordAuth.isReady(true)
							val userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro

							// ws ["discordId"] = userIdentification.id

							val availableGame = games.firstOrNull { (it.player1 != null && it.player2 == null) || (it.player1 == null && it.player2 != null) }

							if (availableGame != null) {
								availableGame.join(userIdentification, ws)
							} else {
								val game = Game()
								game.join(userIdentification, ws)

								games.add(
										game
								)
							}
						} catch (e: Exception) {
							e.printStackTrace()

							println("Error while authenticating, redirecting...")
							session.unset("discordAuth")

							ws.send(
									gson.toJson(
											jsonObject(
													"status" to "UNAUTHORIZED"
											)
									)
							)
						}
					} else {
						// Unauthorized, redirecting...
						println("Unauthorized, redirecting...")
						ws.send(
								gson.toJson(
										jsonObject(
												"status" to "UNAUTHORIZED"
										)
								)
						)
					}
				} else {
					// Session does not exist, redirecting...
					println("Session does not exist, redirecting...")
					ws.send(
							gson.toJson(
									jsonObject(
											"status" to "UNAUTHORIZED"
									)
							)
					)
				}
			}
		}

		thread {
			while (ws.isOpen) {
				ws.send(
						gson.toJson(
								jsonObject(
										"status" to "PING"
								)
						)
				)

				Thread.sleep(2_500)
			}
		}
		// ws.send("hello world from server ;)")
	}
}) {
	companion object {
		val games = mutableListOf<Game>()
	}
}