package net.perfectdreams.loritta.parallax.wrapper

import org.graalvm.polyglot.Context

class JSCommandContext(
		private val context: Context,
		val lorittaClusterID: Int,
		val client: Client,
		val member: GuildMember,
		val message: Message,
		val args: Array<String>,
		val clusterUrl: String
) {
	val rateLimiter = ParallaxRateLimiter(context)

	fun jsStacktrace(throwable: Any?) {
		message.channel.send(throwable.toString())
	}
}