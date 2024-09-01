package net.perfectdreams.spicymorenitta.components.colorpicker

import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.Image
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Image.awaitLoad(url: String) {
    return kotlin.coroutines.suspendCoroutine { cont ->
        this.onload = {
            cont.resume(Unit)
        }
        this.onerror = { b: dynamic, s: String, i: Int, i1: Int, any: Any? ->
            cont.resumeWithException(Exception())
        }
        this.src = url
    }
}

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
        image.crossOrigin = "anonymous"

        return suspendCancellableCoroutine { cont ->
            // Set up an event listener to handle both successful and failed image loading.
            image.onload = {
                // Image has loaded successfully.
                loaded = true
                cont.resume(image)
            }

            image.onerror = { b: dynamic, s: String, i: Int, i1: Int, any: Any? ->
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