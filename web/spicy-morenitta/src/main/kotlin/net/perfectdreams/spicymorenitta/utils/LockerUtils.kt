package net.perfectdreams.spicymorenitta.utils

import kotlinx.browser.window
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundVariation
import net.perfectdreams.loritta.legacy.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.legacy.common.utils.StoragePaths
import net.perfectdreams.loritta.legacy.serializable.ProfileDesign
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image

object LockerUtils : Logging {
	fun getBackgroundUrl(dreamStorageServiceUrl: String, namespace: String, backgroundVariation: BackgroundVariation): String {
		val extension = MediaTypeUtils.convertContentTypeToExtension(backgroundVariation.preferredMediaType)
		return "$dreamStorageServiceUrl/$namespace/${StoragePaths.Background(backgroundVariation.file).join()}.$extension"
	}

	fun getBackgroundUrlWithCropParameters(dreamStorageServiceUrl: String, namespace: String, backgroundVariation: BackgroundVariation): String {
		var url = getBackgroundUrl(dreamStorageServiceUrl, namespace, backgroundVariation)
		val crop = backgroundVariation.crop
		if (crop != null)
			url += "?crop_x=${crop.x}&crop_y=${crop.y}&crop_width=${crop.width}&crop_height=${crop.height}"
		return url
	}

	suspend fun prepareBackgroundCanvasPreview(m: SpicyMorenitta, dreamStorageServiceUrl: String, namespace: String, backgroundVariation: BackgroundVariation, canvasPreview: HTMLCanvasElement): CanvasPreviewDownload {
		val job = m.async {
			// Normal BG
			val backgroundImg = Image()
			backgroundImg.awaitLoad(getBackgroundUrl(dreamStorageServiceUrl, namespace, backgroundVariation))
			val canvasPreviewContext = (canvasPreview.getContext("2d")!! as CanvasRenderingContext2D)

			canvasPreviewContext
					.drawImage(
							backgroundImg,
							(backgroundVariation.crop?.x ?: 0).toDouble(),
							(backgroundVariation.crop?.y ?: 0).toDouble(),
							(backgroundVariation.crop?.width ?: backgroundImg.width).toDouble(),
							(backgroundVariation.crop?.height ?: backgroundImg.height).toDouble(),
							0.0,
							0.0,
							800.0,
							600.0
					)

			CanvasPreviewDownload(backgroundImg)
		}

		return job.await()
	}

	suspend fun prepareProfileDesignsCanvasPreview(m: SpicyMorenitta, profileDesign: ProfileDesign, canvasPreview: HTMLCanvasElement): CanvasPreviewDownload {
		val job = m.async {
			// Normal BG
			val backgroundImg = Image()
			backgroundImg.awaitLoad("${window.location.origin}/api/v1/users/@me/profile?type=${profileDesign.internalName}")
			val canvasPreviewContext = (canvasPreview.getContext("2d")!! as CanvasRenderingContext2D)

			canvasPreviewContext
					.drawImage(
							backgroundImg,
							0.0,
							0.0,
							backgroundImg.width.toDouble(),
							backgroundImg.height.toDouble(),
							0.0,
							0.0,
							800.0,
							600.0
					)

			CanvasPreviewDownload(backgroundImg)
		}

		return job.await()
	}

	data class CanvasPreviewDownload(
			val image: Image
	)
}