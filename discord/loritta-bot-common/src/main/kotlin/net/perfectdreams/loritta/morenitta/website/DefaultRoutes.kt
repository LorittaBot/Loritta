package net.perfectdreams.loritta.morenitta.website

import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.AdsTxtRoute
import net.perfectdreams.loritta.morenitta.website.routes.CommunityGuidelinesRoute
import net.perfectdreams.loritta.morenitta.website.routes.DailyRoute
import net.perfectdreams.loritta.morenitta.website.routes.DonateRoute
import net.perfectdreams.loritta.morenitta.website.routes.FanArtsArtistRoute
import net.perfectdreams.loritta.morenitta.website.routes.FanArtsRoute
import net.perfectdreams.loritta.morenitta.website.routes.SponsorsRoute
import net.perfectdreams.loritta.morenitta.website.routes.TermsOfServiceRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.GetBundlesRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.GetDailyShopRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostBundlesRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostBuyDailyShopItemRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostTransferBalanceExternalRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetGuildInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetGuildWebAuditLogRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetMembersWithPermissionsInGuildRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetMembersWithRolesInGuildRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetServerConfigRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.GetServerConfigSectionRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.PatchServerConfigRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.PostObsoleteServerConfigRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.PostSearchGuildsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.PostSendMessageGuildRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.languages.GetLanguageInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.twitch.GetTwitchInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.twitter.GetShowTwitterUserRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.GetMutualGuildsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.GetSelfInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.GetSelfUserProfileRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.GetUserReputationsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PatchProfileRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostDeleteDataRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostDonationPaymentRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostLogoutRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostSearchUsersRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.PostUserReputationsRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.youtube.GetChannelInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.DashboardRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.*
import net.perfectdreams.loritta.morenitta.website.routes.sponsors.SponsorsRedirectRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.UserReputationRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.AllBackgroundsListRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.AvailableBundlesRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.BackgroundsListRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.DailyShopRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ProfileListRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ShipEffectsRoute

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

		// Dashboard
		DashboardRoute(loritta),
		ConfigureGeneralRoute(loritta),
		AuditLogRoute(loritta),
		ConfigureAutoroleRoute(loritta),
		ConfigureCommandsRoute(loritta),
		ConfigureCustomBadgeRoute(loritta),
		ConfigureDailyMultiplierRoute(loritta),
		ConfigureEconomyRoute(loritta),
		ConfigureEventLogRoute(loritta),
		ConfigureInviteBlockerRoute(loritta),
		ConfigureLevelUpRoute(loritta),
		ConfigureTwitchRoute(loritta),
		ConfigureMemberCounterRoute(loritta),
		ConfigureMiscellaneousRoute(loritta),
		ConfigureModerationRoute(loritta),
		ConfigurePermissionsRoute(loritta),
		ConfigurePremiumKeyRoute(loritta),
		ConfigureStarboardRoute(loritta),
		ConfigureTrackedTwitterAccountsRoute(loritta),
		ConfigureWelcomerRoute(loritta),
		ConfigureYouTubeRoute(loritta),
		ConfigureNashornCommandsRoute(loritta),
		ConfigureCustomCommandsRoute(loritta),
		ConfigureGamerSaferVerifyRoute(loritta),

		// Reps
		UserReputationRoute(loritta),

		// Profiles
		ProfileListRoute(loritta),
		ShipEffectsRoute(loritta),
		AvailableBundlesRoute(loritta),
		BackgroundsListRoute(loritta),
		AllBackgroundsListRoute(loritta),
		DailyShopRoute(loritta),

		// ===[ API ROUTES ]===
		// Callbacks
		GetPubSubHubbubCallbackRoute(loritta),
		PostDiscordBotsCallbackRoute(loritta),
		PostPubSubHubbubCallbackRoute(loritta),
		PostPerfectPaymentsCallbackRoute(loritta),
		CreateWebhookRoute(loritta),
		PostGamerSaferCallbackRoute(loritta),

		// Economy
		PostTransferBalanceExternalRoute(loritta),
		GetBundlesRoute(loritta),
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

		// Loritta
		GetLanguageInfoRoute(loritta),
		GetCommandsRoute(loritta),
		GetApplicationCommandsRoute(loritta),
		GetFanArtsController(loritta),
		GetLocaleRoute(loritta),
		GetLorittaActionRoute(loritta),
		GetRaffleStatusRoute(loritta),
		GetStatusRoute(loritta),
		GetPrometheusMetricsRoute(loritta),
		GetAvailableBackgroundsRoute(loritta),
		GetAvailableProfileDesignsRoute(loritta),
		GetSelfUserProfileRoute(loritta),
		PostLorittaRpcRoute(website),
		PostLorittaActionRoute(loritta),
		PostRaffleStatusRoute(loritta),
		PostReputationMessageRoute(loritta),
		PostTransferBalanceRoute(loritta),
		PostUpdateReadyRoute(loritta),
		PostUpdateUserBackgroundRoute(loritta),
		PostErrorRoute(loritta),

		// Twitch
		GetTwitchInfoRoute(loritta),

		// Twitter
		GetShowTwitterUserRoute(loritta),

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

		// Twitch
		GetTwitchInfoRoute(loritta),

		// YouTube
		GetChannelInfoRoute(loritta),

		// ===[ MISC ]===
		AdsTxtRoute(loritta)
	)
}