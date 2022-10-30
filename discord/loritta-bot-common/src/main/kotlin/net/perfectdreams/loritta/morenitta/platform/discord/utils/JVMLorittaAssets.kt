package net.perfectdreams.loritta.morenitta.platform.discord.utils

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.utils.LorittaAssets
import net.perfectdreams.loritta.common.utils.image.Image
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.io.File

class JVMLorittaAssets(val loritta: LorittaBot) : LorittaAssets {
    val cachedImages = mutableMapOf<String, Image>()

    override suspend fun loadImage(path: String, storeInCache: Boolean, loadFromCache: Boolean): Image {
        if (loadFromCache && cachedImages.containsKey(path))
            return cachedImages[path]!!

        val image = JVMImage(readImage(File(LorittaBot.ASSETS + path)))

        if (storeInCache)
            cachedImages[path] = image

        return image
    }
}