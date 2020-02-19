package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationConfig
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

object DonationConfigTransformer : ConfigTransformer {
    override val payloadType: String = "donation"
    override val configKey: String = "donationConfig"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        transaction(Databases.loritta) {
            val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
                this.dailyMultiplier = false
                this.customBadge = false
            }
            donationConfig.dailyMultiplier = payload["dailyMultiplier"].nullBool ?: donationConfig.dailyMultiplier
            donationConfig.customBadge = payload["customBadge"].nullBool ?: donationConfig.customBadge

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

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return transaction(Databases.loritta) {
            jsonObject(
                    "customBadge" to (serverConfig.donationConfig?.customBadge ?: false),
                    "dailyMultiplier" to (serverConfig.donationConfig?.dailyMultiplier ?: false)
            )
        }
    }
}