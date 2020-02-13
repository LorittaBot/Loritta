package net.perfectdreams.loritta.platform.nodejs.utils
import net.perfectdreams.loritta.api.utils.LorittaAssets
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JSImage
import nodecanvas.createCanvas
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class JSLorittaAssets : LorittaAssets {
	override suspend fun loadImage(path: String, storeInCache: Boolean, loadFromCache: Boolean): Image {
		return suspendCoroutine { cont ->
			nodecanvas.loadImage("assets/$path").then({ it: dynamic ->
				val canvas = createCanvas(it.width, it.height)
				canvas.getContext("2d").drawImage(it, 0.0, 0.0)
				val jsImage = JSImage(canvas)
				cont.resume(jsImage)
			}, { cont.resumeWithException(it) }
			)
		}
	}
}