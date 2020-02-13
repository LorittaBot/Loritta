package net.perfectdreams.loritta.api.utils

import net.perfectdreams.loritta.api.utils.image.Image

interface LorittaAssets {
	suspend fun loadImage(path: String, storeInCache: Boolean = false, loadFromCache: Boolean = true): Image
}