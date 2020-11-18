package net.perfectdreams.loritta.platform.discord.utils

import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.utils.LorittaAssets
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.utils.extensions.readImage
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