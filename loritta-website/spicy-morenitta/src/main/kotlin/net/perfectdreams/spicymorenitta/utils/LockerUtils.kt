package net.perfectdreams.spicymorenitta.utils

import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.ProfileDesign
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import kotlinx.browser.window

object LockerUtils : Logging {
	suspend fun prepareBackgroundCanvasPreview(m: SpicyMorenitta, background: Background, canvasPreview: HTMLCanvasElement): CanvasPreviewDownload {
		val job = m.async {
			// Normal BG
			val backgroundImg = Image()
			backgroundImg.awaitLoad("${window.location.origin}/assets/img/profiles/backgrounds/${background.imageFile}")
			val canvasPreviewContext = (canvasPreview.getContext("2d")!! as CanvasRenderingContext2D)

			canvasPreviewContext
					.drawImage(
							backgroundImg,
							(background.crop?.offsetX ?: 0).toDouble(),
							(background.crop?.offsetY ?: 0).toDouble(),
							(background.crop?.width ?: backgroundImg.width).toDouble(),
							(background.crop?.height ?: backgroundImg.height).toDouble(),
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