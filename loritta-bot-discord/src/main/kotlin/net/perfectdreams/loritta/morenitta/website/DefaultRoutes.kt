package net.perfectdreams.loritta.morenitta.website

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy.PostTransferBalanceExternalRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.languages.GetLanguageInfoRoute
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta.*
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.user.*
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.*
import net.perfectdreams.loritta.morenitta.website.routes.sponsors.SponsorsRedirectRoute
import net.perfectdreams.loritta.morenitta.website.routes.user.UserReputationRoute

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

		// Reps
		UserReputationRoute(loritta),

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

		// Guild
		GetGuildInfoRoute(loritta),
		GetMembersWithPermissionsInGuildRoute(loritta),
		GetMembersWithRolesInGuildRoute(loritta),
		PostSearchGuildsRoute(loritta),

		// Loritta
		GetLanguageInfoRoute(loritta),
		GetCommandsRoute(loritta),
		GetApplicationCommandsRoute(loritta),
		GetLocaleRoute(loritta),
		GetRaffleStatusRoute(loritta),
		GetStatusRoute(loritta),
		PostLorittaRpcRoute(website),
		PostLorittaActionRoute(loritta),
		PostRaffleStatusRoute(loritta),
		PostReputationMessageRoute(loritta),

		// User
		GetMutualGuildsRoute(loritta),
		GetSelfInfoRoute(loritta),
		GetUserReputationsRoute(loritta),
		PostDonationPaymentRoute(loritta),
		PostSearchUsersRoute(loritta),
		PostUserReputationsRoute(loritta),
		GetBackgroundRoute(loritta),

		// ===[ MISC ]===
		AdsTxtRoute(loritta)
	)
}