package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationConfig
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class CustomBadgePayload : ConfigPayloadType("badge") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
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
				ImageIO.write(img, "png", File(Loritta.ASSETS, "badges/custom/${guild.id}.png"))
			}
		}
	}
}