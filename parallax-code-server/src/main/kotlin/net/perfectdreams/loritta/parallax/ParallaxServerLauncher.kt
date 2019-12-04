package net.perfectdreams.loritta.parallax

object ParallaxServerLauncher {
	@JvmStatic
	fun main(args: Array<String>) {
		val server = ParallaxServer()
		server.start()
	}
}