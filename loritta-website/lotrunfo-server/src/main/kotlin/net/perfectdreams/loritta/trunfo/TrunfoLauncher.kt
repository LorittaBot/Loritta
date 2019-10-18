package net.perfectdreams.loritta.trunfo

object TrunfoLauncher {
	@JvmStatic
	fun main(args: Array<String>) {
		org.jooby.run({
			Trunfo()
		})
	}
}