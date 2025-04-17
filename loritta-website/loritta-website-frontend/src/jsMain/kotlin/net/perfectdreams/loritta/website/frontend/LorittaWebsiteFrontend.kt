package net.perfectdreams.loritta.website.frontend

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.img
import kotlinx.html.span
import kotlinx.html.style
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import net.perfectdreams.bliss.BlissManager
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.dokyo.elements.HomeElements
import net.perfectdreams.harmony.web.addClass
import net.perfectdreams.harmony.web.clear
import net.perfectdreams.harmony.web.hasClass
import net.perfectdreams.harmony.web.removeClass
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.loritta.website.frontend.components.SidebarHamburgerButtonComponentMounter
import net.perfectdreams.loritta.website.frontend.utils.LinkPreloaderManager
import net.perfectdreams.loritta.website.frontend.utils.extensions.get
import net.perfectdreams.loritta.website.frontend.utils.extensions.onClick
import net.perfectdreams.loritta.website.frontend.utils.extensions.select
import net.perfectdreams.loritta.website.frontend.utils.extensions.selectAll
import net.perfectdreams.loritta.website.frontend.views.ViewManager
import web.dom.Element
import web.dom.ParentNode
import web.dom.document
import web.events.Event
import web.events.EventHandler
import web.events.addEventListener
import web.history.history
import web.html.HTMLDivElement
import web.html.HTMLElement
import web.window.window
import kotlin.js.Json

class LorittaWebsiteFrontend {
    companion object {
        val http = HttpClient {
            expectSuccess = false
        }

        // Need to declare the logger name because, if it is not declared, it will show up as "Function" in the browser console
        private val logger = KotlinLogging.logger("Showtime")
    }

    val linkPreloaderManager = LinkPreloaderManager(this)
    val viewManager = ViewManager(this)
    val pageSpecificTasks = mutableListOf<Job>()
    val globalTasks = mutableListOf<Job>()
    val mounters = listOf(
        SidebarHamburgerButtonComponentMounter()
    )

    var currentPath = window.location.pathname

    fun start() {
        KotlinLoggingConfiguration.LOG_LEVEL = KotlinLoggingLevel.DEBUG
        logger.info { "HELLO FROM KOTLIN ${KotlinVersion.CURRENT.toString()}! :3" }
        logger.info { "Howdy, my name is Loritta!" }
        logger.info { "I want to make the world a better place... making people happier and helping other people... changing their lives..."  }
        logger.info { "I hope I succeed..." }
        logger.info { "Do you like peeking under the hood? https://github.com/LorittaBot/Loritta" }
        // Chromium easter egg
        console.log("%c       ", "font-size: 64px; background: url(https://stuff.loritta.website/loritta-zz-heathecliff.png) no-repeat; background-size: 64px 64px;")

        document.addEventListener(Event.DOM_CONTENT_LOADED, {
            logger.debug { "DOM loaded" }

            // If the pathname doesn't exist... how that happens???
            val pathName = document.location?.pathname ?: return@addEventListener

            // Now we are going to setup all the pages! :3
            launchGlobal {
                startFakeProgressIndicator()
                viewManager.preparePreLoad(pathName)
                viewManager.preparingMutex.withLock {
                    viewManager.switchPreparingToActiveView()
                }
                stopFakeProgressIndicator()
                // TODO: This needs to be here due to the viewManager cancelling our tasks
                BlissManager.setup(this@LorittaWebsiteFrontend, document.body)
                linkPreloaderManager.setupLinkPreloader()
                addNavbarOptions()
                checkAndFixNavbarOverflownEntries()
                loadLoggedInUser()
                mountComponents()
            }

            // Handles back button
            // Most of the solutions found in the internet are kinda wonky or requires a lot of hacky workarounds
            //
            // So we are coding our *own* hacky workaround! :3
            //
            // This requires the usage of the pushState(...) function in this class, that function will update the currentPath when needed
            window.onpopstate = EventHandler {
                if (currentPath == window.location.pathname) {
                    logger.debug { "History changed but seems to be a hash change (maybe?), ignoring onpopstate event..." }
                } else {
                    logger.debug { "History changed! Trying to load the new page... New pathname is ${window.location.pathname}" }
                    // Just refresh the page and hope for the best
                    document.location.reload()
                }
            }
        })
    }

