package net.perfectdreams.loritta.morenitta.websitedashboard

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.compression.*
import io.ktor.server.request.header
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedDiscordUserIdentifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite.UserPermissionLevel
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Guild
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals.BanAppealsOverrideRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals.BanAppealsFormRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.ChooseYourServerUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordAddBotUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PostFavoriteGuildUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PocketLorittaUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals.PostBanAppealsOverrideRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals.PostBanAppealsRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PostDashboardThemeGuildUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PostLogoutUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PostServerListUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PostUnfavoriteGuildUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PutLorittaSpawnerSettingsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.TwitchAccountCallbackRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.UserBackgroundPreviewDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.UserProfilePreviewDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.apikeys.APIKeysUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.apikeys.PostGenerateAPIKeyUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds.BackgroundsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds.GetBackgroundUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds.PostApplyBackgroundUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.backgrounds.PostUploadBackgroundUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals.BanAppealsOverviewRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals.PostBanAppealsAccountIdsRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop.DailyShopUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop.PostBuyDailyShopItemUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop.SSEDailyShopTimerUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.PostTestMessageGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.added.AddedLorittaGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole.AutoroleGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole.PostAddRoleToListAutoroleGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole.PostRemoveRoleFromListAutoroleGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole.PutAutoroleGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.badge.BadgeGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.badge.PostBadgeImageGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.badge.PutBadgeGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky.AddBlueskyProfileGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky.BlueskyGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky.DeleteBlueskyProfileGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky.EditBlueskyProfileGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky.PostBlueskyProfileGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky.PutBlueskyProfileGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bomdiaecia.BomDiaECiaGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bomdiaecia.PostAddChannelBomDiaECiaGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bomdiaecia.PostRemoveChannelBomDiaECiaGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bomdiaecia.PutBomDiaECiaGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commandchannels.PostAddChannelToListCommandChannelsConfigurationGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commandchannels.CommandChannelsConfigurationGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commandchannels.PostRemoveChannelFromListCommandChannelsConfigurationGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commandchannels.PutCommandChannelsConfigurationGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commands.CommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commands.PutCommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.CreateCustomCommandGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.CustomCommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.DeleteCustomCommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.EditCustomCommandGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.PostCustomCommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.PutCustomCommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.dailymultiplier.DailyMultiplierGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.dailymultiplier.PutDailyMultiplierGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.dailyshoptrinkets.DailyShopTrinketsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.dailyshoptrinkets.PutDailyShopTrinketsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.drops.DropsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.drops.PostDropsInviteGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.drops.PutDropsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.eventlog.EventLogGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.eventlog.PutEventLogGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards.PostAddRoleRewardGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards.PostRemoveRoleRewardGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards.XPRewardsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards.PostXP2LevelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards.PutXPRewardsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.gamersafer.GamerSaferGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.overview.OverviewConfigurationGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker.InviteBlockerGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker.PostAddChannelToListInviteBlockerGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker.PostRemoveChannelFromListInviteBlockerGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker.PutInviteBlockerGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter.MemberCounterChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter.MemberCounterGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter.PostMemberCounterPreviewGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter.PutMemberCounterChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.permissions.PermissionsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.permissions.PutRolePermissionsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.permissions.RolePermissionsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.prefixedcommands.PostPrefixedCommandsPrefixPreviewGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.punishmentlog.PunishmentLogGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.prefixedcommands.PrefixedCommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.prefixedcommands.PutPrefixedCommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.premiumkeys.PostActivatePremiumKeyGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.premiumkeys.PostDeactivatePremiumKeyGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.premiumkeys.PremiumKeysGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.punishmentlog.PutPunishmentLogGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.quirkymode.PutQuirkyModeGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.quirkymode.QuirkyModeGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.reactionevents.PutReactionEventsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.reactionevents.ReactionEventsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.resetxp.PostResetXPGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.resetxp.ResetXPGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.starboard.PutStarboardGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.starboard.PostStarboardStorytimeGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.starboard.StarboardGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.taxfreedays.PutTaxFreeDaysGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.taxfreedays.TaxFreeDaysGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch.AddTwitchChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch.DeletePremiumTwitchTrackGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch.DeleteTwitchChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch.EditTwitchChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch.PostTwitchChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch.PutTwitchChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch.TwitchGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions.PostAddWarnActionGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions.PostRemoveWarnActionGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions.PutWarnActionsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions.WarnActionsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.welcomer.PutWelcomerGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.welcomer.WelcomerGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers.PostAddChannelXPBlockersGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers.PostAddRoleXPBlockersGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers.PostRemoveChannelXPBlockersGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers.PostRemoveRoleXPBlockersGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers.PutXPBlockersGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers.XPBlockersGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpnotifications.PutXPNotificationsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpnotifications.XPNotificationsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprates.PostAddRoleRateGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprates.PutXPRatesGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprates.XPRatesGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards.PostRemoveRoleRateGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube.AddYouTubeChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube.DeleteYouTubeChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube.EditYouTubeChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube.PostYouTubeChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube.PutYouTubeChannelGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube.YouTubeGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.notifications.NotificationsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.notifications.PutNotificationsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.CreateProfilePresetsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.DeleteProfilePresetUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.PostApplyProfilePresetUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.PostCreateProfilePresetsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.ProfilePresetsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles.GetProfileLayoutUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles.PostApplyProfileLayoutUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles.ProfilesUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations.DeleteReputationUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations.GivenReputationsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations.ReceivedReputationsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations.ReputationsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations.ViewGivenReputationUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations.ViewReceivedReputationUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.PostBuyShipEffectsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.PostPreBuyShipEffectsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.PostShipEffectsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop.PostSonhosShopBuyUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop.PostSonhosShopApplyCouponUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.ShipEffectsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop.SonhosShopUserDashboardRoute
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import java.io.File
import java.sql.Connection
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Locale
import kotlin.io.path.readText

