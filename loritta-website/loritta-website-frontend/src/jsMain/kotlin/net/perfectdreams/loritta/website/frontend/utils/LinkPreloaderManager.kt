package net.perfectdreams.loritta.website.frontend.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.sync.withLock
import kotlinx.dom.removeClass
import net.perfectdreams.loritta.website.frontend.LorittaWebsiteFrontend
import net.perfectdreams.loritta.website.frontend.utils.extensions.onClick
import net.perfectdreams.loritta.website.frontend.utils.extensions.select
import net.perfectdreams.loritta.website.frontend.utils.extensions.selectAll
import org.w3c.dom.Element
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTitleElement
import org.w3c.dom.asList
import org.w3c.dom.url.URL

class LinkPreloaderManager(val showtime: LorittaWebsiteFrontend) {
    companion object {
        private const val PRELOAD_LINK_ATTRIBUTE = "data-preload-link"
        private const val PRELOAD_LINK_ACTIVE_ATTRIBUTE = "data-preload-link-activated"
        private const val PRELOAD_PERSIST_ATTRIBUTE = "data-preload-persist"
        private const val PRELOAD_KEEP_SCROLL = "data-preload-keep-scroll"
    }

    fun setupLinkPreloader() {
        println("Setting up link preloader")
        document.querySelectorAll("a[$PRELOAD_LINK_ATTRIBUTE=\"true\"]:not([$PRELOAD_LINK_ACTIVE_ATTRIBUTE=\"true\"])")
            .asList().forEach {
                if (it is Element) {
                    setupLinkPreloader(it)
                }
            }
    }

    fun setupLinkPreloader(element: Element) {
        val location = document.location ?: return // I wonder when location can be null...

        var pageUrl = element.getAttribute("href")!!

        if (pageUrl.startsWith("http")) {
            if (!pageUrl.startsWith(window.location.origin)) // Mesmo que seja no mesmo domínio, existe as políticas de CORS
                return

            val urlLocation = URL(pageUrl)
            pageUrl = urlLocation.pathname
        }

        element.setAttribute(PRELOAD_LINK_ACTIVE_ATTRIBUTE, "true")

        element.onClick { it ->
            println("Clicked!!! $pageUrl")
            val body = document.body ?: return@onClick // If we don't have a body, what we will switch to?

            if (it.asDynamic().ctrlKey as Boolean || it.asDynamic().metaKey as Boolean || it.asDynamic().shiftKey as Boolean)
                return@onClick

            it.preventDefault()

            // Same page, no need to redirect
            /* if (pageUrl == window.location.pathname)
                return@onClick */

            println("Going to load $pageUrl")

            showtime.launchGlobal {
                // Close navbar if it is open, this avoids the user clicking on something and wondering "but where is the new content?"
                val navbar = document.select<Element?>("#navigation-bar")
                navbar?.removeClass("expanded")
                document.body!!.style.overflowY = ""

                // Start progress indicator
                showtime.startFakeProgressIndicator()

                // First prepare the page preload, this is useful so the page can preload data (do queries, as an example) before we totally switch
                val preparePreLoadJob = showtime.async { showtime.viewManager.preparePreLoad(pageUrl) }

                // Switch page
                // We need to rebuild the URL from scratch, if we just do a "pageUrl" request, it won't add the port for some reason
                val content = LorittaWebsiteFrontend.http.get(location.protocol + "//" + location.host + pageUrl) {
                    header("Link-Preload", true)
                }

                println("Content:")
                println(content)

                // Find all persistent elements in the old page
                val persistentElements = document.selectAll<HTMLElement>("[$PRELOAD_PERSIST_ATTRIBUTE=\"true\"]")

                // Find all keep scroll elements in the old page
                // The values needs to be stored before switching, if not the value will be "0"
                val keepScrollElements = document.selectAll<HTMLElement>("[$PRELOAD_KEEP_SCROLL=\"true\"]")
                    .map { it.id to it.scrollTop }

                // We need to create a dummy element to append our inner HTML
                val dummyElement = document.createElement("html")
                dummyElement.innerHTML = content.bodyAsText()

                // Await until preLoad is done to continue
                preparePreLoadJob.await()

                // We are now going to reuse the mutex in the pageManager
                // The reason we reuse it is to avoid calling "onLoad()" before "onPreLoad()" is finished! :3
                showtime.viewManager.preparingMutex.withLock {
                    // Replace the page's title
                    dummyElement.select<HTMLTitleElement?>("title")?.let {
                        println("Page title is ${it.outerHTML}")
                        document.select<HTMLTitleElement?>("title")?.outerHTML = it.outerHTML
                    }

                    // Switch the page's content (not all, just the body xoxo)
                    body.innerHTML = dummyElement.select<HTMLBodyElement?>("body")?.innerHTML ?: "Broken!"

                    // Push the current page to history
                    showtime.pushState(pageUrl)

                    // Now copy the persistent elements
                    persistentElements.forEach {
                        val newElementThatShouldBeReplaced = document.getElementById(it.id) as HTMLElement?
                        println("Persisted element $it is going to persist thru $newElementThatShouldBeReplaced")
                        newElementThatShouldBeReplaced?.replaceWith(it)
                    }

                    // Also copy the keep scroll of the affected elements
                    keepScrollElements.forEach {
                        val newElementThatShouldChangeTheScroll = document.getElementById(it.first) as HTMLElement?
                        println("Keeping element $it scroll, value: ${it.second}")
                        newElementThatShouldChangeTheScroll?.scrollTop = it.second
                    }

                    // Now that all of the elements are loaded, we can switch from the preparing to the active view
                    showtime.viewManager.switchPreparingToActiveView()

                    // Reset scroll to the top of the page
                    window.scrollTo(0.0, 0.0)

                    // remove visible class and set progress bar to 100%
                    showtime.stopFakeProgressIndicator()

                    // Setup link preloader again
                    setupLinkPreloader()

                    // Also setup ads
                    NitroPayUtils.renderAds()
                }
            }
        }
    }
}