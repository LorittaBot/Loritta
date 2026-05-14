package net.perfectdreams.loritta.morenitta.websitedashboard.routes.badges

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.util.getOrFail
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.and
import java.util.UUID

class GetBadgeImageUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/badge-image/{badgeId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val badgeIdString = call.parameters.getOrFail("badgeId")
        val badgeId = try {
            UUID.fromString(badgeIdString)
        } catch (_: IllegalArgumentException) {
            call.respondText("", status = HttpStatusCode.BadRequest)
            return
        }

        val badge = website.loritta.profileDesignManager.badges.firstOrNull { it.id == badgeId } as? Badge.LorittaBadge
            ?: run {
                // It might be a guild badge for one of the user's guilds. Look it up via getUserBadges.
                val profile = website.loritta.getLorittaProfile(session.userId)
                if (profile == null) {
                    call.respondText("", status = HttpStatusCode.NotFound)
                    return
                }

                val userInfo = website.loritta.lorittaShards.retrieveUserInfoById(session.userId)
                if (userInfo == null) {
                    call.respondText("", status = HttpStatusCode.NotFound)
                    return
                }

                val profileSettings = website.loritta.newSuspendedTransaction { profile.settings }
                val profileUserInfoData = website.loritta.profileDesignManager.transformUserToProfileUserInfoData(userInfo, profileSettings)
                val mutualGuilds = website.loritta.pudding.transaction {
                    GuildProfiles
                        .select(GuildProfiles.guildId)
                        .where {
                            GuildProfiles.userId eq session.userId and
                                (GuildProfiles.isInGuild eq true)
                        }
                        .map { it[GuildProfiles.guildId] }
                        .toSet()
                }

                val earnedBadges = website.loritta.profileDesignManager.getUserBadges(profileUserInfoData, profile, mutualGuilds, false)
                val guildBadge = earnedBadges.firstOrNull { it.id == badgeId }
                if (guildBadge == null) {
                    call.respondText("", status = HttpStatusCode.NotFound)
                    return
                }
                val guildBadgeImage = guildBadge.getImage()
                if (guildBadgeImage == null) {
                    call.respondText("", status = HttpStatusCode.NotFound)
                    return
                }
                call.respondBytes(guildBadgeImage.toByteArray(ImageFormatType.PNG), ContentType.Image.PNG)
                return
            }

        val image = badge.getImage()
        if (image == null) {
            call.respondText("", status = HttpStatusCode.NotFound)
            return
        }

        call.respondBytes(image.toByteArray(ImageFormatType.PNG), ContentType.Image.PNG)
    }
}
