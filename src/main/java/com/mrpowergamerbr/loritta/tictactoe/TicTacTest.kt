package com.mrpowergamerbr.loritta.tictactoe

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.frontend.views.GlobalHandler
import org.jooby.Jooby
import org.jooby.Kooby
import org.jooby.mongodb.MongoSessionStore
import org.jooby.mongodb.Mongodb
import java.io.File
import kotlin.concurrent.thread

class TicTacTest : Jooby() {
	init {
		port(4568)
		securePort(4569)
		get("hello") { req, res ->
			req.session()["foo"] = "hello"
			res.send(File("C:\\Users\\Whistler\\Documents\\TavaresBot\\tic_tac_toe\\index.html").readText())
		}
		assets("/**", File("C:\\Users\\Whistler\\Documents\\TavaresBot\\tic_tac_toe\\").toPath())
		/** Start a websocket at /ws and send back JSON: */
		ws("/ws", { handler, ws ->
			val test = handler.session().get("test")
		})
	}
}

fun main(args: Array<String>) {
	val website = thread(true, name = "Website Thread") {
		org.jooby.run({ TicTacTest() })
	}
}