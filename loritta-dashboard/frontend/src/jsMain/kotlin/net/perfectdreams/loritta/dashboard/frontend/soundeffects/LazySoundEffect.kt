package net.perfectdreams.loritta.dashboard.frontend.soundeffects

import io.ktor.client.request.*
import io.ktor.client.statement.*
import js.typedarrays.Uint8Array
import js.typedarrays.toUint8Array
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import web.blob.Blob
import web.blob.BlobPropertyBag
import web.events.EventHandler
import web.html.Audio
import web.html.play
import web.url.URL

class LazySoundEffect(val m: LorittaDashboardFrontend, val url: String) {
    private var objUrl: String? = null
    private val mutex = Mutex()

    fun play(
        volume: Double,
        playbackRate: Double = 1.0,
        onEnd: () -> (Unit) = {}
    ) {
        GlobalScope.launch {
            mutex.lock()

            val cachedObjUrl = objUrl

            // Already cached, just unlock and play!
            if (cachedObjUrl != null) {
                mutex.unlock()
                println("Playing cached objUrl for $url...")
                Audio(cachedObjUrl).apply {
                    this.volume = volume
                    this.playbackRate = playbackRate
                    this.asDynamic().preservesPitch = false
                    this.onended = EventHandler {
                        onEnd.invoke()
                    }
                }.play()
                return@launch
            } else {
                println("Cached objUrl is not present for $url, downloading...")

                // Oof, not cached yet!
                val errorAsByteArray = m.http.get(url)
                    .bodyAsBytes()

                val blob = Blob(arrayOf(errorAsByteArray.toUint8Array()), BlobPropertyBag(type = "audio/ogg"))
                val objUrl = URL.createObjectURL(blob)
                println("Successfully cached objUrl for $url!")
                this@LazySoundEffect.objUrl = objUrl
                mutex.unlock()

                // Play!
                Audio(objUrl).apply {
                    this.volume = volume
                    this.playbackRate = playbackRate
                    this.asDynamic().preservesPitch = false
                    this.onended = EventHandler {
                        onEnd.invoke()
                    }
                }.play()
            }
        }
    }
}