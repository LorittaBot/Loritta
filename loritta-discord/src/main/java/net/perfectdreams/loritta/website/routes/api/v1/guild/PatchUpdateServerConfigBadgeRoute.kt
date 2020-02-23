package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class PatchUpdateServerConfigBadgeRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/{guildId}/badge") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val payload = JsonParser.parseString(call.receiveText())
		val data = payload["badge"].string

		val base64Image = data.split(",")[1]
		val imageBytes = Base64.getDecoder().decode(base64Image)
		val img = ImageIO.read(ByteArrayInputStream(imageBytes))

		if (img != null)
			ImageIO.write(img, "png", File(Loritta.ASSETS, "badges/custom/${call.parameters["guildId"]}.png"))

		call.respondJson(jsonObject())
	}
}