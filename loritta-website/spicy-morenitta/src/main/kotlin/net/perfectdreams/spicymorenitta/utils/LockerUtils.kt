package net.perfectdreams.spicymorenitta.utils

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JSON
import net.perfectdreams.loritta.datawrapper.Background
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.routes.user.dashboard.DailyShopDashboardRoute
import org.khronos.webgl.Uint8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import kotlin.browser.window

object LockerUtils : Logging {
	suspend fun prepareBackgroundCanvasPreview(m: SpicyMorenitta, background: Background, canvasPreview: HTMLCanvasElement): CanvasPreviewDownload {
		val job = m.async {
			if (background.imageFile.endsWith(".loribg")) {
				// Animated BG
				debug("Downloading animated background...")
				val response = http.get<HttpResponse>("${window.location.origin}/assets/img/profiles/backgrounds/${background.imageFile}")
				val array = Uint8Array(response.readBytes().toTypedArray())
				debug("Bytes: $array")
				val jsZip = JSZip()

				debug("Loading bytes async...")
				val zip = jsZip.loadAsync(array).await()
				debug("Zip: $zip")
				val backgroundDataAsString = zip.file("background.json").async("string").await()
				val backgroundData = JSON.nonstrict.parse(DailyShopDashboardRoute.AnimatedBackground.serializer(), backgroundDataAsString as String)

				val frames = mutableListOf<Image>()
				val jobs = backgroundData.frames.map {
					m.async {
						val byteArray = zip.file("frames/${it}").async("uint8array").await() as Uint8Array
						val byteArrayAsString = window.btoa(js("String.fromCharCode").apply(null, byteArray))
						val image = Image()
						image.awaitLoad("data:image/png;base64,$byteArrayAsString")
						image
					}
				}

				jobs.awaitAll().forEach {
					frames.add(it)
				}

				// keep updating canvas
				m.launch {
					while (true) {
						for (frame in frames) {
							val canvasPreviewContext = (canvasPreview.getContext("2d")!! as CanvasRenderingContext2D)
							canvasPreviewContext.clearRect(0.0, 0.0, 800.0, 600.0)
							canvasPreviewContext
									.drawImage(
											frame,
											0.0,
											0.0,
											800.0,
											600.0
									)

							delay(backgroundData.speed.toLong() * 10)
						}
					}
				}

				CanvasPreviewDownload(frames.first())
			} else {
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
		}

		return job.await()
	}

	data class CanvasPreviewDownload(
			val image: Image
	)
}