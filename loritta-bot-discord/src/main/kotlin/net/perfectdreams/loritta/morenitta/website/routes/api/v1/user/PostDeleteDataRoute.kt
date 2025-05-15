package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reminders
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.tables.StoredMessages
import net.perfectdreams.loritta.morenitta.utils.Constants
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedDiscordUsers
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedCommandsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransaction
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class PostDeleteDataRoute(loritta: LorittaBot) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/@me/delete") {
	companion object {
		private val logger = KotlinLogging.logger {}

		suspend fun deleteAccountData(loritta: LorittaBot, userId: Long) {
			loritta.newSuspendedTransaction {
				// According to Discord, IDs aren't user identifiable information, so we aren't going to delete the following stuff:
				// Mutes (Useful for moderation, doesn't store anything related to the user, also IDs aren't personally identifiable)
				// Payments (Required for banking stuff, we can't delete those)
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

				logger.info { "Deleting $userId's background payments..." }
				BackgroundPayments.deleteWhere {
					BackgroundPayments.userId eq userId
				}

				logger.info { "Deleting $userId's profile designs payments..." }
				ProfileDesignsPayments.deleteWhere {
					ProfileDesignsPayments.userId eq userId
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

				logger.info { "Deleting $userId's sonhos transactions..." }
				SonhosTransaction.deleteWhere {
					SonhosTransaction.receivedBy eq userId or (SonhosTransaction.givenBy eq userId)
				}

				// First we will select the marriage, check if there is a marriage and THEN update all profiles to have a null reference to it, and then delete it!
				/* val marriage = Marriages.selectAll().where { Marriages.user1 eq userId or (Marriages.user2 eq userId) }
					.firstOrNull()
				if (marriage != null) {
					logger.info { "Deleting $userId's marriage..." }
					Profiles.update({ Profiles.marriage eq marriage[Marriages.id] }) {
						it[Profiles.marriage] = null
					}

					Marriages.deleteWhere { Marriages.id eq marriage[Marriages.id] }
				} else {
					logger.info { "Not deleting $userId's marriage because they aren't married! :P" }
				} */

				logger.info { "Deleting $userId's profile..." }
				Profiles.deleteWhere {
					Profiles.id eq userId
				}
			}
		}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val userId = userIdentification.id.toLong()

		logger.info { "User $userId requested to delete the account data!" }

		deleteAccountData(loritta, userId)

		loritta.newSuspendedTransaction {
			logger.info { "Banning $userId for 90 days..." }
			BannedUsers.insert {
				it[BannedUsers.userId] = userId
				it[bannedAt] = System.currentTimeMillis()
				it[bannedBy] = null
				it[valid] = true
				it[expiresAt] = System.currentTimeMillis() + (Constants.ONE_DAY_IN_MILLISECONDS * 90)
				it[BannedUsers.reason] = loritta.localeManager.getLocaleById("default")["website.dashboard.profile.deleteAccount.bannedAccountDueToDeletion"]
			}
		}

		call.sessions.clear<LorittaJsonWebSession>()
		call.respondJson(jsonObject())
	}
}