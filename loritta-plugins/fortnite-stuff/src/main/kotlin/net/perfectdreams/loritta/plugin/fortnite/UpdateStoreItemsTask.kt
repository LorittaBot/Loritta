package net.perfectdreams.loritta.plugin.fortnite

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.FortniteConfigs
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.FortniteServerConfigs
import net.perfectdreams.loritta.plugin.fortnite.extendedtables.TrackedFortniteItems
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.*
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO


class UpdateStoreItemsTask(val m: FortniteStuff) {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val PADDING = 8
		private val PADDING_BETWEEN_SECTIONS = 42
		private val PADDING_BETWEEN_ITEMS = 8
		private val ELEMENT_HEIGHT = 144
	}

	var task: Job? = null
	// Locale ID -> ByteArray
	var storeImages = ConcurrentHashMap<String, ByteArray>()
	val lastUpdatedAt = ConcurrentHashMap<String, Long>()

	fun start() {
		logger.info { "Starting Update Fortnite Store Items Task..." }

		task = GlobalScope.launch(LorittaLauncher.loritta.coroutineDispatcher) {
			while (true) {
				try {
					val distinctApiIds = loritta.locales.values.map {
						it["commands.fortnite.shop.localeId"]
					}.distinct()

					for (apiId in distinctApiIds) {
						logger.info { "Updated ${apiId} items locale" }
						m.itemsInfo[apiId] = jsonParser.parse(
								HttpRequest.get("https://fnapi.me/api/items/all?lang=$apiId")
										.header("Authorization", com.mrpowergamerbr.loritta.utils.loritta.config.fortniteApi.token)
										.body()
						)["data"].array
					}

					updateFortniteShop()

					logger.info { "Waiting until 15000ms for the next update..." }
				} catch (e: Exception) {
					logger.warn(e) { "Error while updating Fortnite Stuff" }
				}

				delay(15_000)
			}
		}
	}

	private suspend fun updateFortniteShop() {
		logger.info { "Updating Fortnite Shop..." }

		val distinctApiIds = loritta.locales.values.map {
			it["commands.fortnite.shop.localeId"]
		}.distinct()

		logger.info { "There are ${distinctApiIds.size} distinct API IDs: $distinctApiIds" }

		val shopsData = mutableMapOf<String, Deferred<JsonObject>>()

		distinctApiIds.forEach {
			shopsData[it] = GlobalScope.async {
				getShopData(it)
			}
		}

		var alreadyNotifiedUsers = false

		for (locale in loritta.locales.values) {
			val apiLocaleId = locale["commands.fortnite.shop.localeId"]
			logger.info { "Updating shop for ${locale.id}... API Locale ID is $apiLocaleId, Shop Data is ${shopsData[apiLocaleId]}" }

			val shopData = shopsData[apiLocaleId]?.await() ?: continue
			val newUpdatedAt = shopData["updateAt"].long
			val updatedAt = lastUpdatedAt.getOrDefault(apiLocaleId, 0L)

			val firstUpdate = updatedAt == 0L

			logger.info { "Last shop update for $apiLocaleId was at ${shopData["updateAt"].long}"}

			if (updatedAt != newUpdatedAt) {
				lastUpdatedAt[apiLocaleId] = newUpdatedAt

				logger.info { "Shop $locale was updated! Generating images and stuff..." }

				generateAndSaveStoreImage(shopData, locale)

				if (firstUpdate) {
					logger.info { "All shops were successfully updated! However this is the first update, so we won't broadcast it..." }
				} else {
					logger.info { "All shops were successfully updated! Broadcasting to every guild..." }

					if (!alreadyNotifiedUsers && loritta.isMaster) { // Se todos os clusters enviarem, úsuários vão receber 4 mensagens diferentes para o mesmo item
						GlobalScope.launch {
							notifyUsersAboutItems(shopData)
						}
						alreadyNotifiedUsers = true
					}

					broadcastNewFortniteShopItems(locale.id)
				}
			}
		}
	}

	suspend fun notifyUsersAboutItems(obj: JsonObject) {
		val items = obj["data"].array

		for (storeItem in items) {
			val itemId = storeItem["itemId"].string

			try {
				val item = (m.itemsInfo["ptbr"]!!.firstOrNull { storeItem["itemId"].string == it["itemId"].string }
						?: m.itemsInfo["en"]!!.first { storeItem["itemId"].string == it["itemId"].string })["item"].obj

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

					val locale = loritta.getLocaleById("default")

					try {
						theUser?.openPrivateChannel()?.await()?.sendMessage(
								EmbedBuilder()
										.setTitle("${Emotes.DEFAULT_DANCE} ${item["name"].string} voltou para a loja!")
										.setThumbnail(item["images"]["background"].string)
										.setDescription("O item que você pediu para ser notificado voltou para a loja! Espero que você tenha economizado os V-Bucks para comprar. ${Emotes.LORI_HAPPY}\n\nPor favor use o código de criador `MrPowerGamerBR` na loja de itens antes de comprar! Assim você me ajuda a ficar online, para que eu possa continuar a te notificar novos itens! ${Emotes.LORI_OWO}")
										.addField("\uD83D\uDD16 ${locale["commands.fortnite.item.type"]}", item["typeName"].nullString, true)
										.addField("⭐ ${locale["commands.fortnite.item.rarity"]}", item["rarityName"].nullString, true)
										.addField("<:vbucks:635158614109192199> ${locale["commands.fortnite.item.cost"]}", storeItem["store"]["cost"].nullInt.toString(), true)
										.setColor(FortniteStuff.convertRarityToColor(item["rarity"].nullString
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

	fun broadcastNewFortniteShopItems(localeId: String) {
		val guildsWithNotificationsEnabled = transaction(Databases.loritta) {
			(FortniteServerConfigs innerJoin FortniteConfigs).select {
				FortniteConfigs.advertiseNewItems eq true and FortniteConfigs.channelToAdvertiseNewItems.isNotNull()
			}.toMutableList()
		}

		guildsWithNotificationsEnabled.forEach {
			val guild = lorittaShards.getGuildById(it[FortniteServerConfigs.id].value) ?: return@forEach
			val channel = guild.getTextChannelById(it[FortniteConfigs.channelToAdvertiseNewItems] ?: return@forEach) ?: return@forEach
			val guildLocaleId = loritta.getServerConfigForGuild(guild.id).localeId

			if (guildLocaleId == localeId) {
				val storeImage = when {
					m.updateStoreItems?.storeImages?.containsKey(localeId) == true -> m.updateStoreItems!!.storeImages[localeId]
					m.updateStoreItems?.storeImages?.containsKey(Constants.DEFAULT_LOCALE_ID) == true -> m.updateStoreItems!!.storeImages[Constants.DEFAULT_LOCALE_ID]
					else -> return@forEach
				} ?: return@forEach

				channel.sendFile(
						storeImage,
						"fortnite-shop.png"
				).queue()
			}
		}
	}

	private fun getShopData(localeId: String): JsonObject {
		val shop = HttpRequest.get("https://fnapi.me/api/shop?lang=$localeId")
				.header("Authorization", loritta.config.fortniteApi.token)
				.body()

		return jsonParser.parse(shop).obj
	}

	fun getNewsData(gameMode: String, localeId: String): JsonObject {
		val news = HttpRequest.get("https://fnapi.me/api/news/?type=$gameMode&lang=$localeId")
				.header("Authorization", loritta.config.fortniteApi.token)
				.body()

		return jsonParser.parse(news).obj
	}

	private fun generateAndSaveStoreImage(parse: JsonObject, locale: BaseLocale) {
		val storeBufferedImage = generateStoreImage(parse, locale)
		val baos = ByteArrayOutputStream()

		ImageIO.write(storeBufferedImage, "png", baos)
		storeImages[locale.id] = baos.toByteArray()
	}

	private fun generateStoreImage(parse: JsonObject, locale: BaseLocale): BufferedImage {
		val width = 1024 + PADDING + PADDING_BETWEEN_SECTIONS + PADDING + (PADDING_BETWEEN_ITEMS * 4)

		val data = parse["data"].array

		val maxYFeatured = run {
			var x = 0
			var y = 36 + PADDING_BETWEEN_ITEMS + PADDING + PADDING

			data.forEach {
				if (!it["store"]["isFeatured"].bool)
					return@forEach

				if (x == 512) {
					x = 0
					y += ELEMENT_HEIGHT + PADDING_BETWEEN_ITEMS
				}

				x += 128
			}
			y + ELEMENT_HEIGHT
		}

		var nonFeaturedElementsOnLastLine = 0
		val maxYNotFeatured = run {
			var x = 0
			var y = 36 + PADDING_BETWEEN_ITEMS + PADDING + PADDING

			data.forEach {
				if (it["store"]["isFeatured"].bool)
					return@forEach

				if (x == 512) {
					x = 0
					y += ELEMENT_HEIGHT + PADDING_BETWEEN_ITEMS
				}

				x += 128
			}
			nonFeaturedElementsOnLastLine = (x / 128)
			y + ELEMENT_HEIGHT
		}

		var maxY = Math.max(maxYFeatured, maxYNotFeatured)
		val increaseYForCreatorCode = maxYFeatured == maxYNotFeatured && nonFeaturedElementsOnLastLine > 1

		if (increaseYForCreatorCode)
			maxY += 30

		val bufImage = BufferedImage(width, maxY, BufferedImage.TYPE_INT_ARGB)
		val graphics = bufImage.graphics.enableFontAntiAliasing()

		val blueToBlack = GradientPaint(0f, 0f, Color(38, 132, 225),
				0f, bufImage.height.toFloat(), Color(15, 52, 147))

		graphics.paint = blueToBlack
		graphics.fillRect(0, 0, bufImage.width, bufImage.height)
		graphics.paint = null

		// graphics.color = Color(82, 103, 138, 180)
		// graphics.fillRect(PADDING, 0 + PADDING, 512 + (PADDING_BETWEEN_ITEMS * 3), 36)
		// graphics.fillRect(512 + PADDING + PADDING_BETWEEN_SECTIONS + PADDING + (PADDING_BETWEEN_ITEMS * 3), 0 + PADDING, 512, 36)

		graphics.color = Color(203, 210, 220)
		graphics.font = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(27f)

		val subHeaderApplyPath = makeFortniteHeader(graphics.fontMetrics, locale["commands.fortnite.shop.featuredItems"])

		graphics.drawImage(subHeaderApplyPath, PADDING, 0 + PADDING, null)

		val subHeaderApplyPathItems = makeFortniteHeader(graphics.fontMetrics, locale["commands.fortnite.shop.dailyItems"])
		graphics.drawImage(subHeaderApplyPathItems, 512 + PADDING + PADDING_BETWEEN_SECTIONS + PADDING, 0 + PADDING, null)

		run {
			var x = 0
			var y = 36 + PADDING_BETWEEN_ITEMS + PADDING

			data.forEach {
				if (!it["store"]["isFeatured"].bool)
					return@forEach

				if (x >= 512 + PADDING) {
					x = 0
					y += ELEMENT_HEIGHT + PADDING_BETWEEN_ITEMS
				}

				val url = it["item"]["images"]["icon"].string

				graphics.drawImage(
						createItemBox(url, it["item"]["name"].string, it["item"]["rarity"].string, it["store"]["cost"].asInt),
						x + PADDING,
						y,
						null
				)

				x += 128 + PADDING_BETWEEN_ITEMS
			}
		}

		run {
			var x = 512 + PADDING + PADDING_BETWEEN_SECTIONS
			var y = 36 + PADDING_BETWEEN_ITEMS + PADDING

			data.forEach {
				if (it["store"]["isFeatured"].bool)
					return@forEach

				if (x >= 1024 + PADDING) {
					x = 512 + PADDING + PADDING_BETWEEN_SECTIONS
					y += ELEMENT_HEIGHT + PADDING_BETWEEN_ITEMS
				}

				val url = it["item"]["images"]["icon"].string

				graphics.drawImage(
						createItemBox(url, it["item"]["name"].string, it["item"]["rarity"].string, it["store"]["cost"].asInt),
						x + PADDING,
						y,
						null
				)

				x += 128 + PADDING_BETWEEN_ITEMS
			}
		}

		// burbank-big-condensed-bold.otf
		graphics.font = Constants.BURBANK_BIG_CONDENSED_BOLD.deriveFont(27f)
		graphics.color = Color(255, 255, 255, 120)

		val creatorCodeText = locale["commands.fortnite.shop.creatorCode", loritta.config.fortniteApi.creatorCode]
		graphics.drawString(
				creatorCodeText,
				bufImage.width - graphics.fontMetrics.stringWidth(creatorCodeText) - 15,
				bufImage.height - 15
		)

		return bufImage
	}

	private fun makeFortniteHeader(fontMetrics: FontMetrics, str: String): BufferedImage {
		val header = str
		val width = fontMetrics.stringWidth(header.toUpperCase())

		val subHeader = BufferedImage(512 + (PADDING_BETWEEN_ITEMS * 3), 36, BufferedImage.TYPE_INT_ARGB)
		val subHeaderGraphics = subHeader.graphics.enableFontAntiAliasing()

		subHeaderGraphics.font = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(27f)

		subHeaderGraphics.color = Color(255, 255, 255)
		subHeaderGraphics.drawString(header.toUpperCase(), 14, 27)

		subHeaderGraphics.color = Color(255, 255, 255, 70)

		subHeaderGraphics.fillRect(width + 18, (subHeader.height / 2) - 2, subHeader.width, 4)
		return subHeader
	}

	private fun createItemBox(itemImageUrl: String, name: String, rarity: String, price: Int): BufferedImage {
		val height = 144

		// rarity = common, uncommon, rare, epic, legendary, marvel
		val base = BufferedImage(128, 144, BufferedImage.TYPE_INT_ARGB)
		val graphics = base.graphics.enableFontAntiAliasing()

		val backgroundColor = FortniteStuff.convertRarityToColor(rarity)

		graphics.color = backgroundColor
		graphics.fillRect(0, 0, 128, height)

		val center = Point2D.Float(128f / 2, 128f / 2)
		val radius = 90f
		val dist = floatArrayOf(0.05f, .95f)

		val colors = arrayOf(Color(0, 0, 0, 0), Color(0, 0, 0, 255 / 2))
		val paint = RadialGradientPaint(center, radius, dist, colors, MultipleGradientPaint.CycleMethod.REFLECT)

		graphics.paint = paint
		graphics.fillRect(2, 2, 124, 124)

		val img = ImageIO.read(URL(itemImageUrl))

		graphics.drawImage(
				img.getScaledInstance(124, 124, BufferedImage.SCALE_SMOOTH),
				2,
				2,
				null
		)

		graphics.paint = null

		// V-Bucks
		graphics.color = Color(0, 7, 36)
		graphics.fillRect(2, height - 18, 124, 16)

		graphics.color = Color.WHITE

		val burbankBig = Constants.BURBANK_BIG_CONDENSED_BOLD.deriveFont(15f)
		graphics.font = burbankBig

		ImageUtils.drawCenteredString(
				graphics,
				price.toString(),
				Rectangle(2, height - 18, 124, 16),
				burbankBig
		)

		// Nome
		graphics.color = Color(0, 0, 0, 255 / 2)
		graphics.fillRect(2, height - 46, 124, 28)

		val burbankBig2 = Constants.BURBANK_BIG_CONDENSED_BOLD.deriveFont(20f)
		graphics.font = burbankBig2

		graphics.color = Color(0, 0, 0, 128)
		ImageUtils.drawCenteredString(
				graphics,
				name,
				Rectangle(2, 128 - 30, 124, 33),
				burbankBig2
		)
		ImageUtils.drawCenteredString(
				graphics,
				name,
				Rectangle(2, 128 - 32, 124, 33),
				burbankBig2
		)
		ImageUtils.drawCenteredString(
				graphics,
				name,
				Rectangle(3, 128 - 31, 125, 33),
				burbankBig2
		)
		ImageUtils.drawCenteredString(
				graphics,
				name,
				Rectangle(1, 128 - 31, 123, 33),
				burbankBig2
		)

		graphics.color = Color(255, 255, 255)

		ImageUtils.drawCenteredString(
				graphics,
				name,
				Rectangle(2, 128 - 30, 124, 33),
				burbankBig2
		)

		return base
	}
}