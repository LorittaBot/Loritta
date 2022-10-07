package net.perfectdreams.loritta.morenitta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationConfig
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class CustomBadgePayload(val loritta: LorittaBot) : ConfigPayloadType("badge") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		runBlocking {
			loritta.pudding.transaction {
				val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
					this.customBadge = false
				}
				donationConfig.customBadge = payload["customBadge"].bool

				serverConfig.donationConfig = donationConfig
			}

			val data = payload["badgeImage"].nullString

			if (data != null) {
				val base64Image = data.split(",")[1]
				val imageBytes = Base64.getDecoder().decode(base64Image)
				val img = ImageIO.read(ByteArrayInputStream(imageBytes))

				if (img != null) {
					ImageIO.write(img, "png", File(LorittaBot.ASSETS, "badges/custom/${guild.id}.png"))
				}
			}
		}
	}
}