/**
 * Loritta's Dashboard Web Server
 */
class LorittaDashboardWebServer(val loritta: LorittaBot) {
    companion object {
        lateinit var INSTANCE: LorittaDashboardWebServer
        lateinit var assets: DashboardAssets

        private val logger by HarmonyLoggerFactory.logger {}
        const val WEBSITE_SESSION_COOKIE = "loritta_session"
        const val WEBSITE_SESSION_COOKIE_MAX_AGE = 86_400 * 90 // 90 days
        const val WEBSITE_SESSION_COOKIE_REFRESH = 86_400 * 30 // 30 days
        // We use ports instead of hosts to be easier to debug locally
        // Because if we used hosts, we would need to manually change the hosts file
        const val DASHBOARD_PORT = 13004
        const val BAN_APPEALS_PORT = 13005

        fun canManageGuild(g: DiscordOAuth2Guild): Boolean {
            val isAdministrator = g.permissions shr 3 and 1 == 1L
            val isManager = g.permissions shr 5 and 1 == 1L
            return g.owner || isAdministrator || isManager
        }

        fun getUserPermissionLevel(g: DiscordOAuth2Guild): UserPermissionLevel {
            val isAdministrator = g.permissions shr 3 and 1 == 1L
            val isManager = g.permissions shr 5 and 1 == 1L

            return when {
                g.owner -> UserPermissionLevel.OWNER
                isAdministrator -> UserPermissionLevel.ADMINISTRATOR
                isManager -> UserPermissionLevel.MANAGER
                else -> UserPermissionLevel.MEMBER
            }
        }

        val favicon192x192Hash = DigestUtils.md5Hex(LorittaDashboardWebServer::class.java.getResourceAsStream("/dashboard/static/assets/images/favicon-192x192.png"))
    }

    val advertisementBaits = listOf(
        AdvertisementBait(
            "/assets/js/fuckadblock.js",
            "isUserUsingUBlockOrigin",
        ),
        AdvertisementBait(
            "/jquery.adi.js",
            "isUserUsingEasyList",
        ),
        AdvertisementBait(
            "/ads/anuncio.js",
            "isUserUsingEasyListPortuguese",
        ),
        AdvertisementBait(
            "/publi.js",
            "isUserUsingAdGuardSpanishPortuguese",
        ),
        AdvertisementBait(
            "/jquery.adx.js",
            "isUserUsingBraveShields",
        )
    )

