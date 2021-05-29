package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.extensions.readImage
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class PatchUpdateServerConfigBadgeRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/{guildId}/badge") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }
		val data = payload["badge"].string

		val base64Image = data.split(",")[1]
		val imageBytes = Base64.getDecoder().decode(base64Image)
		val img = readImage(ByteArrayInputStream(imageBytes))

		if (img != null) {
			var finalImage = img
			if (finalImage.width > 256 && finalImage.height > 256)
				finalImage = finalImage.getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH)
						.toBufferedImage()

			ImageIO.write(finalImage, "png", File(Loritta.ASSETS, "badges/custom/${call.parameters["guildId"]}.png"))
		}

		call.respondJson(jsonObject())
	}
}