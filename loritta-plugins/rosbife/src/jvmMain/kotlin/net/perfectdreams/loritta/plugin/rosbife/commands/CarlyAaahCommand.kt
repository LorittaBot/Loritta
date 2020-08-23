package net.perfectdreams.loritta.plugin.rosbife.commands

import io.ktor.client.call.receive
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.json
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DSLCommandBase
import java.util.*

object CarlyAaahCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot) = create(
			loritta,
			listOf("carlyaaah")
	) {
		description { it["commands.images.carlyaaah.description"] }

		usage {
			argument(ArgumentType.IMAGE) {}
		}

		needsToUploadFiles = true

		executes {
			// TODO: Multiplatform
			val context = checkType<DiscordCommandContext>(this)

			val mppImage = validate(image(0))

			val response = loritta.http.post<HttpResponse>("https://gabriela.loritta.website/api/videos/carly-aaah") {
				body = json {
					"image" to Base64.getEncoder().encodeToString(mppImage.toByteArray())
				}.toString()
			}

			context.sendFile(response.receive<ByteArray>().inputStream(), "carly_aaah.mp4")
		}
	}
}