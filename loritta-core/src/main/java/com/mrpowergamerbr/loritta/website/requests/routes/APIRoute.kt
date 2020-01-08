package com.mrpowergamerbr.loritta.website.requests.routes

import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.callbacks.*
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.channels.GetMessageGuildController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.economy.LoriTransferBalanceController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.economy.TransferBalanceExternalController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild.GuildWebAuditLogController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild.SendMessageGuildController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild.StoreItemsGuildController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild.UpdateServerConfigController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta.*
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.parallax.AddRoleToMemberController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.parallax.RemoveRoleFromMemberController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.parallax.SendMessageController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.rss.DefaultRssFeedsController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.rss.ReceivedFeedEntryController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.twitter.ReceivedTweetController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.twitter.ShowTwitterUserController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.twitter.UpdateStreamController
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user.*
import org.jooby.Jooby

class APIRoute : Jooby() {
	init {
		// ===[ GUILDS ]===
		use(UpdateServerConfigController::class.java)
		use(SendMessageGuildController::class.java)
		use(StoreItemsGuildController::class.java)
		use(GetMessageGuildController::class.java)
		use(GuildWebAuditLogController::class.java)

		// ===[ CALLBACKS ]===
		use(MixerCallbackController::class.java)
		use(DiscordBotsCallbackController::class.java)
		use(PubSubHubbubCallbackController::class.java)
		use(LoriTransferBalanceController::class.java)
		use(UpdateAvailableCallbackController::class.java)
		use(MercadoPagoCallbackController::class.java)

		// ===[ USERS ]===
		use(UserReputationController::class.java)
		use(SelfProfileController::class.java)
		use(CreateDonationPaymentController::class.java)
		use(ProfileImageController::class.java)

		// ===[ LORITTA ]===
		use(UsersController::class.java)
		use(GetStatusController::class.java)
		use(GetMutualGuildsController::class.java)
		use(SendHelpController::class.java)
		use(RegisterUsernameChangeController::class.java)
		use(GetRaffleStatusController::class.java)
		use(UpdateReadyController::class.java)
		use(GetGuildInfoController::class.java)
		use(LorittaActionController::class.java)
		use(GetMembersWithPermissionsInGuildController::class.java)
		use(GetCurrentFanMadeAvatarController::class.java)
		use(TransferBalanceController::class.java)
		use(SendReputationMessageController::class.java)
		use(SearchUsersController::class.java)
		use(SearchGuildsController::class.java)
		use(GetMembersWithRolesInGuildController::class.java)
		use(GlobalBansController::class.java)
		use(UpdateUserBackgroundController::class.java)

		// ===[ NEW WEBSITE STUFF ]===
		use(GetLocaleController::class.java)
		use(GetSelfInfoController::class.java)
		use(GetFanArtsController::class.java)

		// ===[ TWITTER ]===
		use(ReceivedTweetController::class.java)
		use(UpdateStreamController::class.java)
		use(ShowTwitterUserController::class.java)

		// ===[ MONEY ]===
		use(TransferBalanceExternalController::class.java)

		// ===[ PARALLAX ]===
		use(SendMessageController::class.java)
		use(AddRoleToMemberController::class.java)
		use(RemoveRoleFromMemberController::class.java)

		// ===[ RSS FEEDS ]===
		use(DefaultRssFeedsController::class.java)
		use(ReceivedFeedEntryController::class.java)
	}
}