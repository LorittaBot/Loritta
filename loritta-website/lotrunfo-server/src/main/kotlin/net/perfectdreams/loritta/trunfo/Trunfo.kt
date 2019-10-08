package net.perfectdreams.loritta.trunfo

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import org.jooby.Kooby
import java.io.File

class Trunfo : Kooby({
	port(9999) // Porta do website
	assets("/**", File("/home/servers/trunfo/static/").toPath()).onMissing(0)

	ws("/ws") { ws ->
		println("Logged in! Yay!!")

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
				val availableGame = games.firstOrNull { (it.player1 != null && it.player2 == null) || (it.player1 == null && it.player2 != null) }

				if (availableGame != null) {
					availableGame.join(json["name"].string, ws)
				} else {
					val game = Game()
					game.join(json["name"].string, ws)

					games.add(
							game
					)
				}
			}
		}
		// ws.send("hello world from server ;)")
	}
}) {
	companion object {
		val games = mutableListOf<Game>()
	}
}