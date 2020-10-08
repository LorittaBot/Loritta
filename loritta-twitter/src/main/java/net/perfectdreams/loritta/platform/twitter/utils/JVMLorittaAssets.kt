package net.perfectdreams.loritta.platform.twitter.utils

import net.perfectdreams.loritta.api.utils.LorittaAssets
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.twitter.LorittaTwitter
import java.io.File
import javax.imageio.ImageIO

class JVMLorittaAssets(val loritta: LorittaTwitter) : LorittaAssets {
	val cachedImages = mutableMapOf<String, Image>()

	override suspend fun loadImage(path: String, storeInCache: Boolean, loadFromCache: Boolean): Image {
		if (loadFromCache && cachedImages.containsKey(path))
			return cachedImages[path]!!

		val image = JVMImage(ImageIO.read(File(loritta.config.assetsFolder, path)))

		if (storeInCache)
			cachedImages[path] = image

		return image
	}
}