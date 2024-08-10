package net.perfectdreams.loritta.discordchatmessagerenderer

import com.microsoft.playwright.*
import com.microsoft.playwright.options.ScreenshotType
import com.microsoft.playwright.options.WaitUntilState
import mu.KotlinLogging
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import java.io.Closeable
import kotlin.time.measureTimedValue

class DiscordMessageRendererManager(private val messageHtmlRenderer: DiscordMessageRenderer, createBrowserType: Playwright.() -> (BrowserType)) : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val RECREATE_PAGE_AND_BROWSERCONTEXT_EVERY_X_PAGES = 100
    }

    private val playwright = Playwright.create()
    // Firefox has an issue in headless more where there is a white space at the bottom of the screenshot...
    // Chromium has an issue where screenshots >16384 are "corrupted"
    private val isHeadful = java.lang.Boolean.getBoolean("discordchatmessagerenderer.headful")
    // For now, we'll use Firefox instead of Chromium, because Chromium has some random timeout issues
    // And it seems that WebKit and Firefox is also faster to do the "set DOM and take screenshot" dance for some reason?
    // Firefox also seems to be way faster if you DON'T reuse pages
    // Firefox has some "hitches" when rendering messages in headful mode (things taking 2s+ to render)
    // I haven't reproduced that issue in headless mode yet
    private val browser = createBrowserType.invoke(playwright).launch(BrowserType.LaunchOptions().setHeadless(!isHeadful))
    private val deviceScale = 2.0
    // Only affects Chromium
    private val maxDimensionsOfImages = (16_384 / deviceScale).toInt()

    private var browserContext: BrowserContext? = null
    private var page: Page? = null
    private var currentlyRenderingMessage: SavedMessage? = null
    private var renders = 0

    private fun recreateBrowserContextAndPage() {
        val wasItAlreadyCreatedBefore = page != null && browserContext != null
        logger.info { "Recreating browser context and page... Current renders: $renders - Was it already created before? $wasItAlreadyCreatedBefore" }

        // We create a new browser context and a new page because Playwright leaks memory by keeping all request/responses stored in the "Connection.objects" HashMap
        // See: https://github.com/microsoft/playwright-java/issues/717
        // (However, in my experience it seems that Playwright does free the objects map when closing the page, but better be safe than sorry)
        page?.close()
        browserContext?.close()

        val newBrowserContext = browser.newContext(
            Browser.NewContextOptions()
                .setDeviceScaleFactor(deviceScale)
                .setAcceptDownloads(false)
                .setOffline(true)
                .setJavaScriptEnabled(false)
        )
        val newPage = newBrowserContext.newPage()

        newPage.onCrash {
            // The reason we don't attempt to withPage lock it, is because this seems to create a deadlock because the onCrash handler is triggered within the rendering call
            // Failsafe if a page crashes
            logger.error { "Whoops, page $it crashed while rendering ${currentlyRenderingMessage?.id}!" }
        }

        newPage.onLoad {
            logger.info { "Load event for ${currentlyRenderingMessage?.id}" }
        }

        newPage.onDOMContentLoaded {
            logger.info { "DOMContentLoaded event for ${currentlyRenderingMessage?.id}" }
        }

        newPage.onConsoleMessage {
            logger.info { "Page ${currentlyRenderingMessage?.id}: ${it.text()}" }
        }

        this.browserContext = newBrowserContext
        this.page = newPage
    }

    // The caller must make sure that this is called on only a single thread!
    fun renderMessage(
        savedMessage: SavedMessage,
        htmlContent: String
    ): ByteArray {
        this.currentlyRenderingMessage = savedMessage
        // Recreate the context and the page every 10k renders
        if (renders % RECREATE_PAGE_AND_BROWSERCONTEXT_EVERY_X_PAGES == 0)
            recreateBrowserContextAndPage()

        val browserContext = this.browserContext ?: error("BrowserContext is null!")
        val page = this.page ?: error("Page is null!")

        try {
            val messageId = savedMessage.id

            val timedValueScreenshot = measureTimedValue {
                logger.info { "Starting to render message $messageId! - Open pages: ${browserContext.pages().size} - ${browser.browserType().name()}" }

                // If this throws an error we are already fucked anyway, there isn't a good fallback here
                val timedValueLoadingPage = measureTimedValue {
                    logger.info { "Loading message preview page for $messageId!" }

                    page.setContent(
                        htmlContent,
                        // By using LOAD, we force the browser to wait everything to be loaded
                        Page.SetContentOptions().setWaitUntil(WaitUntilState.LOAD),
                    )
                }

                logger.info { "Took ${timedValueLoadingPage.duration} to load the message preview page for $messageId! - ${browser.browserType().name()}" }

                val timedValueTakingScreenshot = measureTimedValue {
                    logger.info { "Taking screenshot of message $messageId!" }

                    page.querySelector("#wrapper").screenshot(
                        ElementHandle.ScreenshotOptions()
                            .setType(ScreenshotType.PNG)
                    )
                }

                logger.info { "Took ${timedValueTakingScreenshot.duration} to generate a message screenshot for $messageId! - ${browser.browserType().name()}" }

                return@measureTimedValue timedValueTakingScreenshot.value
            }

            logger.info { "Took ${timedValueScreenshot.duration} to generate everything related to $messageId! - ${browser.browserType().name()}" }

            renders++

            return timedValueScreenshot.value
        } finally {
            currentlyRenderingMessage = null
        }
    }

    override fun close() {
        playwright.close()
    }
}