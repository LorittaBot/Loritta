package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.DonationConfig
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.dreamstorageservice.data.DeleteFileLinkRequest
import net.perfectdreams.dreamstorageservice.data.UploadFileRequest
import net.perfectdreams.loritta.api.utils.NoCopyByteArrayOutputStream
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO

object DonationConfigTransformer : ConfigTransformer {
    override val payloadType: String = "donation"
    override val configKey: String = "donationConfig"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        var badgePath: String? = null
        val data = payload["badgeImage"].nullString

        if (data != null) {
            val base64Image = data.split(",")[1]
            val imageBytes = Base64.getDecoder().decode(base64Image)
            val img = readImage(ByteArrayInputStream(imageBytes))

            if (img != null) {
                var finalImage = img
                if (finalImage.width > 256 && finalImage.height > 256)
                    finalImage = finalImage.getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH)
                        .toBufferedImage()

                // Write the badge to a ByteArray
                val baos = NoCopyByteArrayOutputStream()
                ImageIO.write(finalImage, "png", baos)

                // And now upload it!
                val (path, fullPath) = loritta.dreamStorageService.fileLinks.uploadFile(
                    baos.toByteArray(),
                    ContentType.Image.PNG,
                    UploadFileRequest(
                        "badges/custom/${guild.idLong}/%s.png"
                    )
                )
                badgePath = path
            }
        }

        loritta.newSuspendedTransaction {
            val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
                this.dailyMultiplier = false
                this.customBadge = false
            }
            donationConfig.dailyMultiplier = payload["dailyMultiplier"].nullBool ?: donationConfig.dailyMultiplier
            donationConfig.customBadge = payload["customBadge"].nullBool ?: donationConfig.customBadge
            if (badgePath != null) { // Update the badge path if needed
                val currentCustomBadgePath = donationConfig.customBadgePath

                if (currentCustomBadgePath != null && badgePath != donationConfig.customBadgePath) {
                    // Request deletion of the old badge link
                    // We are going to execute in a separate coroutine because it doesn't really matter, right?
                    GlobalScope.launch {
                        loritta.dreamStorageService.fileLinks.deleteLink(DeleteFileLinkRequest(currentCustomBadgePath))
                    }
                }

                donationConfig.customBadgePath = badgePath
            }

            serverConfig.donationConfig = donationConfig
        }
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return loritta.newSuspendedTransaction {
            jsonObject(
                "customBadge" to (serverConfig.donationConfig?.customBadge ?: false),
                "dailyMultiplier" to (serverConfig.donationConfig?.dailyMultiplier ?: false)
            )
        }
    }
}