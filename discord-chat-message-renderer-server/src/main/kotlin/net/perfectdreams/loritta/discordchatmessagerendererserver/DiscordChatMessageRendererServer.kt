package net.perfectdreams.loritta.discordchatmessagerendererserver

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.discordchatmessagerenderer.DiscordMessageRenderer
import net.perfectdreams.loritta.discordchatmessagerenderer.DiscordMessageRendererManager
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import net.perfectdreams.loritta.discordchatmessagerendererserver.utils.DebugSender
import net.perfectdreams.loritta.discordchatmessagerendererserver.utils.SimpleImageInfo
import net.perfectdreams.loritta.discordchatmessagerendererserver.utils.readAllBytes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.net.URL
import java.time.ZoneId
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTimedValue


class DiscordChatMessageRendererServer {
    private val logger = KotlinLogging.logger {}
    private val http = HttpClient {}
    private val messageHtmlRenderer = DiscordMessageRenderer(
        ZoneId.of("America/Sao_Paulo"),
        setOf(
            "image/gif",
            "image/jpeg",
            "image/bmp",
            "image/png",
            "image/webp"
        )
    )
    private val rendererManagers = (0 until 8).map {
        DiscordMessageRendererManager(messageHtmlRenderer) { this.firefox() }
    }
    private val availableRenderers = CoroutineQueue<DiscordMessageRendererManager>(rendererManagers.size)
    private var successfulRenders = 0
    private var failedRenders = 0
    private val pendingRequests = AtomicInteger()

    fun start() {
        scheduleWithFixedDelay(DebugSender(), 0L, 15L, TimeUnit.SECONDS)

        logger.info { "Downloading fonts..." }

        val fontStylesheetAsTextReplacedFontsToBase64 = runBlocking {
            val fontStylesheetResponse = http.get("https://fonts.googleapis.com/css2?family=Lato:ital,wght@0,100;0,300;0,400;0,700;0,900;1,100;1,300;1,400;1,700;1,900&display=swap&family=Pacifico&display=swap&family=JetBrains+Mono:ital,wght@0,100..800;1,100..800&display=swap") {
                // Google Fonts serves different stylesheets depending on the browser
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0")
            }
            val fontStylesheetAsText = fontStylesheetResponse.bodyAsText()
            val fontStylesheetAsTextReplacedFontsToBase64 = Regex("url\\((.+?)\\)")
                .replace(fontStylesheetAsText) {
                    val fontData = runBlocking { http.get(it.groupValues[1]).readBytes() }
                    "url(data:application/octet-stream;base64,${Base64.getEncoder().encodeToString(fontData)})"
                }
            fontStylesheetAsTextReplacedFontsToBase64
        }
        logger.info { "Successfully downloaded all fonts!" }

        logger.info { "Using ${rendererManagers.size} renderers" }
        for (rendererManager in rendererManagers) {
            runBlocking { availableRenderers.send(rendererManager) }
        }

        val http = embeddedServer(CIO, port = 8080) {
            routing {
                // Dumps all currently running coroutines
                get("/coroutines") {
                    val os = ByteArrayOutputStream()
                    val ps = PrintStream(os)
                    DebugProbes.dumpCoroutines(ps)
                    call.respondText(os.toString(Charsets.UTF_8))
                }

                post("/generate-message") {
                    val body = call.receiveText()

                    val savedMessage = Json.decodeFromString<SavedMessage>(body)
                    logger.info { "Received request to render ${savedMessage.id}! Available renderers: ${availableRenderers.getCount()}/${rendererManagers.size}; Pending requests: $pendingRequests" }

                    // We will attempt to download all required images before rendering, this way, we don't need to wait all images to individually download on the browser itself
                    // The less time we spend locking a renderer, the better!
                    // The image data will be embedded in the generated HTML
                    val savedMessageHtmlContentReplacedImagesTimedValue = measureTimedValue {
                        // We don't need to mimick Twemoji because Firefox for Linux already uses Twemoji (yay)
                        // We also don't need to worry about this being on the heap, the JVM will optimize this out for us (double yay)
                        val savedMessageHtmlContent = messageHtmlRenderer.renderMessage(
                            savedMessage,
                            null,
                            fontStylesheetAsTextReplacedFontsToBase64
                        )

                        // Parse with Jsoup and replace all img tags with a base64 representation of the image
                        val jobs = mutableListOf<Job>()
                        val document = Jsoup.parse(savedMessageHtmlContent)
                        // We disable pretty print to avoid any unnecessary whitespace causing design issues
                        document.outputSettings(Document.OutputSettings().prettyPrint(false))
                        for (imgElement in document.select("img")) {
                            jobs.add(
                                GlobalScope.async(Dispatchers.IO) {
                                    logger.info { "Downloading file ${imgElement.attr("src")} for message ${savedMessage.id}" }
                                    val byteArray = downloadFile(imgElement.attr("src"))
                                    // We don't really mind keeping the original image URL if it fails, the browser window is running on offline mode anyway
                                    if (byteArray != null) {
                                        val simpleImageInfo = try {
                                            SimpleImageInfo(byteArray)
                                        } catch (e: IOException) {
                                            null
                                        }

                                        val mimeType = simpleImageInfo?.mimeType

                                        if (mimeType != null) {
                                            logger.info { "Successfully downloaded file ${imgElement.attr("src")} for message ${savedMessage.id}! Bytes: ${byteArray.size} bytes (${(String.format("%.2f", byteArray.size / (1024.0 * 1024.0)))} MiB); MIME type: $mimeType" }

                                            imgElement.attr(
                                                "src",
                                                "data:$mimeType;base64,${Base64.getEncoder().encodeToString(byteArray)}"
                                            )
                                        } else {
                                            logger.warn { "Failed to check the MIME type of the file ${imgElement.attr("src")} for message ${savedMessage.id}!" }
                                        }
                                    } else {
                                        logger.warn { "Failed to download file ${imgElement.attr("src")} for message ${savedMessage.id}!" }
                                    }
                                }
                            )
                        }

                        // Wait all URLs to be downloaded
                        jobs.joinAll()

                        document.html()
                    }
                    val savedMessageHtmlContentReplacedImages = savedMessageHtmlContentReplacedImagesTimedValue.value
                    logger.info { "Took ${savedMessageHtmlContentReplacedImagesTimedValue.duration} to render the message to HTML and download all images for message ${savedMessage.id}! Available renderers: ${availableRenderers.getCount()}/${rendererManagers.size}; Pending requests: $pendingRequests" }

                    logger.info { "Attempting to get a available renderer for message ${savedMessage.id}... Available renderers: ${availableRenderers.getCount()}/${rendererManagers.size}; Pending requests: $pendingRequests" }

                    try {
                        pendingRequests.incrementAndGet()
                        val rendererManager = measureTimedValue {
                            availableRenderers.receive()
                        }.also {
                            logger.info { "Took ${it.duration} to get an available renderer for ${savedMessage.id}! Available renderers: ${availableRenderers.getCount()}/${rendererManagers.size}; Pending requests: $pendingRequests" }
                        }.value

                        val image = try {
                            val image = rendererManager.renderMessage(savedMessage, savedMessageHtmlContentReplacedImages)

                            successfulRenders++
                            logger.info { "Successfully rendered message ${savedMessage.id}! Successful renders: $successfulRenders; Failed renders: $failedRenders" }
                            image
                        } catch (e: Exception) {
                            logger.warn(e) { "Something went wrong while trying to render message ${savedMessage.id}! Request Body: $body" }
                            call.respondText(
                                e.stackTraceToString(),
                                status = HttpStatusCode.InternalServerError
                            )
                            failedRenders++
                            null
                        } finally {
                            logger.info { "Putting $rendererManager back into the available renderers queue" }
                            availableRenderers.send(rendererManager)
                        }

                        if (image != null) {
                            call.respondBytes(
                                image,
                                ContentType.Image.PNG
                            )
                        } else {
                            call.respondText("", status = HttpStatusCode.InternalServerError)
                        }
                    } finally {
                        pendingRequests.decrementAndGet()
                    }
                }
            }
        }
        http.start(true)
    }

