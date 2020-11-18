package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.mrpowergamerbr.loritta.Loritta
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.SimpleImageInfo
import net.perfectdreams.loritta.utils.extensions.readImage
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.BaseRoute
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

class GetFanArtImageController(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/fan-art/{artist}/{art-name}/image") {
	override suspend fun onRequest(call: ApplicationCall) {
		val artistName = call.parameters["artist"]
		val artName = call.parameters["art-name"]
		val size = call.parameters["size"]

		val fanArt = loritta.fanArtArtists.first { it.id == artistName }
				.fanArts.firstOrNull { it.fileName == artName }

		val cacheKey = "$artistName#$artName#$size"

		// First we get from the cache, if it exists, then we just ignore and don't try to retrieve it again
		var cachedThumbnail = LorittaWebsite.cachedFanArtThumbnails.getIfPresent(cacheKey)

		if (cachedThumbnail == null) {
			if (fanArt != null) {
				val fanArtFile = File(Loritta.FRONTEND, "/static/assets/img/fanarts/${fanArt.fileName}")
				val info = SimpleImageInfo(fanArtFile)
				if (info.mimeType == "image/gif") {
					// Just send it as it is
					LorittaWebsite.cachedFanArtThumbnails.put(
							cacheKey,
							LorittaWebsite.Companion.CachedThumbnail(
									ContentType.Image.GIF,
									fanArtFile.readBytes()
							)
					)
				} else {
					// Try to load the image to compress it to jpg
					val fanArtImage = readImage(fanArtFile)

					val targetHeight = when (size) {
						"small" -> 256
						else -> throw RuntimeException("Unsupported Size $size")
					}

					// currentHeight --- currentWidth
					// targetHeight --- x
					val targetWidth = (targetHeight * fanArtImage.width) / fanArtImage.height

					val scaledImage = fanArtImage.getScaledInstance(targetWidth, targetHeight, BufferedImage.SCALE_SMOOTH)

					// We can't write directly to "jpeg", this will cause issues (empty image file) due to color issues
					// So we need to create our own image
					val imageCopy = BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_RGB)
					val graphics = imageCopy.graphics
					graphics.color = Color.WHITE
					// Avoid transparency issues
					graphics.fillRect(0, 0, imageCopy.width, imageCopy.height)
					// Now copy the scaled image to here!
					graphics.drawImage(scaledImage, 0, 0, null)

					val baos = ByteArrayOutputStream()
					ImageIO.write(imageCopy, "jpeg", baos)
					val byteArray = baos.toByteArray()

					LorittaWebsite.cachedFanArtThumbnails.put(
							cacheKey,
							LorittaWebsite.Companion.CachedThumbnail(
									ContentType.Image.JPEG,
									byteArray
							)
					)
				}
			}
		}

		// Now we check the cache again, because maybe we inserted the fan art in the cache!
		cachedThumbnail = LorittaWebsite.cachedFanArtThumbnails.getIfPresent(cacheKey)

		if (cachedThumbnail != null)
			call.respondBytes(cachedThumbnail.thumbnailBytes, cachedThumbnail.type)
	}
}