package net.perfectdreams.loritta.plugin.fortnite

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.gson
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.plugin.fortnite.tables.TrackedFortniteItems
import net.perfectdreams.loritta.utils.ClusterOfflineException
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import kotlin.collections.set

class UpdateStoreItemsTask(val m: FortniteStuff) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	// Locale ID -> Shop Hash
	val lastUpdatedAt = ConcurrentHashMap<String, String>()
	var lastItemListPostUpdate = -1L

	fun start() {
		logger.info { "Starting Update Fortnite Store Items Task..." }

		m.launch {
			while (true) {
				try {
					if (loritta.isMaster) {
						delay(5_000)

						if (System.currentTimeMillis() - lastItemListPostUpdate >= 900_000) {
							lastItemListPostUpdate = System.currentTimeMillis()
							logger.info { "Updating Fortnite Items..." }
							val distinctApiIds = loritta.localeManager.locales.values.map {
								it["commands.command.fnshop.localeId"]
							}.distinct()

							for (apiId in distinctApiIds) {
								val result = loritta.http.get<String>("https://fortnite-api.com/v2/cosmetics/br?language=$apiId") {
									userAgent(loritta.lorittaCluster.getUserAgent())
									header("Authorization", com.mrpowergamerbr.loritta.utils.loritta.config.fortniteApi.token)
								}

								val clusters = loritta.config.clusters

								clusters.map {
									GlobalScope.async(loritta.coroutineDispatcher) {
										withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
											val response = loritta.http.post<HttpResponse>("https://${it.getUrl()}/api/v1/fortnite/items/$apiId") {
												userAgent(loritta.lorittaCluster.getUserAgent())
												header("Authorization", loritta.lorittaInternalApiKey.name)
												body = result
											}

											logger.info { "${it.getUserAgent()} replied with ${response.status} when updating the Fortnite Item List!" }
										}
									}
								}
							}
						}

						updateFortniteShop()
					}

					logger.info { "Waiting until 60000ms for the next update..." }
				} catch (e: Exception) {
					logger.warn(e) { "Error while updating Fortnite Stuff" }
				}

				delay(60_000)
			}
		}
	}

	private suspend fun updateFortniteShop() {
		logger.info { "Updating Fortnite Shop..." }

		val distinctApiIds = loritta.localeManager.locales.values.map {
			it["commands.command.fnshop.localeId"]
		}.distinct()

		logger.info { "There are ${distinctApiIds.size} distinct API IDs: $distinctApiIds" }

		val shopsData = mutableMapOf<String, Deferred<JsonObject>>()

		distinctApiIds.forEach {
			shopsData[it] = GlobalScope.async {
				getShopData(it)
			}
		}

		var alreadyNotifiedUsers = false

		for (locale in loritta.localeManager.locales.values) {
			val apiLocaleId = locale["commands.command.fnshop.localeId"]
			logger.info { "Updating shop for ${locale.id}... API Locale ID is $apiLocaleId, Shop Data is ${shopsData[apiLocaleId]}" }

			val shopData = try { shopsData[apiLocaleId]?.await() } catch (e: Exception) { continue } ?: continue
			val newUpdatedAt = shopData["hash"].string
			val updatedAt = lastUpdatedAt.getOrDefault(apiLocaleId, null)

			val firstUpdate = updatedAt == null
			var isNew = false

			val instant = Instant.parse(shopData["date"].string)
			val instantAtOffset = instant.atOffset(ZoneOffset.UTC)

			val year = instantAtOffset.year
			val month = instantAtOffset.monthValue.toString().padStart(2, '0')
			val day = instantAtOffset.dayOfMonth.toString().padStart(2, '0')

			logger.info { "Last shop update for $apiLocaleId was at ${shopData["date"].string} (${year}_${month}_${day}) New updated at = $newUpdatedAt"}

			val fileName = "${locale.id}-${year}_${month}_${day}.png"

			if (updatedAt != newUpdatedAt) {
				lastUpdatedAt[apiLocaleId] = newUpdatedAt

				logger.info { "Shop $locale was updated! Generating images and stuff..." }

				generateAndSaveStoreImage(shopData, locale, fileName)

				if (firstUpdate) {
					logger.info { "All shops were successfully updated! However this is the first update, so we won't broadcast it..." }
				} else {
					logger.info { "All shops were successfully updated! Broadcasting to every guild..." }
					isNew = true

					if (!alreadyNotifiedUsers && loritta.isMaster) { // Se todos os clusters enviarem, úsuários vão receber 4 mensagens diferentes para o mesmo item
						GlobalScope.launch {
							notifyUsersAboutItems(shopData)
						}
						alreadyNotifiedUsers = true
					}
				}
			}

			logger.info { "Relaying Fortnite shop info to other clusters..." }
			val clusters = loritta.config.clusters

			clusters.map {
				GlobalScope.async(loritta.coroutineDispatcher) {
					try {
						withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
							val response = loritta.http.post<HttpResponse>("https://${it.getUrl()}/api/v1/fortnite/shop") {
								header("Authorization", loritta.lorittaInternalApiKey.name)
								userAgent(loritta.lorittaCluster.getUserAgent())

								body = gson.toJson(
										jsonObject(
												"fileName" to fileName,
												"localeId" to locale.id,
												"isNew" to isNew
										)
								)
							}

							val body = response.readText()
							JsonParser.parseString(
									body
							)
						}
					} catch (e: Exception) {
						logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
						throw ClusterOfflineException(it.id, it.name)
					}
				}
			}
		}
	}

	suspend fun notifyUsersAboutItems(obj: JsonObject) {
		val featuredItems = obj["featured"]["entries"].array.flatMap { it["items"].array }
		val dailyItems = obj["daily"]["entries"].array.flatMap { it["items"].array }
		val items = featuredItems + dailyItems

		for (storeItem in items) {
			val itemId = storeItem["id"].string

			try {
				logger.info { "Finding users that are tracking ${itemId}..." }

				val usersThatAreTrackingThisItem = transaction(Databases.loritta) {
					TrackedFortniteItems.innerJoin(Profiles).select {
						TrackedFortniteItems.itemId eq itemId
					}.toMutableList()
				}

				logger.info { "There are ${usersThatAreTrackingThisItem.size} users that are tracking this item" }

				for (user in usersThatAreTrackingThisItem) {
					logger.info { "Sending DM to ${user[Profiles.id].value}..." }
					val theUser = lorittaShards.retrieveUserById(user[Profiles.id].value)

					val locale = loritta.localeManager.getLocaleById("default")

					try {
						theUser?.openPrivateChannel()?.await()?.sendMessage(
								EmbedBuilder()
										.setTitle("${Emotes.DEFAULT_DANCE} ${storeItem["name"].string} voltou para a loja!")
										.setThumbnail(storeItem["images"]["smallIcon"].string)
										.setDescription("O item que você pediu para ser notificado voltou para a loja! Espero que você tenha economizado os V-Bucks para comprar. ${Emotes.LORI_HAPPY}\n\nPor favor use o código de criador `MrPowerGamerBR` na loja de itens antes de comprar! Assim você me ajuda a ficar online, para que eu possa continuar a te notificar novos itens! ${Emotes.LORI_OWO}")
										.addField("\uD83D\uDD16 ${locale["commands.command.fnitem.type"]}", storeItem["type"]["displayValue"].nullString, true)
										.addField("⭐ ${locale["commands.command.fnitem.rarity"]}", storeItem["rarity"]["displayValue"].nullString, true)
										.setColor(FortniteStuff.convertRarityToColor(storeItem["rarity"]["value"].nullString
												?: "???"))
										.build()
						)?.await()
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
			} catch (e: Exception) {
				logger.warn(e) { "Error while trying to notify users about $itemId in shop" }
			}
		}
	}

	private fun getShopData(localeId: String): JsonObject {
		logger.info { "Getting shop data for locale $localeId" }
		val shop = HttpRequest.get("https://fortnite-api.com/v2/shop/br?language=$localeId")
				.connectTimeout(15_000)
				.readTimeout(15_000)
				.header("Authorization", loritta.config.fortniteApi.token)
				.body()

		return JsonParser.parseString(shop)["data"].obj
	}

	fun getNewsData(gameMode: String, localeId: String): JsonObject {
		logger.info { "Getting news data (game mode: $gameMode) for locale $localeId" }

		val news = HttpRequest.get("https://fortnite-api.com/v2/news/$gameMode?language=$localeId")
				.connectTimeout(15_000)
				.readTimeout(15_000)
				.header("Authorization", loritta.config.fortniteApi.token)
				.body()

		return JsonParser.parseString(news).obj
	}

	private suspend fun generateAndSaveStoreImage(parse: JsonObject, locale: BaseLocale, fileName: String) {
		val generator = FortniteShopGenerator(parse, loritta.config.fortniteApi.creatorCode)
		val storeBufferedImage = generator.generateStoreImage(locale)

		ImageIO.write(storeBufferedImage, "png", File(loritta.instanceConfig.loritta.website.folder, "/static/assets/img/fortnite/shop/$fileName"))
	}
}