    fun pushState(pageUrl: String) {
        history.pushState(null, "", pageUrl)
        currentPath = window.location.pathname

        logger.debug { "Updated current path to $currentPath" }
    }

    fun startFakeProgressIndicator() {
        logger.debug { "Starting Fake Progress" }

        // resetFakeProgressIndicator()

        val progressIndicator = HomeElements.progressIndicator.get()
        progressIndicator.addClass("visible")

        // Reset to 0%
        progressIndicator.style.transition = "none"
        progressIndicator.style.width = "0%"
        // Trigger a reflow
        console.log("Reflow: ${progressIndicator.offsetWidth}")
        // Reset transition
        progressIndicator.style.transition = ""
        // Trigger a reflow again
        console.log("Reflow2: ${progressIndicator.offsetWidth}")

        progressIndicator.style.width = "99%" // fake fill just so the user "thinks" that something is happening :P
    }

    fun stopFakeProgressIndicator() {
        logger.debug { "Stopping Fake Progress" }
        val progressIndicator = HomeElements.progressIndicator.get()

        // Change progress bar to 100%
        progressIndicator.style.width = "100%"

        // remove visible class and reset progress bar
        progressIndicator.removeClass("visible")

        // resetFakeProgressIndicator()
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        val job = GlobalScope.launch(block = block)
        pageSpecificTasks.add(job)
        job.invokeOnCompletion {
            pageSpecificTasks.remove(job)
        }
        return job
    }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        val job = GlobalScope.async(block = block)
        pageSpecificTasks.add(job)
        job.invokeOnCompletion {
            pageSpecificTasks.remove(job)
        }
        return job
    }

    fun launchGlobal(block: suspend CoroutineScope.() -> Unit): Job {
        val job = GlobalScope.launch(block = block)
        globalTasks.add(job)
        job.invokeOnCompletion {
            globalTasks.remove(job)
        }
        return job
    }

    fun addNavbarOptions() {
        val navbar = document.select<Element?>("#navigation-bar")

        if (navbar != null) {
            logger.debug { "Setting up theme changer button..." }

            val themeChangerButton = document.select<Element?>("#theme-changer-button")

            themeChangerButton?.onClick {
                val body = document.body!!

                if (body.hasClass("dark")) {
                    WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteTheme.DEFAULT)
                } else {
                    WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteTheme.DARK_THEME)
                }
            }

            logger.debug { "Setting up hamburger button events..." }

            val hamburgerButton = document.select<Element?>("#hamburger-menu-button")

            hamburgerButton?.onClick {
                logger.debug { "Clicked on the hamburger button!" }
                if (navbar.hasClass("expanded")) {
                    navbar.removeClass("expanded")
                    document.body!!.style.overflowY = ""
                } else {
                    navbar.addClass("expanded")
                    document.body!!.style.overflowY = "hidden" // Para remover as scrollbars e apenas deixar as scrollbars da navbar
                }
            }

            if (hamburgerButton != null) {
                logger.debug { "Setting up resize handler..." }

                window.addEventListener(Event.RESIZE, {
                    checkAndFixNavbarOverflownEntries()
                })

                checkAndFixNavbarOverflownEntries()

                logger.debug { "Resize handler successfully created!" }
            }
        }
    }

    /**
     * Checks and, if needed, shows or hides the hamburger button if any of the entries are overflowing to the next line.
     *
     * This should be called every time when the navbar is updated with new entries, or if a entry changes size. (example: [updateLoggedInUser])
     */
    fun checkAndFixNavbarOverflownEntries() {
        val leftSidebar = document.select<HTMLDivElement>(".left-side-entries")
        val hamburgerButton = document.select<HTMLDivElement>("#hamburger-menu-button")

        val isOverflowing = leftSidebar.selectAll<HTMLDivElement>(".entry").any { it.offsetTop != 0 }
        if (isOverflowing) {
            logger.debug { "Navbar entries are overflowing, let's unhide the hamburger button!" }

            hamburgerButton.style.display = "block"
        } else {
            logger.debug { "Navbar entries are not overflowing, let's hide the hamburger button!" }

            hamburgerButton.style.display = "none"
        }
    }

    /**
     * Requests the logged in user via "/api/v1/users/@me" and calls [updateLoggedInUser] if the request succeeds.
     */
    suspend fun loadLoggedInUser() {
        val httpResponse = http.get("${window.location.origin}/api/v1/users/@me")
        val payload = httpResponse.bodyAsText()

        println(payload)

        if (httpResponse.status != HttpStatusCode.OK) {
            val jsonPayload = JSON.parse<Json>(payload)
            logger.warn { "Get User Request failed - ${jsonPayload["code"]}" }
        } else {
            val userIdentification = kotlinx.serialization.json.Json.decodeFromString(UserIdentification.serializer(), payload)
            logger.debug { "Get User Request success! - ${userIdentification.username} (${userIdentification.id})" }
            updateLoggedInUser(userIdentification)
        }
    }

    /**
     * Updates the current user identification with the [newUser]
     *
     * This updates the login button (switches to the user login) and sets the [userIdentification] to the [newUser]
     *
     * @param newUser the logged in user identification
     */
    fun updateLoggedInUser(newUser: UserIdentification) {
        // userIdentification = newUser
        logger.debug { "New user is $newUser" }
        val loginButton = document.select<Element?>("#login-button")
        if (loginButton != null) {
            val cloned = loginButton.cloneNode(true) // Nós precisamos remover os event listeners (onClick)
            cloned as Element

            loginButton.replaceWith(cloned)
            cloned.clear()
            // cloned.setAttribute("href", "/$websiteLocaleId/dashboard")
            cloned.setAttribute("href", "/dashboard")

            cloned.unsafeCast<org.w3c.dom.HTMLDivElement>().append {
                val avatarUrl = newUser.effectiveAvatarUrl

                img(src = avatarUrl) {
                    style = """    font-size: 0px;
    line-height: 0px;
    width: 40px;
    height: 40px;
    position: absolute;
    top: 3px;
    border-radius: 100%;"""
                }

                div {
                    // Dummy image que "ocupa" o espaço (já que a imagem usa position: absolute)
                    style = """    font-size: 0px;
    line-height: 0px;
    width: 40px;
    visibility: hidden;
    height: 0px;
    display: inline-block;"""
                }

                span {
                    style = "padding-left: 4px;"

                    +newUser.username
                }
            }
        }

        // Update the navbar entries because the name + avatar may cause the navbar to overflow
        checkAndFixNavbarOverflownEntries()
    }

    fun mountComponents() {
        for (element in document.selectAll<HTMLElement>("[harmony-component-mounter]")) {
            val componentId = element.getAttribute("harmony-component-mounter")
            val mounter = mounters.firstOrNull { it.id == componentId }

            if (mounter != null) {
                logger.info { "Mounting component $componentId with mounter $mounter..." }
                mounter.mount(element)
            } else {
                logger.debug { "Unknown component mounter! $componentId" }
            }
        }
    }

    fun mountComponents(element: ParentNode) {
        for (element in element.selectAll<HTMLElement>("[harmony-component-mounter]")) {
            val componentId = element.getAttribute("harmony-component-mounter")
            val mounter = mounters.firstOrNull { it.id == componentId }

            if (mounter != null) {
                logger.info { "Mounting component $componentId with mounter $mounter..." }
                mounter.mount(element)
            } else {
                logger.debug { "Unknown component mounter! $componentId" }
            }
        }
    }
}