package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.badge

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.dreamstorageservice.data.api.CreateImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.DeleteImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.UploadImageRequest
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.StoragePaths
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.DonationConfig
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import net.perfectdreams.loritta.morenitta.utils.toBufferedImage
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds.PostUploadBackgroundUserDashboardRoute.UploadBackgroundRequest
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64
import javax.imageio.ImageIO

class PostBadgeImageGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/badge/upload") {
    @Serializable
    data class UploadBadgeImageRequest(
        val file: List<File>
    ) {
        @Serializable
        data class File(
            val data: String,
            val name: String,
        )
    }

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<UploadBackgroundRequest>(call.receiveText()).file.first()

        if (!guildPremiumPlan.hasCustomBadge) {
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "O servidor precisa ter premium para fazer isto!"
                            )
                        )
                    },
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val decodedBytes = Base64.getDecoder().decode(request.data)

        val mediaType = try { SimpleImageInfo(decodedBytes).mimeType } catch (e: IOException) { null }
        val img = readImage(ByteArrayInputStream(decodedBytes))

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
            val (isUnique, imageInfo) = website.loritta.dreamStorageService.uploadImage(
                baos.toByteArray(),
                targetContentType,
                UploadImageRequest(false)
            )

            val (folder, file) = StoragePaths.CustomBadge(guild.idLong, "%s")

            val (info) = website.loritta.dreamStorageService.createImageLink(
                CreateImageLinkRequest(
                    imageInfo.imageId,
                    folder,
                    file
                )
            )
            val badgePath = info.file
            val badgePreferredMediaType = targetContentType.toString()

            website.loritta.newSuspendedTransaction {
                val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

                val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
                    this.dailyMultiplier = false
                    this.customBadge = false
                }

                val currentCustomBadgePath = donationConfig.customBadgeFile

                if (currentCustomBadgePath != null && badgePath != donationConfig.customBadgeFile) {
                    // Request deletion of the old badge link
                    // We are going to execute in a separate coroutine because it doesn't really matter, right?
                    GlobalScope.launch {
                        val (folder, file) = StoragePaths.CustomBadge(serverConfig.id.value, currentCustomBadgePath)

                        website.loritta.dreamStorageService.deleteImageLink(
                            DeleteImageLinkRequest(folder, file)
                        )
                    }
                }

                donationConfig.customBadgeFile = badgePath
                donationConfig.customBadgePreferredMediaType = badgePreferredMediaType

                serverConfig.donationConfig = donationConfig
            }
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissCloseModal()
                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Insígnia personalizada enviada!"))
                    }
            )
        } else {
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.WARN, "Imagem inválida!"))
                    },
                status = HttpStatusCode.BadRequest
            )
        }
    }
}