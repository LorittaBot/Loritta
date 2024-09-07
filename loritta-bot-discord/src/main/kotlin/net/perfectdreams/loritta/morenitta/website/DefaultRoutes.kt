package net.perfectdreams.loritta.morenitta.website

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.GetDailyShopRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostBundlesRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostTransferBalanceExternalRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.languages.GetLanguageInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.twitch.GetTwitchInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.youtube.GetChannelInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.DashboardRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.PostFavoriteGuildRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.*
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.*
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.commands.ConfigureCommandsRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.commands.PutConfigureCommandsRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.*
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.welcomer.ConfigureWelcomerRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.welcomer.PatchWelcomerRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.*
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.*
import net.perfectdreams.loritta.morenitta.website.routes.sponsors.SponsorsRedirectRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.UserReputationRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.*

object DefaultRoutes {
	fun defaultRoutes(loritta: LorittaBot, website: LorittaWebsite) = listOf(
		// ===[ USER ROUTES ]===
		CommunityGuidelinesRoute(loritta),
		FanArtsArtistRoute(loritta),
		FanArtsRoute(loritta),
		SponsorsRoute(loritta),
		SponsorsRedirectRoute(loritta),
		TermsOfServiceRoute(loritta),
		DailyRoute(loritta),
		DonateRoute(loritta),

		// HTTP API Docs
		LoriDevelopersDocsRoute(loritta),
		PostTestLoriDevelopersDocsEndpointRoute(loritta),
		PostCreateObjectTemplateLoriDevelopersDocsRoute(loritta),
		PostDeleteObjectTemplateLoriDevelopersDocsRoute(loritta),
		GetLorifetchStatsSSERoute(loritta),

		// Dashboard
		DashboardRoute(loritta),
		PostFavoriteGuildRoute(loritta),
		ConfigureGeneralRoute(loritta),
		AuditLogRoute(loritta),
		ConfigureAutoroleRoute(loritta),
		ConfigureCommandsRoute(loritta),
		PutConfigureCommandsRoute(loritta),
		ConfigureLegacyCommandsRoute(loritta),
		ConfigureCustomBadgeRoute(loritta),
		ConfigureDailyMultiplierRoute(loritta),
		ConfigureEconomyRoute(loritta),
		ConfigureEventLogRoute(loritta),
		PostConfigureEventLogRoute(loritta),
		ConfigureInviteBlockerRoute(loritta),
		ConfigureLevelUpRoute(loritta),

		ConfigureTwitchRoute(loritta),
		GetConfigureTwitchTrackRoute(loritta),
		GetAddTwitchTrackRoute(loritta),
		GetAddByLoginTwitchTrackRoute(loritta),
		PutTwitchTrackRoute(loritta),
		PatchTwitchTrackRoute(loritta),
		DeleteTwitchTrackRoute(loritta),
		DeleteTwitchPremiumTrackRoute(loritta),
		PutTwitchPremiumTrackRoute(loritta),
		TwitchAccountCallbackRoute(loritta),

		ConfigureMemberCounterRoute(loritta),
		ConfigureMiscellaneousRoute(loritta),
		ConfigureModerationRoute(loritta),
		ConfigurePermissionsRoute(loritta),
		ConfigurePremiumKeyRoute(loritta),
		ConfigureStarboardRoute(loritta),
		ConfigureTrackedTwitterAccountsRoute(loritta),
		ConfigureWelcomerRoute(loritta),
		PatchWelcomerRoute(loritta),

		ConfigureYouTubeRoute(loritta),
		GetAddYouTubeTrackRoute(loritta),
		GetConfigureYouTubeTrackRoute(loritta),
		PutYouTubeTrackRoute(loritta),
		PatchYouTubeTrackRoute(loritta),
		DeleteYouTubeTrackRoute(loritta),

		ConfigureNashornCommandsRoute(loritta),
		ConfigureCustomCommandsRoute(loritta),

		ConfigureBlueskyRoute(loritta),
		GetAddBlueskyTrackRoute(loritta),
		GetConfigureBlueskyTrackRoute(loritta),
		DeleteBlueskyTrackRoute(loritta),
		PutBlueskyTrackRoute(loritta),
		PatchBlueskyTrackRoute(loritta),

		PostTestMessageRoute(loritta),

		// Reps
		UserReputationRoute(loritta),

		// Profiles
		ProfileListRoute(loritta),
		ShipEffectsRoute(loritta),
		PostShipEffectsRoute(loritta),
		PostPreBuyShipEffectRoute(loritta),
		PostBuyShipEffectRoute(loritta),
		SonhosShopRoute(loritta),
		PostSonhosShopRoute(loritta),
		BackgroundsListRoute(loritta),
		AllBackgroundsListRoute(loritta),
		DailyShopRoute(loritta),
		PostSpawnPocketLorittaRoute(loritta),
		PostClearPocketLorittaRoute(loritta),
		PocketLorittaRoute(loritta),
		PostDashboardThemeRoute(loritta),
		LorittaUserAppRoute(loritta),
		LorittaUserAPIKeysRoute(loritta),
		PostGenerateNewLorittaUserAPIKeyRoute(loritta),

		// ===[ API ROUTES ]===
		// Callbacks
		GetPubSubHubbubCallbackRoute(loritta),
		PostDiscordBotsCallbackRoute(loritta),
		PostPubSubHubbubCallbackRoute(loritta),
		PostTwitchEventSubCallbackRoute(loritta),
		PostPerfectPaymentsCallbackRoute(loritta),
		CreateWebhookRoute(loritta),

		// Economy
		PostTransferBalanceExternalRoute(loritta),
		PostBundlesRoute(loritta),
		GetDailyShopRoute(loritta),
		PostBuyDailyShopItemRoute(loritta),

		// Guild
		GetGuildInfoRoute(loritta),
		GetGuildWebAuditLogRoute(loritta),
		GetMembersWithPermissionsInGuildRoute(loritta),
		GetMembersWithRolesInGuildRoute(loritta),
		GetServerConfigRoute(loritta, website),
		PatchServerConfigRoute(loritta, website),
		PostObsoleteServerConfigRoute(loritta),
		PostSearchGuildsRoute(loritta),
		PostSendMessageGuildRoute(loritta),
		GetServerConfigSectionRoute(loritta, website),
		GetGuildEmojiFightTopWinnersRoute(loritta),
		GetGuildUserEmojiFightVictoriesRoute(loritta),

		// Loritta
		GetLanguageInfoRoute(loritta),
		GetCommandsRoute(loritta),
		GetApplicationCommandsRoute(loritta),
		GetFanArtsController(loritta),
		GetLocaleRoute(loritta),
		GetLorittaActionRoute(loritta),
		GetRaffleStatusRoute(loritta),
		GetStatusRoute(loritta),
		GetAvailableBackgroundsRoute(loritta),
		GetAvailableProfileDesignsRoute(loritta),
		GetSelfUserProfileRoute(loritta),
		PostLorittaRpcRoute(website),
		PostLorittaActionRoute(loritta),
		PostRaffleStatusRoute(loritta),
		PostReputationMessageRoute(loritta),
		PostTransferBalanceRoute(loritta),
		PostUpdateUserBackgroundRoute(loritta),
		PostErrorRoute(loritta),

		// Twitch
		GetTwitchInfoRoute(loritta),

		// User
		GetMutualGuildsRoute(loritta),
		GetSelfInfoRoute(loritta),
		GetUserReputationsRoute(loritta),
		PatchProfileRoute(loritta),
		PostDonationPaymentRoute(loritta),
		PostSearchUsersRoute(loritta),
		PostUserReputationsRoute(loritta),
		PostLogoutRoute(loritta),
		PostDeleteDataRoute(loritta),
		GetBackgroundRoute(loritta),

		// Twitch
		GetTwitchInfoRoute(loritta),

		// YouTube
		GetChannelInfoRoute(loritta),

		// ===[ MISC ]===
		AdsTxtRoute(loritta)
	)
}