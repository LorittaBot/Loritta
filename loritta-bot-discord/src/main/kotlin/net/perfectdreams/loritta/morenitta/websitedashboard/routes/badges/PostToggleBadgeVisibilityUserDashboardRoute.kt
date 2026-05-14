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
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.UUID

class PostToggleBadgeVisibilityUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/badges/{badgeId}/toggle-visibility") {
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

        // Must verify the user actually owns this badge before allowing toggle.
        val ownedBadges: List<Badge> = if (userInfo != null) {
            val profileUserInfoData = website.loritta.profileDesignManager.transformUserToProfileUserInfoData(userInfo, profileSettings)
            website.loritta.profileDesignManager.getUserBadges(profileUserInfoData, profile, mutualGuilds, false)
        } else {
            emptyList()
        }

        val badge: Badge = ownedBadges.firstOrNull { it.id == badgeId } ?: run {
            call.respondText("", status = HttpStatusCode.Forbidden)
            return
        }

        val nowHidden = website.loritta.newSuspendedTransaction {
            val existing = HiddenUserBadges.selectAll()
                .where {
                    HiddenUserBadges.userId eq session.userId and (HiddenUserBadges.badgeId eq badge.id)
                }
                .limit(1)
                .firstOrNull()

            if (existing != null) {
                HiddenUserBadges.deleteWhere {
                    HiddenUserBadges.id eq existing[HiddenUserBadges.id]
                }
                false
            } else {
                HiddenUserBadges.insert {
                    it[HiddenUserBadges.userId] = session.userId
                    it[HiddenUserBadges.badgeId] = badge.id
                    it[HiddenUserBadges.hiddenAt] = Instant.now()
                }
                true
            }
        }

        call.respondHtmlFragment {
            badgeItemInfo(
                i18nContext,
                badge.id,
                badge.title,
                badge.description,
                isOwned = true,
                isHidden = nowHidden,
            )

            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    if (nowHidden) {
                        i18nContext.get(I18nKeysData.Commands.Command.Profilebadges.BadgeHasBeenHidden)
                    } else {
                        i18nContext.get(I18nKeysData.Commands.Command.Profilebadges.BadgeHasBeenUnhidden)
                    }
                )
            )
        }
    }
}
