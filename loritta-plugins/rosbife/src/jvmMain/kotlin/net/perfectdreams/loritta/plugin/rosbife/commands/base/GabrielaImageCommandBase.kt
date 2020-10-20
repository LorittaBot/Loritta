package net.perfectdreams.loritta.plugin.rosbife.commands.base

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaAbstractCommandBase
import java.util.*

/**
 * Commands that use Gabriela's Image Generator tool should use this class!
 *
 * TODO: This class could be multiplatform if the Base64 implementation was multiplatform!
 *
 * @param endpoint the page endpoint (example: "/api/v1/videos/carly-aaah")
 * @param fileName the sent file file name (example: "carly_aaah.mp4")
 */
abstract class GabrielaImageCommandBase(
		loritta: LorittaBot,
		labels: List<String>,
		val descriptionKey: String,
		val endpoint: String,
		val fileName: String,
) : LorittaAbstractCommandBase(
		loritta,
		labels,
		CommandCategory.IMAGES
) {
	override fun command() = create {
		localizedDescription(descriptionKey)

		executes {
			val mppImage = validate(image(0))

			val response = loritta.http.post<HttpResponse>("https://gabriela.loritta.website$endpoint") {
				body = buildJsonObject {
					put("image", Base64.getEncoder().encodeToString(mppImage.toByteArray()))
				}.toString()
			}

			sendFile(response.receive(), fileName)
		}
	}
}