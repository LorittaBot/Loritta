package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import net.dv8tion.jda.api.entities.User.UserFlag
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.profiles.AnimatedProfileCreator
import net.perfectdreams.loritta.morenitta.profile.profiles.RawProfileCreator
import net.perfectdreams.loritta.morenitta.profile.profiles.StaticProfileCreator
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.BackgroundStorageType
import net.perfectdreams.loritta.serializable.ColorTheme
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.EnumSet
import javax.imageio.ImageIO

class UserProfilePreviewDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/profile-preview") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val profile = website.loritta.getOrCreateLorittaProfile(session.userId)
        val settings = website.loritta.newSuspendedTransaction {
            profile.settings
        }

        val internalTypeName = call.parameters["type"] ?: "defaultDark"
        val backgroundTypeName = call.parameters["background"]

        val profileCreator = website.loritta.profileDesignManager.designs.first {
            it.internalName == internalTypeName
        }

        val locale = website.loritta.localeManager.getLocaleById("default")

        val userId = session.userId
        // Disabled because Loritta loads the avatar URL from the user identification cache
        // This causes issues when loading the "profile" page when the user changed the avatar recently
        // So for now we are going to always load Discord's default avatar
        /* val avatarUrl = if (userIdentification.avatar != null) {
            val extension = if (userIdentification.avatar.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                "gif"
            } else { "png" }

            "https://cdn.discordapp.com/avatars/${userId}/${userIdentification.avatar}.${extension}?size=256"
        } else {
            val avatarId = userId % 5

            "https://cdn.discordapp.com/embed/avatars/$avatarId.png?size=256"
        } */
        val avatarUrl = run {
            val avatarId = userId % 5

            "https://cdn.discordapp.com/embed/avatars/$avatarId.png?size=256"
        }

        val backgroundImage = if (backgroundTypeName == null) {
            BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        } else {
            var backgroundTypeNameToBeRetrieved = backgroundTypeName

            // The "random" and "custom" backgrounds are special backgrounds that do not have any background variations associated with it, causing a crash when attempting to
            // render a profile with them
            //
            // So, to workaround this, we fall back to the default background
            // This does not happen when using the profile command because the code already has checks for when we are querying these two backgrounds
            if (backgroundTypeNameToBeRetrieved == Background.RANDOM_BACKGROUND_ID || backgroundTypeNameToBeRetrieved == Background.CUSTOM_BACKGROUND_ID)
                backgroundTypeNameToBeRetrieved = Background.DEFAULT_BACKGROUND_ID
            val dssNamespace = website.loritta.dreamStorageService.getCachedNamespaceOrRetrieve()

            val background = website.loritta.pudding.backgrounds.getBackground(backgroundTypeNameToBeRetrieved)
            if (background == null) {
                call.respondText("", status = HttpStatusCode.NotFound)
                return
            }

            val variation = background.getVariationForProfileDesign(internalTypeName)
            val url = when (variation.storageType) {
                BackgroundStorageType.DREAM_STORAGE_SERVICE -> website.loritta.profileDesignManager.getDreamStorageServiceBackgroundUrlWithCropParameters(
                    website.loritta.config.loritta.dreamStorageService.url,
                    dssNamespace,
                    variation
                )

                BackgroundStorageType.ETHEREAL_GAMBI -> website.loritta.profileDesignManager.getEtherealGambiBackgroundUrl(
                    variation
                )
            }

            // We could just respondRedirect the URL, but we need to scale the image because some images don't have the proper aspect ratio
            // (Yes, even tho the DreamStorageService has custom crops on the URL... it, for some reason, aren't actually correct?)
            val backgroundImage = BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB)
            val downloadedBackgroundImage = ImageIO.read(website.loritta.http.get(url).readBytes().inputStream()) // We hope this is a trusted image
            backgroundImage.createGraphics().drawImage(downloadedBackgroundImage, 0, 0, 800, 600, null)
            backgroundImage
        }

        val senderUserData = ProfileUserInfoData(
            session.userId,
            session.username,
            session.discriminator,
            avatarUrl,
            false,
            EnumSet.noneOf(UserFlag::class.java) // TODO: Fix user flags, afaik they are provided in the UserIdentification
        )

        val image = when (profileCreator) {
            is StaticProfileCreator -> profileCreator.create(
                senderUserData,
                senderUserData,
                profile,
                null,
                listOf(),
                listOf(),
                null,
                locale,
                i18nContext,
                backgroundImage, // Create profile with transparent background
                settings.aboutMe ?: "???",
                listOf()
            )
            is AnimatedProfileCreator -> profileCreator.create(
                senderUserData,
                senderUserData,
                profile,
                null,
                listOf(),
                listOf(),
                null,
                locale,
                website.loritta.languageManager.defaultI18nContext, // TODO: Provide the correct i18n context!
                backgroundImage, // Create profile with transparent background
                settings.aboutMe ?: "???",
                listOf()
            ).first() // We only want the first frame of the list
            is RawProfileCreator -> {
                // TODO: We need to refactor RawProfileCreator to properly support this endpoint
                // This is a special case because idk how we could support this endpoint with "RawProfileCreator"
                val profileImageRawData = profileCreator.create(
                    senderUserData,
                    senderUserData,
                    profile,
                    null,
                    listOf(),
                    listOf(),
                    null,
                    locale,
                    website.loritta.languageManager.defaultI18nContext, // TODO: Provide the correct i18n context!
                    backgroundImage, // Create profile with transparent background
                    settings.aboutMe ?: "???",
                    listOf()
                )

                call.respondBytes(
                    profileImageRawData.first,
                    when (profileImageRawData.second) {
                        ImageFormat.PNG -> ContentType.Image.PNG
                        ImageFormat.JPG -> ContentType.Image.JPEG
                        ImageFormat.GIF -> ContentType.Image.GIF
                        // Ktor does not have ContentType.Image.WEBP yet
                        ImageFormat.WEBP -> ContentType.parse("image/webp")
                    },
                    HttpStatusCode.OK
                )
                return
            }
            else -> error("Unsupported Profile Creator Type $profileCreator")
        }

        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        call.respondBytes(baos.toByteArray(), ContentType.Image.PNG, HttpStatusCode.OK)
    }
}