package net.perfectdreams.loritta.trunfo

import org.jooby.WebSocket

class Player(
		val name: String,
		val ws: WebSocket,
		val cards: MutableList<Card>
) {
	enum class Status {

	}
}