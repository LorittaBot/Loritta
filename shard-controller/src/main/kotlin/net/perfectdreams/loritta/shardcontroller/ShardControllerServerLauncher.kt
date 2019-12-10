package net.perfectdreams.loritta.shardcontroller

object ShardControllerServerLauncher {
	@JvmStatic
	fun main(args: Array<String>) {
		val server = ShardControllerServer()
		server.start()
	}
}