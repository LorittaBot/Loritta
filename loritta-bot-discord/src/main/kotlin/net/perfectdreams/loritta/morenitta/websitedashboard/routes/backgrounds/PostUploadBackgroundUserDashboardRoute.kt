package net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamstorageservice.data.api.CreateImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.DeleteImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.UploadImageRequest
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings
import net.perfectdreams.loritta.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.common.utils.StoragePaths
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import net.perfectdreams.loritta.morenitta.utils.toBufferedImage
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64
import javax.imageio.ImageIO

class PostUploadBackgroundUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/backgrounds/upload") {
    @Serializable
    data class UploadBackgroundRequest(
        val file: List<File>
    ) {
        @Serializable
        data class File(
            val data: String,
            val name: String,
        )
    }

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        if (!userPremiumPlan.customBackground) {
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Você precisa ter premium para fazer isto!"
                            )
                        )
                    },
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val request = Json.decodeFromString<UploadBackgroundRequest>(call.receiveText()).file.first()

        val decodedBytes = Base64.getDecoder().decode(request.data)
        // TODO: Maybe add a dimension check to avoid crashing Loritta when loading the image?
        val mediaType = try { SimpleImageInfo(decodedBytes).mimeType } catch (e: IOException) { null }
        val decodedImage = readImage(decodedBytes.inputStream())

        if (decodedImage != null && mediaType != null) {
            var writeImage = decodedImage

            if (decodedImage.width != 800 && decodedImage.height != 600)
                writeImage = decodedImage.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH).toBufferedImage()

            // This will convert the image to the preferred content type
            // This is useful for JPEG images because if the image has alpha (TYPE_INT_ARGB), the result file will have 0 bytes
            // https://stackoverflow.com/a/66954103/7271796
            val targetContentType = ContentType.parse(mediaType)
            if (targetContentType == ContentType.Image.JPEG && writeImage.type == BufferedImage.TYPE_INT_ARGB) {
                val newBufferedImage = BufferedImage(
                    writeImage.width,
                    writeImage.height,
                    BufferedImage.TYPE_INT_RGB
                )
                newBufferedImage.graphics.drawImage(writeImage, 0, 0, null)
                writeImage = newBufferedImage
            }

            // DO NOT USE NoCopyByteArrayOutputStream HERE, IT MAY CAUSE ISSUES DUE TO THE BYTEARRAY HAVING MORE BYTES THAN IT SHOULD!!
            val baos = ByteArrayOutputStream()
            ImageIO.write(writeImage, MediaTypeUtils.convertContentTypeToExtension(targetContentType), baos)

            val (isUnique, imageInfo) = website.loritta.dreamStorageService.uploadImage(
                baos.toByteArray(),
                targetContentType,
                UploadImageRequest(false)
            )

            val (folder, file) = StoragePaths.CustomBackground(session.userId, "%s")
            val (linkInfo) = website.loritta.dreamStorageService.createImageLink(
                CreateImageLinkRequest(
                    imageInfo.imageId,
                    folder,
                    file
                )
            )
            val newPath = linkInfo.file
            val preferredMediaType = targetContentType.toString()

            val oldPath = website.loritta.transaction {
                val profile = website.loritta.getOrCreateLorittaProfile(session.userId)
                val profileSettings = profile.settings

                val oldPath = CustomBackgroundSettings.selectAll().where { CustomBackgroundSettings.settings eq profileSettings.id }.firstOrNull()?.get(CustomBackgroundSettings.file)

                CustomBackgroundSettings.upsert(CustomBackgroundSettings.settings) {
                    it[CustomBackgroundSettings.settings] = profileSettings.id
                    it[CustomBackgroundSettings.file] = newPath
                    it[CustomBackgroundSettings.preferredMediaType] = preferredMediaType
                }

                oldPath
            }

            if (oldPath != null && newPath != oldPath) {
                // Request deletion of the old profile background
                val (folder, file) = StoragePaths.CustomBackground(session.userId, oldPath)
                website.loritta.dreamStorageService.deleteImageLink(
                    DeleteImageLinkRequest(
                        folder,
                        file
                    )
                )
            }

            call.respondHtml(
                createHTML(false)
                    .body {
                        blissCloseModal()
                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Background personalizado enviado!"))
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