    val dashboardRoutes = listOf(
        ChooseYourServerUserDashboardRoute(this),
        PocketLorittaUserDashboardRoute(this),
        SonhosShopUserDashboardRoute(this),
        PostSonhosShopApplyCouponUserDashboardRoute(this),
        PostSonhosShopBuyUserDashboardRoute(this),
        PostServerListUserDashboardRoute(this),
        PostFavoriteGuildUserDashboardRoute(this),
        PostUnfavoriteGuildUserDashboardRoute(this),
        PostDashboardThemeGuildUserDashboardRoute(this),
        PostLogoutUserDashboardRoute(this),

        // Loritta Spawner
        PutLorittaSpawnerSettingsUserDashboardRoute(this),

        // Ship Effects
        ShipEffectsUserDashboardRoute(this),
        PostShipEffectsUserDashboardRoute(this),
        PostPreBuyShipEffectsUserDashboardRoute(this),
        PostBuyShipEffectsUserDashboardRoute(this),

        // Profile Presets
        ProfilePresetsUserDashboardRoute(this),
        CreateProfilePresetsUserDashboardRoute(this),
        PostCreateProfilePresetsUserDashboardRoute(this),
        DeleteProfilePresetUserDashboardRoute(this),
        PostApplyProfilePresetUserDashboardRoute(this),

        // Daily Shop
        DailyShopUserDashboardRoute(this),
        PostBuyDailyShopItemUserDashboardRoute(this),
        SSEDailyShopTimerUserDashboardRoute(this),

        // API Keys
        APIKeysUserDashboardRoute(this),
        PostGenerateAPIKeyUserDashboardRoute(this),

        // Profile Layouts
        ProfilesUserDashboardRoute(this),
        GetProfileLayoutUserDashboardRoute(this),
        PostApplyProfileLayoutUserDashboardRoute(this),

        // Backgrounds
        BackgroundsUserDashboardRoute(this),
        GetBackgroundUserDashboardRoute(this),
        PostApplyBackgroundUserDashboardRoute(this),
        PostUploadBackgroundUserDashboardRoute(this),

        // Reputations
        ReputationsUserDashboardRoute(this),
        GivenReputationsUserDashboardRoute(this),
        ViewGivenReputationUserDashboardRoute(this),
        DeleteReputationUserDashboardRoute(this),
        ReceivedReputationsUserDashboardRoute(this),
        ViewGivenReputationUserDashboardRoute(this),
        ViewReceivedReputationUserDashboardRoute(this),

        // Notifications
        NotificationsGuildDashboardRoute(this),
        PutNotificationsGuildDashboardRoute(this),

        // Starboard
        StarboardGuildDashboardRoute(this),
        PostStarboardStorytimeGuildDashboardRoute(this),
        PutStarboardGuildDashboardRoute(this),

        // Reaction Events
        ReactionEventsGuildDashboardRoute(this),
        PutReactionEventsGuildDashboardRoute(this),

        // Overview
        OverviewConfigurationGuildDashboardRoute(this),

        // Command Channels
        CommandChannelsConfigurationGuildDashboardRoute(this),
        PostAddChannelToListCommandChannelsConfigurationGuildDashboardRoute(this),
        PostRemoveChannelFromListCommandChannelsConfigurationGuildDashboardRoute(this),
        PutCommandChannelsConfigurationGuildDashboardRoute(this),

        // Event Log
        EventLogGuildDashboardRoute(this),
        PutEventLogGuildDashboardRoute(this),

        // Daily Shop Trinkets
        DailyShopTrinketsGuildDashboardRoute(this),
        PutDailyShopTrinketsGuildDashboardRoute(this),

        // Custom Commands
        CustomCommandsGuildDashboardRoute(this),
        EditCustomCommandGuildDashboardRoute(this),
        CreateCustomCommandGuildDashboardRoute(this),
        PostCustomCommandsGuildDashboardRoute(this),
        PutCustomCommandsGuildDashboardRoute(this),
        DeleteCustomCommandsGuildDashboardRoute(this),

        // Loritta Commands
        CommandsGuildDashboardRoute(this),
        PutCommandsGuildDashboardRoute(this),

        // Prefixed Commands
        PrefixedCommandsGuildDashboardRoute(this),
        PutPrefixedCommandsGuildDashboardRoute(this),
        PostPrefixedCommandsPrefixPreviewGuildDashboardRoute(this),

        // Bom Dia & Cia
        BomDiaECiaGuildDashboardRoute(this),
        PutBomDiaECiaGuildDashboardRoute(this),

        // Quirky Mode
        QuirkyModeGuildDashboardRoute(this),
        PutQuirkyModeGuildDashboardRoute(this),

        // GamerSafer
        GamerSaferGuildDashboardRoute(this),

        // Invite Blocker
        InviteBlockerGuildDashboardRoute(this),
        PutInviteBlockerGuildDashboardRoute(this),
        PostAddChannelToListInviteBlockerGuildDashboardRoute(this),
        PostRemoveChannelFromListInviteBlockerGuildDashboardRoute(this),

        // Punishment Log
        PunishmentLogGuildDashboardRoute(this),
        PutPunishmentLogGuildDashboardRoute(this),

        // Premium Keys
        PremiumKeysGuildDashboardRoute(this),
        PostActivatePremiumKeyGuildDashboardRoute(this),
        PostDeactivatePremiumKeyGuildDashboardRoute(this),

        // Daily Multiplier
        DailyMultiplierGuildDashboardRoute(this),
        PutDailyMultiplierGuildDashboardRoute(this),

        // YouTube
        YouTubeGuildDashboardRoute(this),
        AddYouTubeChannelGuildDashboardRoute(this),
        PostYouTubeChannelGuildDashboardRoute(this),
        DeleteYouTubeChannelGuildDashboardRoute(this),
        PutYouTubeChannelGuildDashboardRoute(this),
        EditYouTubeChannelGuildDashboardRoute(this),

        // Bluesky
        BlueskyGuildDashboardRoute(this),
        AddBlueskyProfileGuildDashboardRoute(this),
        PostBlueskyProfileGuildDashboardRoute(this),
        DeleteBlueskyProfileGuildDashboardRoute(this),
        EditBlueskyProfileGuildDashboardRoute(this),
        PutBlueskyProfileGuildDashboardRoute(this),

        // Twitch
        TwitchGuildDashboardRoute(this),
        TwitchAccountCallbackRoute(this),
        AddTwitchChannelGuildDashboardRoute(this),
        PostTwitchChannelGuildDashboardRoute(this),
        DeleteTwitchChannelGuildDashboardRoute(this),
        EditTwitchChannelGuildDashboardRoute(this),
        PutTwitchChannelGuildDashboardRoute(this),
        DeletePremiumTwitchTrackGuildDashboardRoute(this),

        // Autorole
        AutoroleGuildDashboardRoute(this),
        PostAddRoleToListAutoroleGuildDashboardRoute(this),
        PostRemoveRoleFromListAutoroleGuildDashboardRoute(this),
        PutAutoroleGuildDashboardRoute(this),

        // Custom Badge
        BadgeGuildDashboardRoute(this),
        PutBadgeGuildDashboardRoute(this),
        PostBadgeImageGuildDashboardRoute(this),

        // Welcomer
        WelcomerGuildDashboardRoute(this),
        PutWelcomerGuildDashboardRoute(this),

        // Permissions
        PermissionsGuildDashboardRoute(this),
        RolePermissionsGuildDashboardRoute(this),
        PutRolePermissionsGuildDashboardRoute(this),

        // Member Counter
        MemberCounterGuildDashboardRoute(this),
        MemberCounterChannelGuildDashboardRoute(this),
        PutMemberCounterChannelGuildDashboardRoute(this),
        PostMemberCounterPreviewGuildDashboardRoute(this),

        // Warn Actions
        WarnActionsGuildDashboardRoute(this),
        PostAddWarnActionGuildDashboardRoute(this),
        PutWarnActionsGuildDashboardRoute(this),
        PostRemoveWarnActionGuildDashboardRoute(this),

        // Experience Rewards
        XPRewardsGuildDashboardRoute(this),
        PostXP2LevelGuildDashboardRoute(this),
        PostAddRoleRewardGuildDashboardRoute(this),
        PostRemoveRoleRewardGuildDashboardRoute(this),
        PutXPRewardsGuildDashboardRoute(this),

        // Reset XP
        ResetXPGuildDashboardRoute(this),
        PostResetXPGuildDashboardRoute(this),

        // XP Notifications
        XPNotificationsGuildDashboardRoute(this),
        PutXPNotificationsGuildDashboardRoute(this),

        // XP Rates
        XPRatesGuildDashboardRoute(this),
        PostAddRoleRateGuildDashboardRoute(this),
        PostRemoveRoleRateGuildDashboardRoute(this),
        PutXPRatesGuildDashboardRoute(this),

        // XP Blockers
        XPBlockersGuildDashboardRoute(this),
        PostAddChannelXPBlockersGuildDashboardRoute(this),
        PostRemoveChannelXPBlockersGuildDashboardRoute(this),
        PostAddRoleXPBlockersGuildDashboardRoute(this),
        PostRemoveRoleXPBlockersGuildDashboardRoute(this),
        PutXPBlockersGuildDashboardRoute(this),

        // Drops
        DropsGuildDashboardRoute(this),
        PutDropsGuildDashboardRoute(this),
        PostDropsInviteGuildDashboardRoute(this),
        PostAddChannelBomDiaECiaGuildDashboardRoute(this),
        PostRemoveChannelBomDiaECiaGuildDashboardRoute(this),

        // Tax Free Days
        TaxFreeDaysGuildDashboardRoute(this),
        PutTaxFreeDaysGuildDashboardRoute(this),

        // Special
        DiscordLoginUserDashboardRoute(this),
        DiscordAddBotUserDashboardRoute(this),
        AddedLorittaGuildDashboardRoute(this),
        PostTestMessageGuildDashboardRoute(this),
        UserProfilePreviewDashboardRoute(this),
        UserBackgroundPreviewDashboardRoute(this),
    )

