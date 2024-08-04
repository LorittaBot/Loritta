package net.perfectdreams.loritta.discordchatmessagerenderer

import com.microsoft.playwright.*
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.ScreenshotType
import com.microsoft.playwright.options.WaitUntilState
import mu.KotlinLogging
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import java.io.Closeable
import java.io.File
import kotlin.time.measureTimedValue

class DiscordMessageRendererManager(private val messageHtmlRenderer: DiscordMessageRenderer) : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val playwright = Playwright.create()
    // Firefox has an issue in headless more where there is a white space at the bottom of the screenshot...
    // Chromium has an issue where screenshots >16384 are "corrupted"
    private val isHeadful = java.lang.Boolean.getBoolean("discordchatmessagerenderer.headful")
    private val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(!isHeadful))
    private val deviceScale = 1.0
    private val maxDimensionsOfImages = (16_384 / deviceScale).toInt()

    // The caller must make sure that this is called on only a single thread!
    fun renderMessage(
        savedMessage: SavedMessage,
        qrCodeImageAsByteArray: ByteArray?
    ): ByteArray {
        val messageId = savedMessage.id

        // We always create a new browser context and a new page because Playwright leaks memory by keeping all request/responses stored in the "Connection.objects" HashMap
        // See: https://github.com/microsoft/playwright-java/issues/717
        // (However, in my experience it seems that Playwright does free the objects map when closing the page, but better be safe than sorry)
        val browserContext = browser.newContext(Browser.NewContextOptions().setDeviceScaleFactor(deviceScale))
        val page = browserContext.newPage()
        page.onCrash {
            // The reason we don't attempt to withPage lock it, is because this seems to create a deadlock because the onCrash handler is triggered within the rendering call
            // Failsafe if a page crashes
            logger.error { "Whoops, page $it crashed!" }
        }

        page.onLoad {
            logger.info { "Load event for $messageId" }
        }

        page.onDOMContentLoaded {
            logger.info { "DOMContentLoaded event for $messageId" }
        }

        page.onConsoleMessage {
            logger.info { "Page $messageId: ${it.text()}" }
        }

        var writeImageForDebug = false

        try {
            val timedValueScreenshot = measureTimedValue {
                logger.info { "Starting to render message $messageId! - Open pages: ${browserContext.pages().size}" }

                // If this throws an error we are already fucked anyway, there isn't a good fallback here
                val timedValueDOMContentLoadedPage = measureTimedValue {
                    logger.info { "Loading message preview page for $messageId!" }

                    page.setContent(
                        messageHtmlRenderer.renderMessage(savedMessage, null),
                        // By using DOMCONTENTLOADED, we force the browser to wait for DOMContentLoaded, we DON'T want to wait for assets here
                        Page.SetContentOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED),
                    )
                }

                logger.info { "Took ${timedValueDOMContentLoadedPage.duration} to DOMContentLoaded the message preview page for $messageId!" }

                val timedValueLoadPage = measureTimedValue {
                    logger.info { "Waiting for all assets of the message preview page of $messageId to be loaded!" }

                    // We will wait at MOST 2.5s
                    try {
                        page.waitForLoadState(LoadState.LOAD, Page.WaitForLoadStateOptions().setTimeout(2_500.0))
                    } catch (e: TimeoutError) {
                        logger.warn(e) { "Took too long to wait for assets of the message preview page for $messageId, maybe an image wasn't completely loaded? - We are going to skip the load and attempt to render the message anyway..." }
                        writeImageForDebug = true
                    }
                }

                logger.info { "Took ${timedValueLoadPage.duration} to wait for all the message preview page assets of $messageId!" }

                // STOP! Wait a minute!
                // Stop all asset loading to avoid any non-loaded assets causing things to shift when taking screenshots
                // While we could use "window.stop()", it doesn't work that well for images for some reason, sometimes Playwright just keeps loading the image
                // So, to handle this, we will disable all images that aren't fully loaded
                page.evaluate("""
                    // Get all image elements on the page
                    const images = document.querySelectorAll("img");
                
                    // Loop through each image element
                    images.forEach(function(image) {
                        // Check if the image is completely loaded
                        var fullyLoaded = image.complete && image.naturalWidth !== 0
                        if (!fullyLoaded) {
                            console.log("Replacing image " + image.src + " with a broken image");
                            // Replace the src attribute with an invalid image
                            image.setAttribute("src", "data:image/jpeg;charset=utf-8;base64,");
                        }
                    });
                """.trimIndent())

                val timedValueTakingScreenshot = measureTimedValue {
                    logger.info { "Taking screenshot of message $messageId!" }

                    page.querySelector("#wrapper").screenshot(
                        ElementHandle.ScreenshotOptions()
                            .setType(ScreenshotType.PNG)
                    )
                }

                logger.info { "Took ${timedValueTakingScreenshot.duration} to generate a message screenshot for $messageId!" }

                if (writeImageForDebug) {
                    // If a timeout happens, let's write the image to the disk to see what is happening
                    logger.info { "Writing screenshot of message $messageId to disk for debugging purposes..." }
                    File("/debug/$messageId.png").writeBytes(timedValueTakingScreenshot.value)
                }

                return@measureTimedValue timedValueTakingScreenshot.value
            }

            logger.info { "Took ${timedValueScreenshot.duration} to generate everything related to $messageId!" }

            return timedValueScreenshot.value
        } finally {
            page.close()
            browserContext.close()
        }
    }

    override fun close() {
        playwright.close()
    }
}