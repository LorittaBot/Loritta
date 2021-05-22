package net.perfectdreams.loritta.plugin.fortnite

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.nullObj
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import mu.KotlinLogging
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.FontMetrics
import java.awt.GradientPaint
import java.awt.MultipleGradientPaint
import java.awt.RadialGradientPaint
import java.awt.Rectangle
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.net.URL

class FortniteShopGenerator(val parse: JsonObject, val creatorCode: String) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val PADDING = 8
        private val PADDING_BETWEEN_SECTIONS = 42
        private val PADDING_BETWEEN_ITEMS = 8
        private val ELEMENT_HEIGHT = 144
    }

    suspend fun generateStoreImage(locale: BaseLocale): BufferedImage {
        val width = 1024 + PADDING + PADDING_BETWEEN_SECTIONS + PADDING + (PADDING_BETWEEN_ITEMS * 4)

        val data = parse

        // Both of those entries CAN BE NULL for some reason, so we get them but as a "can be null" thing
        // This happened after the leaking breakage, when the old defaults were in the shop
        val featuredEntries = data["featured"].nullObj?.get("entries")
                ?.nullArray

        val dailyEntries = data["daily"].nullObj?.get("entries")
                ?.nullArray

        val maxYFeatured = if (featuredEntries != null) {
            run {
                var x = 0
                var y = 36 + PADDING_BETWEEN_ITEMS + PADDING + PADDING

                repeat(featuredEntries.size()) {
                    if (x == 512) {
                        x = 0
                        y += ELEMENT_HEIGHT + PADDING_BETWEEN_ITEMS
                    }

                    x += 128
                }

                y + ELEMENT_HEIGHT
            }
        } else 0

        var nonFeaturedElementsOnLastLine = 0
        val maxYNotFeatured = if (dailyEntries != null) {
            run {
                var x = 0
                var y = 36 + PADDING_BETWEEN_ITEMS + PADDING + PADDING

                repeat(dailyEntries.size()) {
                    if (x == 512) {
                        x = 0
                        y += ELEMENT_HEIGHT + PADDING_BETWEEN_ITEMS
                    }

                    x += 128
                }

                nonFeaturedElementsOnLastLine = (x / 128)
                y + ELEMENT_HEIGHT
            }
        } else 0

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

        val subHeaderApplyPath = makeFortniteHeader(graphics.fontMetrics, locale["commands.command.fnshop.featuredItems"])

        graphics.drawImage(subHeaderApplyPath, PADDING, 0 + PADDING, null)

        val subHeaderApplyPathItems = makeFortniteHeader(graphics.fontMetrics, locale["commands.command.fnshop.dailyItems"])
        graphics.drawImage(subHeaderApplyPathItems, 512 + PADDING + PADDING_BETWEEN_SECTIONS + PADDING, 0 + PADDING, null)

        run {
            var x = 0
            var y = 36 + PADDING_BETWEEN_ITEMS + PADDING

            featuredEntries?.forEach {
                if (x >= 512 + PADDING) {
                    x = 0
                    y += ELEMENT_HEIGHT + PADDING_BETWEEN_ITEMS
                }

                println(it)

                val firstItem = it["items"].array.first()
                val url = firstItem["images"]["featured"].nullString
                        ?: firstItem["images"]["icon"].nullString
                        ?: firstItem["images"]["smallIcon"].string

                graphics.drawImage(
                        createItemBox(url, firstItem["name"].string, firstItem["rarity"]["value"].string, it["regularPrice"].asInt),
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

            dailyEntries?.forEach {
                if (x >= 1024 + PADDING) {
                    x = 512 + PADDING + PADDING_BETWEEN_SECTIONS
                    y += ELEMENT_HEIGHT + PADDING_BETWEEN_ITEMS
                }

                val firstItem = it["items"].array.first()
                val url = firstItem["images"]["icon"].nullString
                        ?: firstItem["images"]["smallIcon"].string

                graphics.drawImage(
                        createItemBox(url, firstItem["name"].string, firstItem["rarity"]["value"].string, it["regularPrice"].asInt),
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

        val creatorCodeText = locale["commands.command.fnshop.creatorCode", creatorCode]
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

    private suspend fun createItemBox(itemImageUrl: String, name: String, rarity: String, price: Int): BufferedImage {
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

        val img = readImage(URL(itemImageUrl))

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