package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.util.getOrFail
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.BackgroundStorageType
import net.perfectdreams.loritta.serializable.ColorTheme
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class UserBackgroundPreviewDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/background-preview/{internalName}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val internalName = call.parameters.getOrFail("internalName")
        val profileDesign = call.parameters["profileDesign"] ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID
        val dssNamespace = website.loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

        val background = website.loritta.pudding.backgrounds.getBackground(internalName)
        if (background == null) {
            call.respondText("", status = HttpStatusCode.NotFound)
            return
        }

        val url: String
        if (background.id == Background.CUSTOM_BACKGROUND_ID) {
            url = "${website.loritta.config.loritta.dreamStorageService.url}/$dssNamespace/profiles/backgrounds/custom.png"
        } else if (background.id == Background.RANDOM_BACKGROUND_ID) {
            url = "${website.loritta.config.loritta.dreamStorageService.url}/$dssNamespace/profiles/backgrounds/random.png"
        } else {
            val variation = background.getVariationForProfileDesign(profileDesign)
            url = when (variation.storageType) {
                BackgroundStorageType.DREAM_STORAGE_SERVICE -> website.loritta.profileDesignManager.getDreamStorageServiceBackgroundUrlWithCropParameters(website.loritta.config.loritta.dreamStorageService.url, dssNamespace, variation)
                BackgroundStorageType.ETHEREAL_GAMBI -> website.loritta.profileDesignManager.getEtherealGambiBackgroundUrl(variation)
            }
        }

        // We could just respondRedirect the URL, but we need to scale the image because some images don't have the proper aspect ratio
        // (Yes, even tho the DreamStorageService has custom crops on the URL... it, for some reason, aren't actually correct?)
        val backgroundImage = BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB)
        val downloadedBackgroundImage = ImageIO.read(website.loritta.http.get(url).readBytes().inputStream()) // We hope this is a trusted image
        backgroundImage.createGraphics().drawImage(downloadedBackgroundImage, 0, 0, 800, 600, null)
        return call.respondBytes(backgroundImage.toByteArray(ImageFormatType.PNG), ContentType.Image.PNG)
    }
}