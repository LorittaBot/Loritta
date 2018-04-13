package com.mrpowergamerbr.loritta.utils

import org.jooby.Kooby
import java.io.File
import java.util.*


class TicTacToeTest() : Kooby({
	port(4568) // Porta do website
	get("/") { request ->
		val random = UUID.randomUUID()

		request.session().set("owo_whats_this", random.toString())
		File("C:\\Users\\Whistler\\Documents\\TavaresBot\\WebSockets\\test.html").readText().replace("{{ wow }}", random.toString())
	}

	ws("/lorisocket") { req, ws ->
		req.session()
	}
})

fun main(args: Array<String>) {
	TicTacToeTest().start()
}