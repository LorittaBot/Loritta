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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.*
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.*
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
		val payload = JsonParser.parseString(call.receiveText()).obj

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

					profile.takeSonhosNested(cost.toLong())
					PaymentUtils.addToTransactionLogNested(
							cost.toLong(),
							SonhosPaymentReason.BACKGROUND,
							givenBy = profile.id.value
					)

					BackgroundPayments.insert {
						it[BackgroundPayments.userId] = profile.userId
						it[BackgroundPayments.background] = background[Backgrounds.id]
						it[BackgroundPayments.boughtAt] = System.currentTimeMillis()
						it[BackgroundPayments.cost] = cost.toLong()
					}

					val createdBy = background[Backgrounds.createdBy]
					val creatorReceived = (cost.toDouble() * 0.1).toLong()
					for (creatorId in createdBy) {
						val author = loritta.fanArtArtists.firstOrNull { it.id == creatorId } ?: continue

						val discordId = author.socialNetworks?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()?.id
								?: continue

						val creator = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(discordId)

						creator.addSonhosNested(creatorReceived)
						PaymentUtils.addToTransactionLogNested(
								creatorReceived,
								SonhosPaymentReason.BACKGROUND,
								receivedBy = creatorReceived
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

					profile.takeSonhosNested(cost.toLong())
					PaymentUtils.addToTransactionLogNested(
							cost.toLong(),
							SonhosPaymentReason.BACKGROUND,
							givenBy = profile.id.value
					)

					ProfileDesignsPayments.insert {
						it[ProfileDesignsPayments.userId] = profile.userId
						it[ProfileDesignsPayments.profile] = background[ProfileDesigns.id]
						it[ProfileDesignsPayments.boughtAt] = System.currentTimeMillis()
						it[ProfileDesignsPayments.cost] = cost.toLong()
					}

					val createdBy = background[ProfileDesigns.createdBy]
					val creatorReceived = (cost.toDouble() * 0.1).toLong()
					for (creatorId in createdBy) {
						val author = loritta.fanArtArtists.firstOrNull { it.id == creatorId } ?: continue

						val discordId = author.socialNetworks?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()?.id
								?: continue

						val creator = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(discordId)

						creator.addSonhosNested(creatorReceived)
						PaymentUtils.addToTransactionLogNested(
								creatorReceived,
								SonhosPaymentReason.PROFILE,
								receivedBy = creatorReceived
						)
					}
				}
			}

			call.respondJson(jsonObject())
		}
	}
}