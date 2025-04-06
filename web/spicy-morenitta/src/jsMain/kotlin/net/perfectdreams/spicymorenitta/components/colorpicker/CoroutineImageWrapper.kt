package net.perfectdreams.spicymorenitta.components.colorpicker

import js.function.JsFunction
import kotlinx.coroutines.suspendCancellableCoroutine
import web.events.EventHandler
import web.html.Image
import web.http.CrossOrigin
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A [Image] can be loaded asynchornously with Coroutines
 */
class CoroutineImageWrapper(val src: String) {
    var loaded: Boolean = false
    var image = Image()

    suspend fun load(): Image {
        if (loaded)
            return image

        // Required for CORS
        image.crossOrigin = CrossOrigin.anonymous

        return suspendCancellableCoroutine { cont ->
            // Set up an event listener to handle both successful and failed image loading.
            image.onload = EventHandler {
                // Image has loaded successfully.
                loaded = true
                cont.resume(image)
            }

            image.onerror = JsFunction<Unit, Unit> {
                // Image loading failed.
                cont.resumeWithException(Exception("Failed to load image: $src"))
            }

            // Set the image source to start loading it.
            image.src = src

            // When the coroutine is cancelled, remove the event listeners and stop loading the image.
            cont.invokeOnCancellation {
                image.onload = null
                image.onerror = null
                image.src = ""
            }
        }
    }
}