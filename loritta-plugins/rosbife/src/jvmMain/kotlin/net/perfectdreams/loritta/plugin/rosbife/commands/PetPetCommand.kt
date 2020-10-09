package net.perfectdreams.loritta.plugin.rosbife.commands

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DSLCommandBase
import java.util.*

object PetPetCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot) = create(
			loritta,
			listOf("petpet", "patpat")
	) {
		description { it["commands.images.petpet.description"] }

		usage {
			argument(ArgumentType.IMAGE) {}
		}

		needsToUploadFiles = true

		executes {
			// TODO: Multiplatform
			val mppImage = validate(image(0))

			val response = loritta.http.post<HttpResponse>("https://gabriela.loritta.website/api/images/pet-pet") {
				body = buildJsonObject {
					put("image", Base64.getEncoder().encodeToString(mppImage.toByteArray()))
				}.toString()
			}

			sendFile(response.receive(), "petpet.gif")
		}
	}
}