    val appealsRoute = listOf(
        // Ban Appeal
        BanAppealsOverviewRoute(this),
        BanAppealsFormRoute(this),
        BanAppealsOverrideRoute(this),
        PostBanAppealsOverrideRoute(this),
        PostBanAppealsRoute(this),
        PostBanAppealsAccountIdsRoute(this)
    )

    val oauth2Manager = DiscordOAuth2Manager(this.loritta.http, this.loritta.config.loritta.discord.baseUrl)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        INSTANCE = this

        val jsPath = loritta.config.loritta.dashboard.jsPath
        val jsBundle = if (jsPath != null) {
            DashboardBundle.FileSystemBundle(File(jsPath))
        } else {
            DashboardBundle.CachedBundle(LorittaDashboardWebServer::class.getPathFromResources("/dashboard/js/frontend.js")!!.readText(Charsets.UTF_8))
        }

        val cssPath = loritta.config.loritta.dashboard.cssPath
        val cssBundle = if (cssPath != null) {
            DashboardBundle.FileSystemBundle(File(cssPath))
        } else {
            DashboardBundle.CachedBundle(LorittaDashboardWebServer::class.getPathFromResources("/dashboard/css/style.css")!!.readText(Charsets.UTF_8))
        }

        assets = DashboardAssets(
            jsBundle,
            cssBundle
        )

