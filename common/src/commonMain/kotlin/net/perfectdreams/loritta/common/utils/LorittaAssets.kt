package net.perfectdreams.loritta.common.utils

import net.perfectdreams.loritta.common.utils.image.Image

interface LorittaAssets {
	suspend fun loadImage(path: String, storeInCache: Boolean = false, loadFromCache: Boolean = true): Image
}