package net.perfectdreams.loritta.trunfo

import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import org.jooby.WebSocket

class Player(
		val identification: TemmieDiscordAuth.UserIdentification,
		val ws: WebSocket,
		val cards: MutableList<Card>
) {
	enum class Status {

	}
}