        val server = embeddedServer(
            Netty,
            configure = {
                connectors.add(EngineConnectorBuilder().apply {
                    host = "0.0.0.0"
                    port = DASHBOARD_PORT
                })

                connectors.add(EngineConnectorBuilder().apply {
                    host = "0.0.0.0"
                    port = BAN_APPEALS_PORT
                })
            }
        ) {
            install(Compression)

            intercept(ApplicationCallPipeline.Setup) {
                call.response.headers.append("Loritta-Cluster", "Loritta Cluster ${loritta.lorittaCluster.id} (${loritta.lorittaCluster.name})")
            }

            routing {
                get("/howdy") {
                    call.respondText("Howdy! Loritta Cluster ${loritta.lorittaCluster.id}")
                }

                // DO NOT USE port! We NEED to use localPort because port relies on the "Host" header!
                localPort(DASHBOARD_PORT) {
                    for (route in this@LorittaDashboardWebServer.dashboardRoutes) {
                        route.register(this)

                        if (route is DashboardLocalizedRoute && route.getMethod() == HttpMethod.Get) {
                            get(route.originalPath) {
                                val i18nContext = getI18nContextFromCall(call)
                                call.respondRedirect("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}${call.request.uri}")
                            }
                        }
                    }
                }

                localPort(BAN_APPEALS_PORT) {
                    for (route in appealsRoute) {
                        route.register(this)

                        if (route is DashboardLocalizedRoute && route.getMethod() == HttpMethod.Get) {
                            get(route.originalPath) {
                                val i18nContext = getI18nContextFromCall(call)
                                call.respondRedirect("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}${call.request.uri}")
                            }
                        }
                    }
                }

                get("/assets/css/style.css") {
                    call.respondText(
                        assets.cssBundle.content,
                        contentType = ContentType.Text.CSS
                    )
                }

                get("/assets/js/frontend.js") {
                    call.respondText(
                        assets.jsBundle.content,
                        contentType = ContentType.Application.JavaScript
                    )
                }

                // ad baits :3
                for (advertismentBait in this@LorittaDashboardWebServer.advertisementBaits) {
                    get(advertismentBait.path) {
                        call.respondText(
                            """window.${advertismentBait.variableName} = false;""",
                            contentType = ContentType.Application.JavaScript
                        )
                    }
                }

                staticResources("/assets", "/dashboard/static/assets")
            }
        }

