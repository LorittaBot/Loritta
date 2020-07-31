package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.tables.*
import com.mrpowergamerbr.loritta.utils.Constants
import io.ktor.application.ApplicationCall
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.*
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or

class PostDeleteDataRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/@me/delete") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val userId = userIdentification.id.toLong()

		logger.info { "User $userId requested to delete the account data!" }

		loritta.newSuspendedTransaction {
			logger.info { "Deleting $userId's 2FA checks..." }
			Requires2FAChecksUsers.deleteWhere {
				Requires2FAChecksUsers.userId eq userId
			}

			logger.info { "Deleting $userId's dailies..." }
			Dailies.deleteWhere {
				Dailies.receivedById eq userId
			}

			logger.info { "Deleting $userId's donation keys..." }
			DonationKeys.deleteWhere {
				DonationKeys.userId eq userId
			}

			logger.info { "Deleting $userId's guild profiles..." }
			GuildProfiles.deleteWhere {
				GuildProfiles.userId eq userId
			}

			logger.info { "Deleting $userId's mutes..." }
			Mutes.deleteWhere {
				Mutes.userId eq userId
			}

			logger.info { "Deleting $userId's reminders..." }
			Reminders.deleteWhere {
				Reminders.userId eq userId
			}

			logger.info { "Deleting $userId's reputations..." }
			Reputations.deleteWhere {
				Reputations.givenById eq userId or (Reputations.receivedById eq userId)
			}

			logger.info { "Deleting $userId's ship effects..." }
			ShipEffects.deleteWhere {
				ShipEffects.user1Id eq userId or (ShipEffects.user2Id eq userId)
			}

			logger.info { "Deleting $userId's stored messages..." }
			StoredMessages.deleteWhere {
				StoredMessages.authorId eq userId
			}

			logger.info { "Deleting $userId's background payments keys..." }
			BackgroundPayments.deleteWhere {
				BackgroundPayments.userId eq userId
			}

			logger.info { "Deleting $userId from bd&c winners..." }
			BomDiaECiaWinners.deleteWhere {
				BomDiaECiaWinners.userId eq userId
			}

			logger.info { "Deleting $userId's bot votes..." }
			BotVotes.deleteWhere {
				BotVotes.userId eq userId
			}

			logger.info { "Deleting $userId's cached data..." }
			CachedDiscordUsers.deleteWhere {
				CachedDiscordUsers.id eq userId
			}

			logger.info { "Deleting $userId from the executed command logs..." }
			ExecutedCommandsLog.deleteWhere {
				ExecutedCommandsLog.userId eq userId
			}

			logger.info { "Deleting $userId's payments..." }
			Payments.deleteWhere {
				Payments.userId eq userId
			}

			logger.info { "Deleting $userId's sonhos transactions..." }
			SonhosTransaction.deleteWhere {
				SonhosTransaction.receivedBy eq userId or (SonhosTransaction.givenBy eq userId)
			}

			logger.info { "Deleting $userId's profile..." }
			Profiles.deleteWhere {
				Profiles.id eq userId
			}

			logger.info { "Deleting $userId's marriages..." }
			Marriages.deleteWhere {
				Marriages.user1 eq userId or (Marriages.user2 eq userId)
			}

			logger.info { "Banning $userId for three days..." }
			BannedUsers.insert {
				it[BannedUsers.userId] = userId
				it[bannedAt] = System.currentTimeMillis()
				it[bannedBy] = null
				it[valid] = true
				it[expiresAt] = System.currentTimeMillis() + (Constants.ONE_DAY_IN_MILLISECONDS * 3)
				it[BannedUsers.reason] = loritta.getLocaleById("default")["website.dashboard.profile.deleteAccount.bannedAccountDueToDeletion"]
			}
		}

		call.sessions.clear<LorittaJsonWebSession>()
		call.respondJson(jsonObject())
	}
}