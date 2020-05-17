package net.perfectdreams.loritta.parallax.wrapper

import net.perfectdreams.loritta.api.commands.SilentCommandException
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
	val utils = JSContextUtils(this)
	val rateLimiter = ParallaxRateLimiter(context)

	fun jsStacktrace(throwable: Any?) {
		if (throwable !is SilentCommandException)
			message.channel.send(throwable.toString())
	}
}