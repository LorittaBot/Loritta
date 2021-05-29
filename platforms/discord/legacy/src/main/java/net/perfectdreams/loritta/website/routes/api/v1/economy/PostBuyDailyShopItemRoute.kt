package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BackgroundPayments
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.tables.DailyProfileShopItems
import net.perfectdreams.loritta.tables.DailyShopItems
import net.perfectdreams.loritta.tables.DailyShops
import net.perfectdreams.loritta.tables.ProfileDesigns
import net.perfectdreams.loritta.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.util.concurrent.TimeUnit

class PostBuyDailyShopItemRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/daily-shop/buy/{type}/{internalName}") {
	companion object {
		private val mutexes = Caffeine.newBuilder()
				.expireAfterAccess(60, TimeUnit.SECONDS)
				.build<Long, Mutex>()
				.asMap()
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val profile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id)
		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }

		val type = call.parameters["type"]!!
		val internalName = call.parameters["internalName"]!!

		// Para evitar que alguém compre o mesmo perfil várias vezes, vamos colocar em um mutex, para evitar que um spam
		// de requests faça a pessoa comprar o mesmo perfil várias vezes
		val mutex = mutexes.getOrPut(profile.userId) { Mutex() }
		mutex.withLock {
			loritta.newSuspendedTransaction {
				if (type == "background") {
					val backgrounds = run {
						val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

						(DailyShopItems innerJoin Backgrounds)
								.select {
									DailyShopItems.shop eq shop[DailyShops.id]
								}
					}

					val background = backgrounds.firstOrNull { it[Backgrounds.id].value == internalName }
							?: throw WebsiteAPIException(
									HttpStatusCode.BadRequest,
									WebsiteUtils.createErrorPayload(
											LoriWebCode.ITEM_NOT_FOUND,
											"Item is not on the current daily shop"
									)
							)

					val cost = background[Backgrounds.rarity].getBackgroundPrice()
					if (cost > profile.money)
						throw WebsiteAPIException(HttpStatusCode.PaymentRequired,
								WebsiteUtils.createErrorPayload(
										LoriWebCode.INSUFFICIENT_FUNDS
								)
						)

					val alreadyBoughtTheBackground = BackgroundPayments.select {
						BackgroundPayments.userId eq profile.userId and (BackgroundPayments.background eq background[Backgrounds.id])
					}.count() != 0L

					if (alreadyBoughtTheBackground)
						throw WebsiteAPIException(
								HttpStatusCode.Conflict,
								WebsiteUtils.createErrorPayload(
										LoriWebCode.ALREADY_BOUGHT_THE_ITEM
								)
						)

					profile.takeSonhosAndAddToTransactionLogNested(
						cost.toLong(),
						SonhosPaymentReason.BACKGROUND
					)

					BackgroundPayments.insert {
						it[userId] = profile.userId
						it[BackgroundPayments.background] = background[Backgrounds.id]
						it[boughtAt] = System.currentTimeMillis()
						it[BackgroundPayments.cost] = cost.toLong()
					}

					val createdBy = background[Backgrounds.createdBy]
					val creatorReceived = (cost.toDouble() * 0.1).toLong()
					for (creatorId in createdBy) {
						val author = loritta.fanArtArtists.firstOrNull { it.id == creatorId } ?: continue

						val discordId = author.socialNetworks?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()?.id
								?: continue

						val creator = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(discordId)

						creator.addSonhosAndAddToTransactionLogNested(
							creatorReceived,
							SonhosPaymentReason.BACKGROUND
						)
					}
				} else if (type == "profile-design") {
					val backgrounds = run {
						val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

						(DailyProfileShopItems innerJoin ProfileDesigns)
								.select {
									DailyProfileShopItems.shop eq shop[DailyShops.id]
								}
					}

					val background = backgrounds.firstOrNull { it[ProfileDesigns.id].value == internalName }
							?: throw WebsiteAPIException(
									HttpStatusCode.BadRequest,
									WebsiteUtils.createErrorPayload(
											LoriWebCode.ITEM_NOT_FOUND,
											"Item is not on the current daily shop"
									)
							)

					val cost = background[ProfileDesigns.rarity].getProfilePrice()
					if (cost > profile.money)
						throw WebsiteAPIException(HttpStatusCode.PaymentRequired,
								WebsiteUtils.createErrorPayload(
										LoriWebCode.INSUFFICIENT_FUNDS
								)
						)

					val alreadyBoughtTheBackground = ProfileDesignsPayments.select {
						ProfileDesignsPayments.userId eq profile.userId and (ProfileDesignsPayments.profile eq background[ProfileDesigns.id])
					}.count() != 0L

					if (alreadyBoughtTheBackground)
						throw WebsiteAPIException(
								HttpStatusCode.Conflict,
								WebsiteUtils.createErrorPayload(
										LoriWebCode.ALREADY_BOUGHT_THE_ITEM
								)
						)

					profile.takeSonhosAndAddToTransactionLogNested(
							cost.toLong(),
							SonhosPaymentReason.PROFILE
					)

					ProfileDesignsPayments.insert {
						it[userId] = profile.userId
						it[ProfileDesignsPayments.profile] = background[ProfileDesigns.id]
						it[boughtAt] = System.currentTimeMillis()
						it[ProfileDesignsPayments.cost] = cost.toLong()
					}

					val createdBy = background[ProfileDesigns.createdBy]
					val creatorReceived = (cost.toDouble() * 0.1).toLong()
					for (creatorId in createdBy) {
						val author = loritta.fanArtArtists.firstOrNull { it.id == creatorId } ?: continue

						val discordId = author.socialNetworks?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()?.id
								?: continue

						val creator = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(discordId)

						creator.addSonhosAndAddToTransactionLogNested(
							creatorReceived,
							SonhosPaymentReason.PROFILE
						)
					}
				}
			}

			call.respondJson(jsonObject())
		}
	}
}