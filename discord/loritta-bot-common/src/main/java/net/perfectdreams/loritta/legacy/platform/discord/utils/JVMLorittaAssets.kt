package net.perfectdreams.loritta.legacy.platform.discord.utils

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.utils.LorittaAssets
import net.perfectdreams.loritta.common.utils.image.Image
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.legacy.utils.extensions.readImage
import java.io.File
import javax.imageio.ImageIO

class JVMLorittaAssets(val loritta: LorittaBot) : LorittaAssets {
	val cachedImages = mutableMapOf<String, Image>()

	override suspend fun loadImage(path: String, storeInCache: Boolean, loadFromCache: Boolean): Image {
		if (loadFromCache && cachedImages.containsKey(path))
			return cachedImages[path]!!

		val image = JVMImage(readImage(File(Loritta.ASSETS + path)))

		if (storeInCache)
			cachedImages[path] = image

		return image
	}
}