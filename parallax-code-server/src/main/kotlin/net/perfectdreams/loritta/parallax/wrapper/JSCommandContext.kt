package net.perfectdreams.loritta.parallax.wrapper

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.commands.SilentCommandException
import net.perfectdreams.loritta.parallax.ParallaxServer
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.PolyglotException

class JSCommandContext(
		private val context: Context,
		val lorittaClusterID: Int,
		val client: Client,
		val member: GuildMember,
		val message: Message,
		val args: Array<String>,
		val clusterUrl: String,
		val locale: BaseLocale
) {
	val utils = JSContextUtils(this)
	val rateLimiter = ParallaxRateLimiter(this, context)
	var lastThrow: Throwable? = null

	fun throwAndHalt(throwable: Throwable) {
		lastThrow = throwable
		logLastThrow()
		throw throwable
	}

	fun logLastThrow() {
		println("jsStacktrace")
		val lastThrow = lastThrow
		if (lastThrow != null) {
			if (lastThrow is SilentCommandException)
				return

			rateLimiter.reset()
			if (lastThrow is PolyglotException) {
				if (lastThrow.isHostException) {
					val hostException = lastThrow.asHostException()
					if (hostException is SilentCommandException)
						return
				}

				lastThrow.printStackTrace()

				val embed = ParallaxEmbed()
						.setTitle("\uD83D\uDC1B ${lastThrow.message}")

				if (lastThrow.sourceLocation != null) {
					val shiftedLine = lastThrow.sourceLocation.startLine - ParallaxServer.SHIFT_STACKTRACE_BY
					lastThrow.sourceLocation.source.getLineNumber(lastThrow.sourceLocation.startLine)

					var build = "at **line ${shiftedLine}**, **column ${lastThrow.sourceLocation.startColumn}**"
					build += "\n"
					build += "```js"
					build += "\n"
					val line = lastThrow.sourceLocation.source.getCharacters(lastThrow.sourceLocation.startLine)
					build += line
					build += "\n"
					val charArray = Array(line.length) { ' ' }

					for (x in (lastThrow.sourceLocation.startColumn - 1) until lastThrow.sourceLocation.endColumn) {
						charArray[x] = '^'
					}

					build += charArray.joinToString("")
					build += "```"

					embed.setDescription(build)
				}

				message.channel.send(message.author.toString(), embed)
			} else if (lastThrow is Exception) {
				val embed = ParallaxEmbed()
						.setTitle("\uD83D\uDC1B ${lastThrow.message}")

				message.channel.send(message.author.toString(), embed)
			}
		}
	}
}