package net.perfectdreams.loritta.dashboard.frontend.compose.components.colorpicker

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import web.canvas.CanvasRenderingContext2D
import web.canvas.ID
import web.html.HTMLCanvasElement

/**
 * A color previewer that uses Gabriela's hair/sweater details/brush tip as a preview. Sweet, huh?
 */
class GabrielaColorPreviewCanvas(private var currentColor: HSBColor) {
    private lateinit var baseImageWrapper: CoroutineImageWrapper
    private lateinit var sweaterOverlayImageWrapper: CoroutineImageWrapper
    private lateinit var canvas: HTMLCanvasElement
    private lateinit var sweaterCanvas: HTMLCanvasElement

    fun mountIn(canvas: HTMLCanvasElement) {
        this.canvas = canvas

        canvas.width = (300 * window.devicePixelRatio).toInt()
        canvas.height = (500 * window.devicePixelRatio).toInt()

        val presets = EtherealGambiUtils.scaleDownToWidthVariantsPresets.sortedBy { it.width }
        val bestMatch = presets.firstOrNull { it.width >= canvas.width }

        // Mimicks html's srcset
        // We want to get the FIRST preset that's ABOVE the max resolution
        if (bestMatch != null) {
            baseImageWrapper = CoroutineImageWrapper("https://stuff.loritta.website/gabriela-art-brush-sortros@${bestMatch.name}.png")
            sweaterOverlayImageWrapper = CoroutineImageWrapper("https://stuff.loritta.website/gabriela-art-brush-sortros-overlay@${bestMatch.name}.png")
        } else {
            baseImageWrapper = CoroutineImageWrapper("https://stuff.loritta.website/gabriela-art-brush-sortros.png")
            sweaterOverlayImageWrapper = CoroutineImageWrapper("https://stuff.loritta.website/gabriela-art-brush-sortros-overlay.png")
        }

        // Create a new canvas that will be used to manipulate the sweater color
        sweaterCanvas = document.createElement("canvas") as HTMLCanvasElement
        sweaterCanvas.width = canvas.width
        sweaterCanvas.height = canvas.height
    }

    fun unmount() {
        canvas.remove()
        sweaterCanvas.remove()
    }

    suspend fun updateColor(color: HSBColor) {
        this.currentColor = color
        render()
    }

    suspend fun render() = coroutineScope {
        val baseJob = async { baseImageWrapper.load() }
        val sweaterOverlayJob = async { sweaterOverlayImageWrapper.load() }

        val base = baseJob.await()
        val sweaterOverlay = sweaterOverlayJob.await()

        // Get a reference to the canvas element and its 2D drawing context
        val sweaterCtx = sweaterCanvas.getContext(CanvasRenderingContext2D.ID) as CanvasRenderingContext2D

        sweaterCtx.clearRect(0.0, 0.0, sweaterCanvas.width.toDouble(), sweaterCanvas.height.toDouble())
        sweaterCtx.drawImage(sweaterOverlay, 0.0, 0.0, sweaterCanvas.width.toDouble(), sweaterCanvas.height.toDouble())

        // Get the pixel data of the entire canvas
        val imageData = sweaterCtx.getImageData(0, 0, sweaterCanvas.width, sweaterCanvas.height)
        val pixelData = imageData.data // This is a one-dimensional array
        var i = 0
        while (pixelData.length > i) {
            // https://youtrack.jetbrains.com/issue/KT-24583/JS-Uint8ClampedArray-declaration-unusable
            // Each pixel consists of four values: red, green, blue, and alpha (transparency)
            val red = pixelData.asDynamic()[i] as Int
            val green = pixelData.asDynamic()[i + 1] as Int
            val blue = pixelData.asDynamic()[i + 2] as Int
            val alpha = pixelData.asDynamic()[i + 3] as Int

            // No need to process pixels with 100% alpha
            if (alpha != 0) {
                val sweaterHsbVals = ColorUtils.RGBtoHSB(red, green, blue, null)

                val newColor = Color(
                    ColorUtils.HSBtoRGB(
                        currentColor.hue,
                        currentColor.saturation,
                        // The -0.6f offset is to make the sweater shading visible, even if you are selecting 100% white
                        (sweaterHsbVals[2] + currentColor.brightness - 0.6f).coerceIn(0f..1f)
                    )
                )

                // You can manipulate the pixel values here if needed
                // For example, you can change the color of each pixel:
                pixelData.asDynamic()[i] = newColor.red // Red
                pixelData.asDynamic()[i + 1] = newColor.green // Green
                pixelData.asDynamic()[i + 2] = newColor.blue // Blue
                pixelData.asDynamic()[i + 3] = alpha // Reuse set alpha
            }

            i += 4
        }

        // Put the modified pixel data back on the canvas
        sweaterCtx.putImageData(imageData, 0, 0)

        // Get a reference to the canvas element and its 2D drawing context
        val baseCtx = canvas.getContext(CanvasRenderingContext2D.ID) as CanvasRenderingContext2D

        // Clear the original canvas
        baseCtx.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())

        // Draw the base image
        baseCtx.drawImage(base, 0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())

        // https://stackoverflow.com/a/53239232/7271796
        baseCtx.drawImage(sweaterCanvas, 0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
    }
}