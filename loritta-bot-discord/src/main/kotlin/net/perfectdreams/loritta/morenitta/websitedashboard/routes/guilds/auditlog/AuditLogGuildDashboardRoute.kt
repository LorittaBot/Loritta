package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.auditlog

import io.ktor.server.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.AuditLogEntries
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeader
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardsWithHeader
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButtonLink
import net.perfectdreams.loritta.morenitta.websitedashboard.components.emptySection
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.loritta.morenitta.websitedashboard.components.swapRightSidebarContentsAttributes
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import kotlin.collections.plus
import kotlin.math.ceil

class AuditLogGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/audit-log") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
        val pageZeroIndexed = page - 1

        val (totalChanges, entries) = website.loritta.transaction {
            val totalChanges = AuditLogEntries.selectAll()
                .where {
                    AuditLogEntries.guildId eq guild.idLong
                }
                .count()

            val entries = AuditLogEntries.selectAll()
                .where {
                    AuditLogEntries.guildId eq guild.idLong
                }
                .orderBy(AuditLogEntries.changedAt, SortOrder.DESC)
                .limit(100)
                .offset(pageZeroIndexed * 100L)
                .toList()

            Pair(totalChanges, entries)
        }

        val totalPages = ceil(totalChanges / WebAuditLogUtils.MAX_ENTRIES_PER_PAGE.toDouble())

        val userInformations = entries.map {
            it[AuditLogEntries.userId]
        }.distinct().map { userId ->
            call.async {
                website.loritta.lorittaShards.retrieveUserInfoById(userId)
            }
        }.awaitAll().filterNotNull()

        val now = Instant.now()

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.AuditLog.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.AUDIT_LOG)
                },
                {
                    heroWrapper {
                        heroText {
                            h1 {
                                text(i18nContext.get(DashboardI18nKeysData.AuditLog.Title))
                            }

                            p {
                                text(i18nContext.get(DashboardI18nKeysData.AuditLog.Description))
                            }
                        }
                    }

                    hr {}

                    div {
                        id = "audit-log-entries"

                        cardsWithHeader {
                            cardHeader {
                                cardHeaderInfo {
                                    cardHeaderTitle {
                                        text(i18nContext.get(DashboardI18nKeysData.AuditLog.Changes.Title))
                                    }

                                    cardHeaderDescription {
                                        text(i18nContext.get(DashboardI18nKeysData.AuditLog.Changes.Changes(totalChanges)))
                                    }
                                }
                            }

                            if (entries.isNotEmpty()) {
                                div(classes = "cards") {
                                    for (entry in entries) {
                                        val userInfo = userInformations.firstOrNull {
                                            it.id == entry[AuditLogEntries.userId]
                                        }

                                        div(classes = "card") {
                                            style = "flex-direction: row; align-items: center; gap: 0.5em;"

                                            div {
                                                style = "flex-grow: 1; display: flex;\n" +
                                                        "  align-items: center;\n" +
                                                        "  flex-direction: row;\n" +
                                                        "  gap: 16px;"

                                                img(src = userInfo?.effectiveAvatarUrl) {
                                                    style = "border-radius: 99999px;"
                                                    width = "48"
                                                    height = "48"
                                                }

                                                div {
                                                    style = "display: flex; flex-direction: column;"

                                                    div {
                                                        span {
                                                            style = "font-weight: bold;"

                                                            text(
                                                                when (entry[AuditLogEntries.trackedChangeType]) {
                                                                    TrackedChangeType.RESET_XP -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ResetXp(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CREATED_BLUESKY_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.CreatedBlueskyTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_BLUESKY_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedBlueskyTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.DELETED_BLUESKY_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.DeletedBlueskyTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CREATED_TWITCH_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.CreatedTwitchTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.EDITED_TWITCH_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedTwitchTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.DELETED_TWITCH_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.DeletedTwitchTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CREATED_YOUTUBE_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.CreatedYouTubeTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_YOUTUBE_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedYouTubeTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.DELETED_YOUTUBE_TRACK -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.DeletedYouTubeTrack(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CREATED_CUSTOM_COMMAND -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.CreatedCustomCommand(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.EDITED_CUSTOM_COMMAND -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedCustomCommand(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.DELETED_CUSTOM_COMMAND -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.DeletedCustomCommand(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.ACTIVATED_PREMIUM_KEY -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ActivatedPremiumKey(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.DEACTIVATED_PREMIUM_KEY -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.DeactivatedPremiumKey(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_AUTOROLE -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedAutorole(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_CUSTOM_BADGE -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedCustomBadge(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_CUSTOM_BADGE_IMAGE -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedCustomBadgeImage(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_BOM_DIA_E_CIA -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedBomDiaECia(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_COMMAND_CHANNELS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedCommandChannels(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_COMMANDS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedCommands(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_DAILY_MULTIPLIER -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedDailyMultiplier(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_DAILY_SHOP_TRINKETS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedDailyShopTrinkets(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_DROPS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedDrops(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_EVENT_LOG -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedEventLog(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_INVITE_BLOCKER -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedInviteBlocker(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_MEMBER_COUNTER -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedMemberCounter(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_ROLE_PERMISSIONS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedRolePermissions(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_PREFIXED_COMMANDS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedPrefixedCommands(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_PUNISHMENT_LOG -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedPunishmentLog(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_QUIRKY_MODE -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedQuirkyMode(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_REACTION_EVENTS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedReactionEvents(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_STARBOARD -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedStarboard(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_TAX_FREE_DAYS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedTaxFreeDays(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_WARN_ACTIONS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedWarnActions(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_JOIN_LEAVE_MESSAGES -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedJoinLeaveMessages(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_XP_BLOCKERS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedXpBlockers(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_XP_NOTIFICATIONS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedXpNotifications(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_XP_RATES -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedXpRates(userInfo?.name ?: "???"))
                                                                    TrackedChangeType.CHANGED_XP_REWARDS -> i18nContext.get(DashboardI18nKeysData.AuditLog.Types.ChangedXpRewards(userInfo?.name ?: "???"))
                                                                }
                                                            )
                                                        }

                                                        div {
                                                            text(
                                                                DateUtils.formatDateDiff(
                                                                    i18nContext,
                                                                    entry[AuditLogEntries.changedAt].toInstant(),
                                                                    now,
                                                                    maxParts = 2
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    div {
                                        style = "display: flex; gap: 16px; justify-content: space-between;"

                                        discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = if (page == 1) null else "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/audit-log?page=${page - 1}") {
                                            classes += "text-with-icon"
                                            if (page == 1) {
                                                attributes["aria-disabled"] = "true"
                                            } else {
                                                swapRightSidebarContentsAttributes()
                                            }

                                            svgIcon(SVGIcons.CaretLeft)

                                            text("Voltar")
                                        }

                                        discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = if (page >= totalPages) null else "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/audit-log?page=${page + 1}") {
                                            classes += "text-with-icon"
                                            if (page >= totalPages) {
                                                attributes["aria-disabled"] = "true"
                                            } else {
                                                swapRightSidebarContentsAttributes()
                                            }
                                            svgIcon(SVGIcons.CaretRight)

                                            text("Próximo")
                                        }
                                    }
                                }
                            } else {
                                emptySection(i18nContext)
                            }
                        }
                    }
                }
            )
        }
    }
}