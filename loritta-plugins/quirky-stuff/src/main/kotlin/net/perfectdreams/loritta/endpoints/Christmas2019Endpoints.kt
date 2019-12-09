package net.perfectdreams.loritta.endpoints

import org.jooby.Jooby

class Christmas2019Endpoints : Jooby() {
	init {
		use(Christmas2019Controller::class.java)
		use(Christmas2019StatsController::class.java)
	}
}