package net.perfectdreams.loritta.api.commands

fun main() {
	val pingCommand = command(listOf("ping")) {
		this.description { it["commands.misc.ping.description"] }
		this.examples { listOf() }

		executes {
			this.sendMessage("Pong!")
		}
	}
}