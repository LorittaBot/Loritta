package net.perfectdreams.loritta.website

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.*
import net.perfectdreams.loritta.website.routes.api.v1.callbacks.*
import net.perfectdreams.loritta.website.routes.api.v1.economy.GetLoriDailyRewardRoute
import net.perfectdreams.loritta.website.routes.api.v1.economy.GetLoriDailyRewardStatusRoute
import net.perfectdreams.loritta.website.routes.api.v1.economy.PostTransferBalanceExternalRoute
import net.perfectdreams.loritta.website.routes.api.v1.guild.*
import net.perfectdreams.loritta.website.routes.api.v1.loritta.*
import net.perfectdreams.loritta.website.routes.api.v1.twitch.GetTwitchInfoRoute
import net.perfectdreams.loritta.website.routes.api.v1.twitter.GetShowTwitterUserRoute
import net.perfectdreams.loritta.website.routes.api.v1.twitter.GetUpdateStreamController
import net.perfectdreams.loritta.website.routes.api.v1.twitter.PostReceivedTweetRoute
import net.perfectdreams.loritta.website.routes.api.v1.user.*
import net.perfectdreams.loritta.website.routes.api.v1.youtube.GetChannelInfoRoute
import net.perfectdreams.loritta.website.routes.dashboard.DashboardAuthRoute
import net.perfectdreams.loritta.website.routes.dashboard.DashboardRoute
import net.perfectdreams.loritta.website.routes.dashboard.PostDashboardRoute
import net.perfectdreams.loritta.website.routes.dashboard.configure.*
import net.perfectdreams.loritta.website.routes.extras.ExtrasViewerRoute
import net.perfectdreams.loritta.website.routes.landingpages.BrazilianBotLandingPageRoute
import net.perfectdreams.loritta.website.routes.sponsors.SponsorsRedirectRoute
import net.perfectdreams.loritta.website.routes.user.UserDashboardRoute
import net.perfectdreams.loritta.website.routes.user.UserReputationRoute
import net.perfectdreams.loritta.website.routes.user.dashboard.ProfileListRoute
import net.perfectdreams.loritta.website.routes.user.dashboard.ShipEffectsRoute

object DefaultRoutes {
	fun defaultRoutes(loritta: LorittaDiscord) = mutableListOf(
			// ===[ USER ROUTES ]===
			HomeRoute(loritta),
			BlogRoute(loritta),
			BlogPostRoute(loritta),
			CommandsRoute(loritta),
			CommunityGuidelinesRoute(loritta),
			FanArtArtistRoute(loritta),
			FanArtsRoute(loritta),
			SponsorsRoute(loritta),
			TranslateRoute(loritta),
			DashboardAuthRoute(loritta),
			ExtrasRoute(loritta),
			ExtrasViewerRoute(loritta),
			SponsorsRedirectRoute(loritta),
			SupportRoute(loritta),
			TermsOfServiceRoute(loritta),
			DailyRoute(loritta),
			NashornDocsRoute(loritta),
			DonateRoute(loritta),
			LogoutRoute(loritta),

			// Landing Pages
			BrazilianBotLandingPageRoute(loritta),

			// Dashboard
			DashboardRoute(loritta),
			PostDashboardRoute(loritta),
			ConfigureGeneralRoute(loritta),
			AuditLogRoute(loritta),
			ConfigureAutoroleRoute(loritta),
			ConfigureCommandsRoute(loritta),
			ConfigureCustomBadgeRoute(loritta),
			ConfigureDailyMultiplierRoute(loritta),
			ConfigureEconomyRoute(loritta),
			ConfigureEventHandlersRoute(loritta),
			ConfigureEventLogRoute(loritta),
			ConfigureInviteBlockerRoute(loritta),
			ConfigureLevelUpRoute(loritta),
			ConfigureTwitchRoute(loritta),
			ConfigureMemberCounterRoute(loritta),
			ConfigureMiscellaneousRoute(loritta),
			ConfigureModerationRoute(loritta),
			ConfigureNashornCommandsRoute(loritta),
			ConfigurePermissionsRoute(loritta),
			ConfigurePremiumKeyRoute(loritta),
			ConfigureStarboardRoute(loritta),
			ConfigureTextChannelsRoute(loritta),
			ConfigureTrackedTwitterAccountsRoute(loritta),
			ConfigureWelcomerRoute(loritta),
			ConfigureYouTubeRoute(loritta),

			// Reps
			UserReputationRoute(loritta),

			// Profiles
			UserDashboardRoute(loritta),
			ProfileListRoute(loritta),
			ShipEffectsRoute(loritta),

			// ===[ API ROUTES ]===
			// Callbacks
			GetPubSubHubbubCallbackRoute(loritta),
			PostDiscordBotsCallbackRoute(loritta),
			PostMercadoPagoCallbackRoute(loritta),
			PostPubSubHubbubCallbackRoute(loritta),
			CreateWebhookRoute(loritta),

			// Economy
			GetLoriDailyRewardRoute(loritta),
			GetLoriDailyRewardStatusRoute(loritta),
			PostTransferBalanceExternalRoute(loritta),

			// Guild
			GetGuildInfoRoute(loritta),
			GetGuildWebAuditLogRoute(loritta),
			GetMembersWithPermissionsInGuildRoute(loritta),
			GetMembersWithRolesInGuildRoute(loritta),
			GetServerConfigRoute(loritta),
			PatchServerConfigRoute(loritta),
			PostObsoleteServerConfigRoute(loritta),
			PostSearchGuildsRoute(loritta),
			PostSendMessageGuildRoute(loritta),
			PatchUpdateServerConfigBadgeRoute(loritta),

			// Loritta
			GetCommandsRoute(loritta),
			GetCurrentFanMadeAvatarRoute(loritta),
			GetFanArtsController(loritta),
			GetGlobalBansRoute(loritta),
			GetLocaleRoute(loritta),
			GetLorittaActionRoute(loritta),
			GetRaffleStatusRoute(loritta),
			GetStatusRoute(loritta),
			PostLorittaActionRoute(loritta),
			PostRaffleStatusRoute(loritta),
			PostReputationMessageRoute(loritta),
			PostTransferBalanceRoute(loritta),
			PostUpdateReadyRoute(loritta),
			PostUpdateUserBackgroundRoute(loritta),
			PostUsernameChangeRoute(loritta),

			// Twitch
			GetTwitchInfoRoute(loritta),

			// Twitter
			GetShowTwitterUserRoute(loritta),
			GetUpdateStreamController(loritta),
			PostReceivedTweetRoute(loritta),

			// User
			GetMutualGuildsRoute(loritta),
			GetSelfInfoRoute(loritta),
			GetUserReputationsRoute(loritta),
			PatchProfileRoute(loritta),
			PostDonationPaymentRoute(loritta),
			PostSearchUsersRoute(loritta),
			PostUserReputationsRoute(loritta),

			// Daily
			GetLoriDailyRewardRoute(loritta),
			GetLoriDailyRewardStatusRoute(loritta),

			// Guild
			GetServerConfigRoute(loritta),
			GetServerConfigSectionRoute(loritta),
			PatchServerConfigRoute(loritta),
			GetGuildWebAuditLogRoute(loritta),
			PostSendMessageGuildRoute(loritta),

			// Twitch
			GetTwitchInfoRoute(loritta),

			// YouTube
			GetChannelInfoRoute(loritta)
	)
}