package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredLorittaItemShopBoughtBackgroundTransaction
import net.perfectdreams.loritta.serializable.StoredLorittaItemShopBoughtProfileDesignTransaction
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.util.concurrent.TimeUnit

class PostBuyDailyShopItemRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/daily-shop/buy") {
	companion object {
		private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()
	}

	override suspend fun onAuthenticatedRequest(
		call: ApplicationCall,
		locale: BaseLocale,
		i18nContext: I18nContext,
		discordAuth: TemmieDiscordAuth,
		userIdentification: LorittaJsonWebSession.UserIdentification
	) {
		// A bit hacky but hey, there's nothing a lot we can do rn
		val galleryOfDreamsResponse = loritta.cachedGalleryOfDreamsDataResponse!!

		val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)

		val params = call.receiveParameters()
		val type = params.getOrFail("type")
		val internalName = params.getOrFail("internalName")

		// TODO: Is this correct? If we are doing this in a transaction then this shouldn't happen!
		// Para evitar que alguém compre o mesmo perfil várias vezes, vamos colocar em um mutex, para evitar que um spam
		// de requests faça a pessoa comprar o mesmo perfil várias vezes
		val mutex = mutexes.getOrPut(profile.userId) { Mutex() }
		val result = mutex.withLock {
			loritta.newSuspendedTransaction {
				if (type == "background") {
					val backgrounds = run {
						val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

						(DailyShopItems innerJoin Backgrounds)
							.selectAll()
							.where {
								DailyShopItems.shop eq shop[DailyShops.id]
							}
					}

					val background = backgrounds.firstOrNull { it[Backgrounds.id].value == internalName }
					if (background == null)
						return@newSuspendedTransaction Result.ItemNotInItemShop

					val cost = background[Backgrounds.rarity].getBackgroundPrice()
					if (cost > profile.money)
						return@newSuspendedTransaction Result.NotEnoughSonhos

					val alreadyBoughtTheBackground = BackgroundPayments.select {
						BackgroundPayments.userId eq profile.userId and (BackgroundPayments.background eq background[Backgrounds.id])
					}.count() != 0L

					if (alreadyBoughtTheBackground)
						return@newSuspendedTransaction Result.YouAlreadyHaveThisItem

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

					// Cinnamon transaction system
					SimpleSonhosTransactionsLogUtils.insert(
						profile.userId,
						Instant.now(),
						TransactionType.LORITTA_ITEM_SHOP,
						cost.toLong(),
						StoredLorittaItemShopBoughtBackgroundTransaction(background[Backgrounds.id].value)
					)

					val createdBy = background[Backgrounds.createdBy]
					val creatorReceived = (cost.toDouble() * 0.1).toLong()
					for (creatorId in createdBy) {
						val author = galleryOfDreamsResponse.artists.firstOrNull { it.slug == creatorId } ?: continue

						val discordId = author.socialConnections.filterIsInstance<DiscordSocialConnection>().firstOrNull()?.id ?: continue

						val creator = loritta.getOrCreateLorittaProfile(discordId)

						creator.addSonhosAndAddToTransactionLogNested(
							creatorReceived,
							SonhosPaymentReason.BACKGROUND
						)
					}

					return@newSuspendedTransaction Result.Success
				} else if (type == "profile-design") {
					val backgrounds = run {
						val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

						(DailyProfileShopItems innerJoin ProfileDesigns)
							.selectAll()
							.where {
								DailyProfileShopItems.shop eq shop[DailyShops.id]
							}
					}

					val background = backgrounds.firstOrNull { it[ProfileDesigns.id].value == internalName }
					if (background == null)
						return@newSuspendedTransaction Result.ItemNotInItemShop

					val cost = background[ProfileDesigns.rarity].getProfilePrice()
					if (cost > profile.money)
						return@newSuspendedTransaction Result.NotEnoughSonhos

					val alreadyBoughtTheBackground = ProfileDesignsPayments.select {
						ProfileDesignsPayments.userId eq profile.userId and (ProfileDesignsPayments.profile eq background[ProfileDesigns.id])
					}.count() != 0L

					if (alreadyBoughtTheBackground)
						return@newSuspendedTransaction Result.YouAlreadyHaveThisItem

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

					// Cinnamon transaction system
					SimpleSonhosTransactionsLogUtils.insert(
						profile.userId,
						Instant.now(),
						TransactionType.LORITTA_ITEM_SHOP,
						cost.toLong(),
						StoredLorittaItemShopBoughtProfileDesignTransaction(background[ProfileDesigns.id].value)
					)

					val createdBy = background[ProfileDesigns.createdBy]
					val creatorReceived = (cost.toDouble() * 0.1).toLong()
					for (creatorId in createdBy) {
						val author = galleryOfDreamsResponse.artists.firstOrNull { it.slug == creatorId } ?: continue

						val discordId = author.socialConnections.filterIsInstance<DiscordSocialConnection>().firstOrNull()?.id ?: continue

						val creator = loritta.getOrCreateLorittaProfile(discordId)

						creator.addSonhosAndAddToTransactionLogNested(
							creatorReceived,
							SonhosPaymentReason.PROFILE
						)
					}

					return@newSuspendedTransaction Result.Success
				} else error("Unsupported item shop type $type")
			}
		}

		when (result) {
			Result.ItemNotInItemShop -> {
				call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")
				call.respondJson(
					buildJsonObject {
						put("closeSpicyModal", null)
						put("refreshItemShop", null)
						put("playSoundEffect", "config-error")
						put(
							"showSpicyToast",
							EmbeddedSpicyModalUtils.encodeURIComponent(
								Json.encodeToString(
									EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, "Item Fora de Rotação", "Vixe, parece que o item saiu da rotação diária da loja bem na hora que você foi comprar!")
								)
							)
						)
					}.toString(),
					status = HttpStatusCode.BadRequest
				)
			}
			Result.NotEnoughSonhos -> {
				call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")
				call.respondJson(
					buildJsonObject {
						put("closeSpicyModal", null)
						put("refreshItemShop", null)
						put("playSoundEffect", "config-error")
						put(
							"showSpicyToast",
							EmbeddedSpicyModalUtils.encodeURIComponent(
								Json.encodeToString(
									EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, "Você não tem sonhos suficientes para comprar este item!", null)
								)
							)
						)
					}.toString(),
					status = HttpStatusCode.PaymentRequired
				)
			}
			Result.YouAlreadyHaveThisItem -> {
				call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")
				call.respondJson(
					buildJsonObject {
						put("closeSpicyModal", null)
						put("refreshItemShop", null)
						put("playSoundEffect", "config-error")
						put(
							"showSpicyToast",
							EmbeddedSpicyModalUtils.encodeURIComponent(
								Json.encodeToString(
									EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, "Você já tem este item!", null)
								)
							)
						)
					}.toString(),
					status = HttpStatusCode.Conflict
				)
			}
			Result.Success -> {
				call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")
				call.respondJson(
					buildJsonObject {
						put("closeSpicyModal", null)
						put("refreshItemShop", null)
						put("playSoundEffect", "cash")
						put(
							"showSpicyToast",
							EmbeddedSpicyModalUtils.encodeURIComponent(
								Json.encodeToString(
									EmbeddedSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, "Item comprado!", null)
								)
							)
						)
					}.toString()
				)
				call.respondText("")
			}
		}
	}

	private sealed class Result {
		data object ItemNotInItemShop : Result()
		data object YouAlreadyHaveThisItem : Result()
		data object NotEnoughSonhos : Result()
		data object Success : Result()
	}
}