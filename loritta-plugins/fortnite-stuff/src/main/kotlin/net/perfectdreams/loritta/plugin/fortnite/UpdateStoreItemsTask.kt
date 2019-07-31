package net.perfectdreams.loritta.plugin.fortnite

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.awt.*
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
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
	var storeImage: ByteArray? = null

	fun start() {
		logger.info { "Starting Update Fortnite Store Items Task..." }

		generateAndSaveStoreImage(loritta.getLocaleById("default"))

		task = GlobalScope.launch(LorittaLauncher.loritta.coroutineDispatcher) {
			while (true) {
				val zoneId = ZoneId.of("UTC");

				val now = ZonedDateTime.now(zoneId)
				val tomorrow = now.toLocalDate().plusDays(1)
				val tomorrowStart = tomorrow.atStartOfDay(zoneId)

				val duration = Duration.between(now, tomorrowStart)
				val millisecondsUntilTomorrow = duration.toMillis()

				logger.info { "Waiting until ${millisecondsUntilTomorrow}ms for the next update..." }
				delay(millisecondsUntilTomorrow)
				logger.info { "Updating Fortnite Shop..." }

				generateAndSaveStoreImage(loritta.getLocaleById("default"))
			}
		}
	}

	fun generateAndSaveStoreImage(locale: BaseLocale) {
		val storeBufferedImage = generateStoreImage(locale)
		val baos = ByteArrayOutputStream()

		ImageIO.write(storeBufferedImage, "png", baos)
		storeImage = baos.toByteArray()
	}

	fun generateStoreImage(locale: BaseLocale): BufferedImage {
		val width = 1024 + PADDING + PADDING_BETWEEN_SECTIONS + PADDING + (PADDING_BETWEEN_ITEMS * 4)

		val shop = HttpRequest.get("https://fnapi.ga/api/shop?lang=ptbr")
				.header("Authorization", loritta.config.fortniteApi.token)
				.body()

		val parse = jsonParser.parse(shop)
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
		val graphics = bufImage.graphics.apply { enableFontAntiAliasing(this) }

		graphics.color = Color(30, 30, 30)
		graphics.fillRect(0, 0, bufImage.width, bufImage.height)

		graphics.color = Color(82, 103, 138, 180)
		graphics.fillRect(PADDING, 0 + PADDING, 512 + (PADDING_BETWEEN_ITEMS * 3), 36)
		graphics.fillRect(512 + PADDING + PADDING_BETWEEN_SECTIONS + PADDING + (PADDING_BETWEEN_ITEMS * 3), 0 + PADDING, 512, 36)

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
		graphics.color = Color(60, 60, 60)

		val creatorCodeText = locale["commands.fortnite.shop.creatorCode", loritta.config.fortniteApi.creatorCode]
		graphics.drawString(
				creatorCodeText,
				bufImage.width - graphics.fontMetrics.stringWidth(creatorCodeText) - 15,
				bufImage.height - 15
		)

		return bufImage
	}

	fun enableFontAntiAliasing(graphics: Graphics): Graphics2D {
		graphics as Graphics2D
		graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
		return graphics
	}

	fun makeFortniteHeader(fontMetrics: FontMetrics, str: String): BufferedImage {
		val header = str
		val width = fontMetrics.stringWidth(header)

		val subHeader = BufferedImage(14 + width + 14 + 14, 36, BufferedImage.TYPE_INT_ARGB)
		val subHeaderGraphics = subHeader.graphics.apply { enableFontAntiAliasing(this) }

		subHeaderGraphics.font = Constants.BURBANK_BIG_CONDENSED_BLACK.deriveFont(27f)

		subHeaderGraphics.color = Color(83, 104, 137)
		subHeaderGraphics.fillRect(0, 0, 1024, 36)

		subHeaderGraphics.color = Color(203, 210, 220)
		subHeaderGraphics.drawString(header, 14, 27)

		val subHeaderApplyPath = BufferedImage(subHeader.width, subHeader.height, BufferedImage.TYPE_INT_ARGB)

		val path = Path2D.Double()
		path.moveTo(0.0, 0.0)
		path.lineTo(14.0 + width.toDouble() + 14.0, 0.0)
		path.lineTo(14.0 + width.toDouble() + 14.0 + 14.0, 36.0)
		path.lineTo(0.0, 36.0)
		path.closePath()

		val sHAPG = subHeaderApplyPath.graphics
		sHAPG.clip = path
		sHAPG.drawImage(subHeader, 0, 0, null)

		return subHeaderApplyPath
	}

	fun createItemBox(itemImageUrl: String, name: String, rarity: String, price: Int): BufferedImage {
		var height = 144

		// rarity = common, uncommon, rare, epic, legendary, marvel
		val base = BufferedImage(128, 144, BufferedImage.TYPE_INT_ARGB)
		val graphics = base.graphics.apply { enableFontAntiAliasing(this) } as Graphics2D

		val backgroundColor = when (rarity) {
			"uncommon" -> Color(64, 136, 1)
			"rare" -> Color(0, 125, 187)
			"epic" -> Color(151, 60, 195)
			"legendary" -> Color(195, 119, 58)
			"marvel" -> Color(213, 186, 99)
			else -> Color(176, 176, 150)
		}

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