        server.start(false)
    }

    /**
     * Gets the user's website session
     */
    suspend fun getSession(call: ApplicationCall): LorittaUserSession? {
        val accessTime = OffsetDateTime.now(ZoneOffset.UTC)

        val sessionToken = call.request.cookies[WEBSITE_SESSION_COOKIE] ?: return null

        // We use READ COMMITED to avoid concurrent serialization exceptions when trying to UPDATE
        val sessionDataAndCachedUserIdentification = loritta.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
            val data = UserWebsiteSessions.selectAll()
                .where {
                    UserWebsiteSessions.token eq sessionToken
                }
                .firstOrNull()

            if (data != null) {
                val cachedUserIdentification = CachedDiscordUserIdentifications.selectAll()
                    .where {
                        CachedDiscordUserIdentifications.id eq data[UserWebsiteSessions.userId]
                    }
                    .firstOrNull()

                if (cachedUserIdentification == null) {
                    // If we don't have any cached user identification, remove the session from the database!
                    UserWebsiteSessions.deleteWhere {
                        UserWebsiteSessions.id eq data[UserWebsiteSessions.id]
                    }
                    return@transaction null
                }

                val timeToRefreshCookie = data[UserWebsiteSessions.cookieSetAt]
                    .plusSeconds(data[UserWebsiteSessions.cookieMaxAge].toLong())
                    .minusSeconds(WEBSITE_SESSION_COOKIE_REFRESH.toLong())

                var setCookie = false
                if (accessTime >= timeToRefreshCookie)
                    setCookie = true

                UserWebsiteSessions.update({ UserWebsiteSessions.id eq data[UserWebsiteSessions.id] }) {
                    it[UserWebsiteSessions.lastUsedAt] = accessTime

                    if (setCookie) {
                        it[UserWebsiteSessions.cookieSetAt] = accessTime
                        it[UserWebsiteSessions.cookieMaxAge] = WEBSITE_SESSION_COOKIE_MAX_AGE
                    }
                }

                Triple(data, cachedUserIdentification, setCookie)
            } else null
        }

        if (sessionDataAndCachedUserIdentification == null) {
            revokeLorittaSessionCookie(call)
            return null
        }

        val sessionData = sessionDataAndCachedUserIdentification.first
        val cachedUserIdentification = sessionDataAndCachedUserIdentification.second
        val setCookie = sessionDataAndCachedUserIdentification.third

        if (setCookie) {
            setLorittaSessionCookie(
                call.response.cookies,
                sessionToken,
                maxAge = WEBSITE_SESSION_COOKIE_MAX_AGE
            )
        }

        return LorittaUserSession(
            loritta,
            this,
            oauth2Manager,
            loritta.config.loritta.discord.applicationId,
            loritta.config.loritta.discord.clientSecret,
            sessionData[UserWebsiteSessions.token],
            sessionData[UserWebsiteSessions.userId],
            DiscordUserCredentials(
                sessionData[UserWebsiteSessions.accessToken],
                sessionData[UserWebsiteSessions.refreshToken],
                sessionData[UserWebsiteSessions.refreshedAt].toInstant(),
                sessionData[UserWebsiteSessions.expiresIn],
            ),
            UserSession.UserIdentification(
                cachedUserIdentification[CachedDiscordUserIdentifications.id].value,
                cachedUserIdentification[CachedDiscordUserIdentifications.username],
                cachedUserIdentification[CachedDiscordUserIdentifications.discriminator],
                cachedUserIdentification[CachedDiscordUserIdentifications.avatarId],
                cachedUserIdentification[CachedDiscordUserIdentifications.globalName],
                cachedUserIdentification[CachedDiscordUserIdentifications.mfaEnabled],
                cachedUserIdentification[CachedDiscordUserIdentifications.banner],
                cachedUserIdentification[CachedDiscordUserIdentifications.accentColor],
                cachedUserIdentification[CachedDiscordUserIdentifications.locale],
                cachedUserIdentification[CachedDiscordUserIdentifications.email],
                cachedUserIdentification[CachedDiscordUserIdentifications.verified],
                cachedUserIdentification[CachedDiscordUserIdentifications.premiumType],
                cachedUserIdentification[CachedDiscordUserIdentifications.flags],
                cachedUserIdentification[CachedDiscordUserIdentifications.publicFlags]
            )
        )
    }

    fun getI18nContextFromCall(call: ApplicationCall): I18nContext {
        val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
        val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
        var localeId = "en"
        for (range in ranges) {
            localeId = range.range.lowercase()
            if (localeId == "pt-br" || localeId == "pt") {
                localeId = "pt"
            }
            if (localeId == "en") {
                localeId = "en"
            }
        }

        return loritta.languageManager.getI18nContextById(localeId)
    }

    fun setLorittaSessionCookie(
        cookies: ResponseCookies,
        value: String,
        maxAge: Int
    ) {
        cookies.append(
            WEBSITE_SESSION_COOKIE,
            value,
            path = "/", // Available in any path of the domain
            domain = loritta.config.loritta.dashboard.cookieDomain,
            // secure = true, // Only sent via HTTPS
            httpOnly = true, // Disable JS access
            maxAge = maxAge.toLong()
        )
    }

    fun revokeLorittaSessionCookie(call: ApplicationCall) {
        setLorittaSessionCookie(
            call.response.cookies,
            "",
            maxAge = 0 // Remove it!
        )
    }

    fun updateCachedDiscordUserIdentification(userIdentification: DiscordOAuth2UserIdentification) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        CachedDiscordUserIdentifications.upsert(
            CachedDiscordUserIdentifications.id,
            onUpdateExclude = listOf(CachedDiscordUserIdentifications.createdAt)
        ) {
            it[CachedDiscordUserIdentifications.createdAt] = now
            it[CachedDiscordUserIdentifications.updatedAt] = now

            it[CachedDiscordUserIdentifications.id] = userIdentification.id
            it[CachedDiscordUserIdentifications.username] = userIdentification.username
            it[CachedDiscordUserIdentifications.globalName] = userIdentification.globalName
            it[CachedDiscordUserIdentifications.discriminator] = userIdentification.discriminator
            it[CachedDiscordUserIdentifications.avatarId] = userIdentification.avatar
            it[CachedDiscordUserIdentifications.email] = userIdentification.email
            it[CachedDiscordUserIdentifications.mfaEnabled] = userIdentification.mfaEnabled
            it[CachedDiscordUserIdentifications.accentColor] = userIdentification.accentColor
            it[CachedDiscordUserIdentifications.locale] = userIdentification.locale
            it[CachedDiscordUserIdentifications.verified] = userIdentification.verified
            it[CachedDiscordUserIdentifications.email] = userIdentification.email
            it[CachedDiscordUserIdentifications.flags] = userIdentification.flags
            it[CachedDiscordUserIdentifications.premiumType] = userIdentification.premiumType
            it[CachedDiscordUserIdentifications.publicFlags] = userIdentification.publicFlags
        }
    }

    fun shouldDisplayAds(call: ApplicationCall, userPremiumPlan: UserPremiumPlans, overrideAdsResult: Boolean?): Boolean {
        if (overrideAdsResult != null)
            return overrideAdsResult

        // Used to force ads to be displayed (useful for debugging)
        val ilovetvCookie = call.request.cookies["loritta_ilovetv"]
        if (ilovetvCookie != null && ilovetvCookie == "true")
            return true

        return userPremiumPlan.displayAds
    }
}