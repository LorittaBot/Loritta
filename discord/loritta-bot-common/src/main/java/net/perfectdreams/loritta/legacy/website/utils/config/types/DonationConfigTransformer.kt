package net.perfectdreams.loritta.legacy.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.perfectdreams.loritta.legacy.dao.DonationConfig
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.utils.loritta
import net.perfectdreams.loritta.legacy.utils.toBufferedImage
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.dreamstorageservice.data.api.CreateImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.DeleteImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.UploadImageRequest
import net.perfectdreams.loritta.legacy.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.legacy.common.utils.StoragePaths
import net.perfectdreams.loritta.legacy.utils.SimpleImageInfo
import net.perfectdreams.loritta.legacy.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

object DonationConfigTransformer : ConfigTransformer {
    override val payloadType: String = "donation"
    override val configKey: String = "donationConfig"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        var badgePath: String? = null
        var badgePreferredMediaType: String? = null
        val data = payload["badgeImage"].nullString

        if (data != null) {
            val base64Image = data.split(",")[1]
            val imageBytes = Base64.getDecoder().decode(base64Image)
            // TODO: Maybe add a dimension check to avoid crashing Loritta when loading the image?
            val mediaType = try { SimpleImageInfo(imageBytes).mimeType } catch (e: IOException) { null }
            val img = readImage(ByteArrayInputStream(imageBytes))

            if (img != null && mediaType != null) {
                var finalImage = img
                if (finalImage.width > 256 && finalImage.height > 256)
                    finalImage = finalImage.getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH)
                        .toBufferedImage()

                // This will convert the image to the preferred content type
                // This is useful for JPEG images because if the image has alpha (TYPE_INT_ARGB), the result file will have 0 bytes
                // https://stackoverflow.com/a/66954103/7271796
                val targetContentType = ContentType.parse(mediaType)
                if (targetContentType == ContentType.Image.JPEG && finalImage.type == BufferedImage.TYPE_INT_ARGB) {
                    val newBufferedImage = BufferedImage(
                        finalImage.width,
                        finalImage.height,
                        BufferedImage.TYPE_INT_RGB
                    )
                    newBufferedImage.graphics.drawImage(finalImage, 0, 0, null)
                    finalImage = newBufferedImage
                }

                // Write the badge to a ByteArray
                // DO NOT USE NoCopyByteArrayOutputStream HERE, IT MAY CAUSE ISSUES DUE TO THE BYTEARRAY HAVING MORE BYTES THAN IT SHOULD!!
                val baos = ByteArrayOutputStream()
                ImageIO.write(finalImage, MediaTypeUtils.convertContentTypeToExtension(targetContentType), baos)

                // And now upload it!
                val (isUnique, imageInfo) = loritta.dreamStorageService.uploadImage(
                    baos.toByteArray(),
                    targetContentType,
                    UploadImageRequest(false)
                )

                val (folder, file) = StoragePaths.CustomBadge(guild.idLong, "%s")

                val (info) = loritta.dreamStorageService.createImageLink(
                    CreateImageLinkRequest(
                        imageInfo.imageId,
                        folder,
                        file
                    )
                )
                badgePath = info.file
                badgePreferredMediaType = targetContentType.toString()
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
                val currentCustomBadgePath = donationConfig.customBadgeFile

                if (currentCustomBadgePath != null && badgePath != donationConfig.customBadgeFile) {
                    // Request deletion of the old badge link
                    // We are going to execute in a separate coroutine because it doesn't really matter, right?
                    GlobalScope.launch {
                        val (folder, file) = StoragePaths.CustomBadge(serverConfig.id.value, currentCustomBadgePath)

                        loritta.dreamStorageService.deleteImageLink(
                            DeleteImageLinkRequest(folder, file)
                        )
                    }
                }

                donationConfig.customBadgeFile = badgePath
                donationConfig.customBadgePreferredMediaType = badgePreferredMediaType
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