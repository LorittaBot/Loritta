package net.perfectdreams.loritta.morenitta.websitedashboard

import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.compression.*
import io.ktor.server.request.header
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSessions
import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite.UserPermissionLevel
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.ChooseYourServerUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PostFavoriteGuildUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.PocketLorittaUserDashboardRoute
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
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop.DailyShopUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop.PostBuyDailyShopItemUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop.SSEDailyShopTimerUserDashboardRoute
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
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.CreateProfilePresetsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.DeleteProfilePresetUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.PostApplyProfilePresetUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.PostCreateProfilePresetsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profilepresets.ProfilePresetsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles.GetProfileLayoutUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles.PostApplyProfileLayoutUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.profiles.ProfilesUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.PostBuyShipEffectsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.PostPreBuyShipEffectsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.PostShipEffectsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop.PostSonhosShopBuyUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop.PostSonhosShopApplyCouponUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.ShipEffectsUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop.SonhosShopUserDashboardRoute
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
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
        lateinit var assets: DashboardAssets

        private val logger by HarmonyLoggerFactory.logger {}
        const val WEBSITE_SESSION_COOKIE = "loritta_session"

        fun canManageGuild(g: DiscordLoginUserDashboardRoute.DiscordGuild): Boolean {
            val isAdministrator = g.permissions shr 3 and 1 == 1L
            val isManager = g.permissions shr 5 and 1 == 1L
            return g.owner || isAdministrator || isManager
        }

        fun getUserPermissionLevel(g: DiscordLoginUserDashboardRoute.DiscordGuild): UserPermissionLevel {
            val isAdministrator = g.permissions shr 3 and 1 == 1L
            val isManager = g.permissions shr 5 and 1 == 1L

            return when {
                g.owner -> UserPermissionLevel.OWNER
                isAdministrator -> UserPermissionLevel.ADMINISTRATOR
                isManager -> UserPermissionLevel.MANAGER
                else -> UserPermissionLevel.MEMBER
            }
        }
    }

    val routes = listOf(
        ChooseYourServerUserDashboardRoute(this),
        PocketLorittaUserDashboardRoute(this),
        DiscordLoginUserDashboardRoute(this),
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

        // Special
        UserProfilePreviewDashboardRoute(this),
        UserBackgroundPreviewDashboardRoute(this),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
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

        val server = embeddedServer(CIO, 13004) {
            install(Compression)

            intercept(ApplicationCallPipeline.Setup) {
                call.response.headers.append("Loritta-Cluster", "Loritta Cluster ${loritta.lorittaCluster.id} (${loritta.lorittaCluster.name})")
            }

            routing {
                get("/howdy") {
                    call.respondText("Howdy! Loritta Cluster ${loritta.lorittaCluster.id}")
                }

                for (route in this@LorittaDashboardWebServer.routes) {
                    route.register(this)

                    if (route is DashboardLocalizedRoute && route.getMethod() == HttpMethod.Get) {
                        get(route.originalPath) {
                            val i18nContext = getI18nContextFromCall(call)
                            call.respondRedirect("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}${call.request.uri}")
                        }
                    }
                }

                get("/assets/css/style.css") {
                    call.respondText(assets.cssBundle.content)
                }

                get("/assets/js/frontend.js") {
                    call.respondText(assets.jsBundle.content)
                }

                staticResources("/assets", "/dashboard/static/assets")
            }
        }

        server.start(false)
    }

    /**
     * Gets the user's website session
     */
    suspend fun getSession(call: ApplicationCall): UserSession? {
        val accessTime = OffsetDateTime.now(ZoneOffset.UTC)

        val sessionToken = call.request.cookies[WEBSITE_SESSION_COOKIE] ?: return null

        // We use READ COMMITED to avoid concurrent serialization exceptions when trying to UPDATE
        val sessionData = loritta.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
            val data = UserWebsiteSessions.selectAll()
                .where {
                    UserWebsiteSessions.token eq sessionToken
                }
                .firstOrNull()

            if (data != null) {
                UserWebsiteSessions.update({ UserWebsiteSessions.id eq data[UserWebsiteSessions.id] }) {
                    it[UserWebsiteSessions.lastUsedAt] = accessTime
                }

                data
            } else null
        }

        if (sessionData == null)
            return null

        return UserSession(
            sessionToken,
            sessionData[UserWebsiteSessions.accessToken],
            sessionData[UserWebsiteSessions.userId],
            sessionData[UserWebsiteSessions.username],
            sessionData[UserWebsiteSessions.discriminator],
            sessionData[UserWebsiteSessions.globalName],
            sessionData[UserWebsiteSessions.avatarId]
        )
    }

    fun getI18nContextFromCall(call: ApplicationCall): I18nContext {
        val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
        val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
        var localeId = "en-us"
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
}