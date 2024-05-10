package net.perfectdreams.loritta.morenitta.website.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.serializable.BackgroundStorageType
import net.perfectdreams.sequins.ktor.BaseRoute
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class GetBackgroundRoute(val loritta: LorittaBot) : BaseRoute("/background/{internalName}") {
    override suspend fun onRequest(call: ApplicationCall) {
        val internalName = call.parameters.getOrFail("internalName")
        val profileDesign = call.parameters["profileDesign"] ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID
        val dssNamespace = loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

        val background = loritta.pudding.backgrounds.getBackground(internalName)
        if (background == null) {
            call.respondText("", status = HttpStatusCode.NotFound)
            return
        }

        val variation = background.getVariationForProfileDesign(profileDesign)
        val url = when (variation.storageType) {
            BackgroundStorageType.DREAM_STORAGE_SERVICE -> loritta.profileDesignManager.getDreamStorageServiceBackgroundUrlWithCropParameters(loritta.config.loritta.dreamStorageService.url, dssNamespace, variation)
            BackgroundStorageType.ETHEREAL_GAMBI -> loritta.profileDesignManager.getEtherealGambiBackgroundUrl(variation)
        }

        // We could just respondRedirect the URL, but we need to scale the image because some images don't have the proper aspect ratio
        // (Yes, even tho the DreamStorageService has custom crops on the URL... it, for some reason, aren't actually correct?)
        val backgroundImage = BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB)
        val downloadedBackgroundImage = ImageIO.read(loritta.http.get(url).readBytes().inputStream()) // We hope this is a trusted image
        backgroundImage.createGraphics().drawImage(downloadedBackgroundImage, 0, 0, 800, 600, null)
        return call.respondBytes(backgroundImage.toByteArray(ImageFormatType.PNG), ContentType.Image.PNG)
    }
}