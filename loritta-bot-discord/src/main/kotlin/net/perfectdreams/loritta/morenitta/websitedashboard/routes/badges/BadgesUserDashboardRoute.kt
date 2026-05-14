package net.perfectdreams.loritta.morenitta.websitedashboard.routes.badges

import io.ktor.server.application.*
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.HiddenUserBadges
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.components.badgeItemInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trinketInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class BadgesUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/badges") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val lorittaBadges = website.loritta.profileDesignManager.badges.filterIsInstance<Badge.LorittaBadge>()

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
        val guildBadges: List<Badge.GuildBadge>

        if (userInfo != null) {
            val profileUserInfoData = website.loritta.profileDesignManager.transformUserToProfileUserInfoData(userInfo, profileSettings)
            val earnedBadges = website.loritta.profileDesignManager.getUserBadges(profileUserInfoData, profile, mutualGuilds, false)
            ownedBadgeIds = earnedBadges.map { it.id }.toSet()
            guildBadges = earnedBadges.filterIsInstance<Badge.GuildBadge>()
        } else {
            ownedBadgeIds = emptySet()
            guildBadges = emptyList()
        }

        val sortedLorittaBadges = lorittaBadges.sortedWith(
            compareByDescending<Badge.LorittaBadge> { it.id in ownedBadgeIds }
                .thenByDescending { it.priority }
        )

        val initialBadge = sortedLorittaBadges.firstOrNull { it.id in ownedBadgeIds }
            ?: sortedLorittaBadges.firstOrNull()
        val initialIsOwned = initialBadge?.id in ownedBadgeIds
        val initialIsHidden = if (initialBadge != null && initialIsOwned) {
            website.loritta.transaction {
                HiddenUserBadges.selectAll()
                    .where {
                        HiddenUserBadges.userId eq session.userId and (HiddenUserBadges.badgeId eq initialBadge.id)
                    }
                    .count() != 0L
            }
        } else false

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Badges.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.PROFILE_BADGES)
                },
                {
                    div {
                        id = "bundles-content"

                        div(classes = "bought-shop-items-list") {
                            div(classes = "loritta-items-wrapper") {
                                for (guildBadge in guildBadges) {
                                    renderBadgeTile(
                                        i18nContext,
                                        guildBadge.id,
                                        guildBadge.title,
                                        isOwned = true,
                                    )
                                }

                                for (badge in sortedLorittaBadges) {
                                    val owned = badge.id in ownedBadgeIds
                                    renderBadgeTile(
                                        i18nContext,
                                        badge.id,
                                        badge.title,
                                        isOwned = owned,
                                    )
                                }
                            }
                        }

                        trinketInfo(i18nContext) {
                            if (initialBadge != null) {
                                badgeItemInfo(
                                    i18nContext,
                                    initialBadge.id,
                                    initialBadge.title,
                                    initialBadge.description,
                                    isOwned = initialIsOwned,
                                    isHidden = initialIsHidden,
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    private fun FlowContent.renderBadgeTile(
        i18nContext: I18nContext,
        badgeId: UUID,
        title: net.perfectdreams.i18nhelper.core.keydata.StringI18nData,
        isOwned: Boolean,
    ) {
        val classes = buildString {
            append("shop-item-entry rarity-common")
            if (!isOwned) append(" locked")
        }

        div(classes = classes) {
            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/badges/$badgeId"
            attributes["bliss-swap:200"] = "body (innerHTML) -> #trinket-info-content (innerHTML)"
            attributes["bliss-indicator"] = "#trinket-info"

            div {
                style = "display: flex; justify-content: center; align-items: center; aspect-ratio: 1/1; padding: 1em;"

                img {
                    src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/badge-image/$badgeId"
                    attributes["title"] = i18nContext.get(title)
                    attributes["alt"] = i18nContext.get(title)
                    style = "max-width: 100%; max-height: 100%; object-fit: contain;"
                }
            }
        }
    }
}
