package net.perfectdreams.loritta.morenitta.websitedashboard.routes.badges

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import io.ktor.server.util.getOrFail
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.HiddenUserBadges
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.badgeItemInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class GetBadgeUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/badges/{badgeId}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val badgeIdString = call.parameters.getOrFail("badgeId")
        val badgeId = try {
            UUID.fromString(badgeIdString)
        } catch (_: IllegalArgumentException) {
            call.respondText("", status = HttpStatusCode.BadRequest)
            return
        }

        val profile = website.loritta.getOrCreateLorittaProfile(session.userId)
        val userInfo = website.loritta.lorittaShards.retrieveUserInfoById(session.userId)

        val profileSettings = website.loritta.newSuspendedTransaction { profile.settings }

        val mutualGuilds = website.loritta.pudding.transaction {
            GuildProfiles.selectAll()
                .where {
                    GuildProfiles.userId eq session.userId and (GuildProfiles.isInGuild eq true)
                }
                .map { it[GuildProfiles.guildId] }
                .toSet()
        }

        val ownedBadgeIds: Set<UUID>
        val earnedGuildBadge: Badge.GuildBadge?

        if (userInfo != null) {
            val profileUserInfoData = website.loritta.profileDesignManager.transformUserToProfileUserInfoData(userInfo, profileSettings)
            val earnedBadges = website.loritta.profileDesignManager.getUserBadges(profileUserInfoData, profile, mutualGuilds, false)
            ownedBadgeIds = earnedBadges.map { it.id }.toSet()
            earnedGuildBadge = earnedBadges.filterIsInstance<Badge.GuildBadge>().firstOrNull { it.id == badgeId }
        } else {
            ownedBadgeIds = emptySet()
            earnedGuildBadge = null
        }

        val lorittaBadge = website.loritta.profileDesignManager.badges
            .filterIsInstance<Badge.LorittaBadge>()
            .firstOrNull { it.id == badgeId }

        val badge: Badge = lorittaBadge ?: earnedGuildBadge ?: run {
            call.respondText("", status = HttpStatusCode.NotFound)
            return
        }

        val isOwned = badge.id in ownedBadgeIds

        val isHidden = website.loritta.transaction {
            HiddenUserBadges.selectAll()
                .where {
                    HiddenUserBadges.userId eq session.userId and (HiddenUserBadges.badgeId eq badge.id)
                }
                .count() != 0L
        }

        call.respondHtmlFragment {
            badgeItemInfo(
                i18nContext,
                badge.id,
                badge.title,
                badge.description,
                isOwned = isOwned,
                isHidden = isHidden,
            )
        }
    }
}