    /**
     * Downloads an file and returns it as a ByteArray, additional checks are made and can be customized to avoid
     * downloading unsafe/big files that crash the application.
     *
     * @param url                            the image URL
     * @param connectTimeout                 the connection timeout
     * @param readTimeout                    the read timeout
     * @param maxSize                        the image's maximum size
     * @param overrideTimeoutsForSafeDomains if the URL is a safe domain, ignore timeouts
     * @param maxWidth                       the image's max width
     * @param maxHeight                      the image's max height
     * @param bypassSafety                   if the safety checks should be bypassed
     *
     * @return the image as a BufferedImage or null, if the image is considered unsafe
     */
    @JvmOverloads
    fun downloadFile(url: String, connectTimeout: Int = 10, readTimeout: Int = 60, maxSize: Int = 8_388_608 /* 8mib */): ByteArray? {
        try {
            val connection = URL(url).openConnection()

            val contentLength = connection.getHeaderFieldInt("Content-Length", 0)

            if (contentLength > maxSize) {
                logger.warn { "Image $url exceeds the maximum allowed Content-Length! ${connection.getHeaderFieldInt("Content-Length", 0)} > $maxSize"}
                return null
            }

            if (connectTimeout != -1) {
                connection.connectTimeout = connectTimeout
            }

            if (readTimeout != -1) {
                connection.readTimeout = readTimeout
            }

            logger.debug { "Reading image $url; connectTimeout = $connectTimeout; readTimeout = $readTimeout; maxSize = $maxSize bytes" }

            val imageBytes = if (contentLength != 0) {
                // If the Content-Length is known (example: images on Discord's CDN do have Content-Length on the response header)
                // we can allocate the array with exactly the same size that the Content-Length provides, this way we avoid a lot of unnecessary Arrays.copyOf!
                // Of course, this could be abused to allocate a gigantic array that causes Loritta to crash, but if the Content-Length is present, Loritta checks the size
                // before trying to download it, so no worries :)
                connection.inputStream.readAllBytes(maxSize, contentLength)
            } else
                connection.inputStream.readAllBytes(maxSize)

            logger.debug { "File $url was successfully downloaded!"}

            return imageBytes
        } catch (e: Exception) {
        }

        return null
    }

    fun scheduleWithFixedDelay(task: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) {
        Executors.newScheduledThreadPool(1, ThreadFactoryBuilder().setNameFormat("${task::class.simpleName} Executor Thread-%d").build()).scheduleWithFixedDelay(task, initialDelay, delay, unit)
    }
}