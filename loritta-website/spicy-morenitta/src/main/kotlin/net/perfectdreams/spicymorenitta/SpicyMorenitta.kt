package net.perfectdreams.spicymorenitta

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.img
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.DashboardRoute
import net.perfectdreams.spicymorenitta.routes.FanArtsRoute
import net.perfectdreams.spicymorenitta.routes.HomeRoute
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.GeneralDashboardRoute
import net.perfectdreams.spicymorenitta.trunfo.TrunfoGame
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.locale.BaseLocale
import net.perfectdreams.spicymorenitta.views.BaseView
import net.perfectdreams.spicymorenitta.ws.PingCommand
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.WebSocket
import org.w3c.dom.asList
import kotlin.browser.document
import kotlin.browser.window
import kotlin.collections.set
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.hasClass
import kotlin.dom.removeClass
import kotlin.js.Date
import kotlin.js.Json

var switchPageStart = 0.0
val pageCache = mutableMapOf<String, String>()
var ignoreCacheRequests = false
var navbarIsSetup = false
external var isOldWebsite: Boolean

val http = HttpClient(Js) {
	expectSuccess = false // Não dar erro ao receber status codes 400-500
}

lateinit var locale: BaseLocale

class SpicyMorenitta : Logging {
	companion object {
		const val KEEP_ALIVE_DELAY = 10_000L
		const val CACHE_ON_HOVER_DELAY = 125L // milliseconds
		lateinit var INSTANCE: SpicyMorenitta
	}

	val pageLoadLock = Mutex()
	val wsCommands = mutableListOf(
			PingCommand()
	)
	val routes = mutableListOf(
			HomeRoute(),
			FanArtsRoute(this),
			DashboardRoute(this),
			GeneralDashboardRoute(this),
			UpdateNavbarSizePostRender("/support"),
			UpdateNavbarSizePostRender("/blog"),
			UpdateNavbarSizePostRender("/extended")
	)
	val validWebsiteLocaleIds = mutableListOf(
			"br",
			"us",
			"es"
	)
	val websiteLocaleIdToLocaleId = mutableMapOf(
			"br" to "default",
			"us" to "us-us",
			"es" to "es-es"
	)

	val view: BaseView? = null
	lateinit var socket: WebSocket
	val localeId: String
		get() {
			return websiteLocaleIdToLocaleId[websiteLocaleId] ?: "default"
		}

	val websiteLocaleId: String
		get() {
			val localeIdFromPath = WebsiteUtils.getWebsiteLocaleIdViaPath()
			return if (localeIdFromPath in validWebsiteLocaleIds)
				localeIdFromPath
			else
				"us"
		}

	var userIdentification: UserIdentification? = null
	val pageSpecificTasks = mutableListOf<Job>()

	@UseExperimental(ImplicitReflectionSerializer::class)
	fun start() {
		INSTANCE = this

		info("SpicyMorenitta :3")
		info("Howdy, my name is Loritta!")
		info("I want to make the world a better place... making people happier and helping other people... changing their lives...")
		info("I hope I succeed...")
		// Chromium easter egg
		console.log("%c       ", "font-size: 64px; background: url(https://loritta.website/assets/img/fanarts/l8.png) no-repeat; background-size: 64px 64px;")

		if (window.location.pathname == "/auth") { // Nós não precisamos processar o resto do código apenas para verificar o popup de auth
			AuthUtils.handlePopup()
			return
		}

		if (true) {
			TrunfoGame.start()
			return
		}

		// From old website
		val darkThemeCookie = CookiesUtils.readCookie("darkTheme")
		if (darkThemeCookie?.toBoolean() == true)
			WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteThemeUtils.WebsiteTheme.DARK_THEME, true)

		if (false) {
			// From new website
			val userThemeCookie = CookiesUtils.readCookie("userTheme")
			if (userThemeCookie != null)
				WebsiteThemeUtils.changeWebsiteThemeTo(
						try {
							WebsiteThemeUtils.WebsiteTheme.valueOf(userThemeCookie)
						} catch (e: IllegalArgumentException) {
							WebsiteThemeUtils.WebsiteTheme.DEFAULT
						},
						true
				)
		}

		debug("Is using http? ${window.location.protocol == "http:"}")
		val url: String = if (window.location.protocol == "http:")
			"ws://${window.location.origin.split("//").last()}/ws"
		else
			"wss://${window.location.origin.split("//").last()}/ws"

		debug("Using protocol ${window.location.protocol} - WebSocket URL is $url")

		socket = WebSocket(
				url
		)

		socket.onopen = {
			success("Socket opened! Setting up keep alive (${KEEP_ALIVE_DELAY}ms)")

			GlobalScope.launch {
				while (true) {
					val obj = object {}.asDynamic()
					obj["type"] = "ping"

					socket.send(JSON.stringify(obj))

					delay(KEEP_ALIVE_DELAY) // every 10s
				}
			}
		}

		socket.onclose = {
			warn("Socket closed!")
		}

		socket.onerror = {
			error("Socket error! Oh no... :(")
		}

		socket.onmessage = {
			val data = it.asDynamic().data
			// println("Message received: ${data}")

			val d = data.toString()
			if (d.startsWith("{")) {
				val raw = JSON.parse<Json>(d)

				val type = raw["type"].toString()

				val command = wsCommands.firstOrNull { it.name == type }

				if (command == null) {
					error("Received payload $raw (type: $type) but I don't know how to handle it!")
				} else {
					command.process(socket, raw)
				}
			}
		}

		document.onDOMReady {
			debug("DOM is ready!")

			debug(window.location.pathname + " - " + WebsiteUtils.getPathWithoutLocale())

			AdvertisementUtils.checkIfUserIsBlockingAds()

			launch {
				val deferred = listOf(
						async {
							loadLocale()
						},
						async {
							loadLoggedInUser()
						}
				)

				deferred.joinAll()

				debug("Locale test: ${locale["commands.images.drawnword.description"]}")
				debug("Locale test: ${locale["commands.fun.ship.bribeLove", ":3"]}")

				onPageChange(socket, window.location.pathname, null)
			}
		}
	}

	@UseExperimental(ImplicitReflectionSerializer::class)
	suspend fun loadLocale() {
		val payload = http.get<String>("${window.location.origin}/api/v1/loritta/locale/$localeId")
		locale = kotlinx.serialization.json.JSON.nonstrict.parse(payload)
	}

	@UseExperimental(ImplicitReflectionSerializer::class)
	suspend fun loadLoggedInUser() {
		val httpResponse = http.get<HttpResponse>("${window.location.origin}/api/v1/users/@me")
		val payload = httpResponse.readText()
		val jsonPayload = JSON.parse<Json>(payload)
		if (httpResponse.status != HttpStatusCode.OK/* jsonPayload["code"] != null */) {
			debug("Get User Request failed - ${jsonPayload["code"]}")
		} else {
			val userIdentification = kotlinx.serialization.json.JSON.nonstrict.parse<UserIdentification>(payload)
			debug("Get User Request success! - ${userIdentification.username} (${userIdentification.id})")
			SpicyMorenitta.INSTANCE.updateLoggedInUser(userIdentification)
		}
	}

	fun updateLoggedInUser(newUser: UserIdentification) {
		userIdentification = newUser
		debug("New user is $newUser")
		val loginButton = document.select<Element>("#login-button")
		val cloned = loginButton.cloneNode(true) // Nós precisamos remover os event listeners (onClick)
		cloned as Element

		loginButton.replaceWith(cloned)
		cloned.clear()
		cloned.setAttribute("href", "/br/dashboard")
		setUpPageSwitcher(cloned, "/br/dashboard")

		cloned.append {
			img(src = "https://cdn.discordapp.com/avatars/${newUser.id}/${newUser.avatar}.png?size=256") {
				style = """    font-size: 0px;
    line-height: 0px;
    width: 40px;
    height: 40px;
    position: absolute;
    top: 3px;
    border-radius: 100%;"""
			}

			div { // Dummy image que "ocupa" o espaço (já que a imagem usa position: absolute)
				style = """    font-size: 0px;
    line-height: 0px;
    width: 40px;
    visibility: hidden;
    height: 0px;
    display: inline-block;"""
			}

			span {
				style = "padding-left: 4px;"

				+ newUser.username
			}
		}
	}

	@UseExperimental(ImplicitReflectionSerializer::class)
	fun onPageChange(socket: WebSocket, path: String, content: Element?) {
		if (!navbarIsSetup) {
			addNavbarOptions(socket)
			navbarIsSetup = true
		}

		debug("Current path is $path")
		debug("Path without locale is ${WebsiteUtils.getPathWithoutLocale()}")

		val route = routes.firstOrNull { it.matches(WebsiteUtils.getPathWithoutLocale()) }
		if (route == null) {
			warn("No route for ${WebsiteUtils.getPathWithoutLocale()} found! Bug?")
			hideLoadingScreen()
			return
		}

		val params = route.getPathParameters(WebsiteUtils.getPathWithoutLocale())
		debug("Parameters: ${params.entries}")
		val call = ApplicationCall(params, content)

		if (!route.keepLoadingScreen) // Utilizado para coisas que querem mais http requests após carregar (página de fan arts!)
			hideLoadingScreen()

		route.onRender(call)
	}

	fun setUpPageSwitcher(element: Element, path: String) {
		element.setAttribute("data-preload-activated", "true")

		element.onClick {
			if (it.asDynamic().ctrlKey as Boolean || it.asDynamic().metaKey as Boolean || it.asDynamic().shiftKey as Boolean)
				return@onClick

			it.preventDefault()

			launch {
				// Switch page
				sendSwitchPageRequest(socket, path)
			}
		}

		var startedAt = 0.0

		element.onMouseEnter {
			if (ignoreCacheRequests)
				return@onMouseEnter

			it.stopPropagation()

			debug("Hovering the button!")
			startedAt = Date().getTime()

			launch {
				delay(CACHE_ON_HOVER_DELAY)
				val diff = Date().getTime() - startedAt

				if (diff >= CACHE_ON_HOVER_DELAY) {
					debug("Pre caching page (path: ${path})!")

					cachePageRequest(path)
				}
			}
		}

		element.onMouseLeave {
			it.stopPropagation()

			val diff = Date().getTime() - startedAt

			if (pageCache.containsKey(path)) {
				debug("Not hovering the button anymore! Hovered for $diff & Dropping page cache for $path")
				pageCache.remove(path)
			}
		}
	}

	fun addNavbarOptions(socket: WebSocket) {
		debug("Adding navbar options!")
		val navbar = document.select<Element>("#navigation-bar")

		val loginButton = document.select<Element>("#login-button")

		loginButton.onClick {
			if (isOldWebsite) {
				window.location.href = "${window.location.origin}/dashboard"
			} else {
				if (userIdentification == null) {
					val popup = window.open("${window.location.origin}/auth", "popup", "height=700,width=400")
				}
			}
		}

		val themeChangerButton = document.select<Element>("#theme-changer-button")

		themeChangerButton.onClick {
			val body = document.body!!

			if (body.hasClass("dark")) {
				WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteThemeUtils.WebsiteTheme.DEFAULT, false)
			} else {
				WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteThemeUtils.WebsiteTheme.DARK_THEME, false)
			}
		}

		val hamburgerButton = document.select<Element>("#hamburger-menu-button")

		hamburgerButton.onClick {
			debug("Clicked on the hamburger button!")
			if (navbar.hasClass("expanded")) {
				navbar.removeClass("expanded")
				document.body!!.style.overflowY = ""
			} else {
				navbar.addClass("expanded")
				document.body!!.style.overflowY = "hidden" // Para remover as scrollbars e apenas deixar as scrollbars da navbar
			}
		}

		setUpLinkPreloader()
		setUpLazyLoad()

		debug("Redirect buttons added!")
	}

	fun setUpLinkPreloader() {
		document.querySelectorAll("a[data-enable-link-preload=\"true\"]:not([data-preload-activated=\"true\"])").asList().forEach {
			debug("Setting up page switcher for $it")
			setUpPageSwitcher(it as Element, it.getAttribute("href")!!)
		}
	}

	fun setUpLazyLoad() {
		document.querySelectorAll("iframe[lazy-load-url]:not([lazy-load-activated=\"true\"])").asList().forEach {
			debug("Setting up iFrame lazy load for $it")

			val el = it as Element
			el.setAttribute("lazy-load-activated", "true")

			val callback = callback@{
				val diffBetweenElementAndCurrentYPosition = el.getBoundingClientRect().top - window.innerHeight

				if (0 >= diffBetweenElementAndCurrentYPosition) {
					if (!el.hasAttribute("lazy-load-url"))
						return@callback

					debug("iFrame is going to be displayed on screen! Loading...")
					val lazyLoadUrl = el.getAttribute("lazy-load-url").toString()
					el.removeAttribute("lazy-load-url")
					el.setAttribute("src", lazyLoadUrl)
				}
			}

			var applyScrollOn: Element? = el
			do {
				applyScrollOn = applyScrollOn?.parentElement
			} while (applyScrollOn != null && !applyScrollOn.hasAttribute("create-scroll-lazy-load-here"))

			if (applyScrollOn != null) {
				applyScrollOn.onScroll {
					callback.invoke()
				}
			} else {
				window.onScroll {
					callback.invoke()
				}
			}

			callback.invoke()
		}

		document.querySelectorAll("img[lazy-load-url]:not([lazy-load-activated=\"true\"]").asList().forEach {
			debug("Setting up image lazy load for $it")

			val el = it as Element
			el.setAttribute("lazy-load-activated", "true")

			val callback = callback@{
				val diffBetweenElementAndCurrentYPosition = el.getBoundingClientRect().top - window.innerHeight

				if (0 >= diffBetweenElementAndCurrentYPosition) {
					if (!el.hasAttribute("lazy-load-url"))
						return@callback

					debug("Image is going to be displayed on screen! Loading...")
					val lazyLoadUrl = el.getAttribute("lazy-load-url").toString()
					el.removeAttribute("lazy-load-url")
					el.setAttribute("src", lazyLoadUrl)
				}
			}

			var applyScrollOn2: Element? = el
			do {
				applyScrollOn2 = applyScrollOn2?.parentElement
			} while (applyScrollOn2 != null && !applyScrollOn2.hasAttribute("create-scroll-lazy-load-here"))

			if (applyScrollOn2 != null) {
				applyScrollOn2.onScroll {
					callback.invoke()
				}
			} else {
				window.onScroll {
					callback.invoke()
				}
			}

			callback.invoke()
		}
	}

	suspend fun sendSwitchPageRequest(socket: WebSocket, path: String) {
		if (pageLoadLock.isLocked) // Se está travado, vamos mostrar a tela de loading (normalmente é quando a página tá fazendo cache)
			showLoadingScreen()

		pageLoadLock.withLock {
			if (pageCache[path] != null) {
				val result = pageCache[path]!!
				pageCache.remove(path)

				ignoreCacheRequests = true
				debug("Path $path is already cached! Let's use it :3")

				switchPage(path, result)
				ignoreCacheRequests = false
				return
			}

			debug("Sending switch page request to $path")
			switchPageStart = Date().getTime()

			showLoadingScreen()

			val result = getPageContent(path)
			switchPage(path, result)
		}
	}

	suspend fun switchPage(path: String, content: String) {
		val temporary = document.createElement("html").apply {
			this.innerHTML = content
		}

		val temporaryBody = temporary.querySelector("#content")!!

		val title = temporary.querySelector("title")?.innerHTML
		debug("New title is $title")

		if (title != null)
			document.title = title

		// document.select<HTMLDivElement>("#content").remove()
		// document.body?.appendChild(temporaryBody)

		window.history.pushState(null, "", path)

		onPageChange(socket, path, temporaryBody)

		val end = Date().getTime()

		info("${end - switchPageStart}ms - Page: ${path}")
	}

	suspend fun cachePageRequest(path: String) {
		pageLoadLock.withLock {
			debug("Sending cache page request to $path")
			switchPageStart = Date().getTime()

			val result = getPageContent(path)

			pageCache[path] = result
		}
	}

	suspend fun getPageContent(path: String): String {
		val result = http.get<String> {
			url("${window.location.origin}$path")
			header("Preloading-Page", true)
		}
		return result
	}

	fun showLoadingScreen(text: String = "Carregando...") {
		document.select<HTMLDivElement>("#loading-screen").apply {
			select<HTMLDivElement>(".loading-text").apply {
				textContent = text
			}
			style.opacity = "1"
		}
	}

	fun hideLoadingScreen() {
		document.select<HTMLDivElement>("#loading-screen").apply {
			style.opacity = "0"
		}
	}

	fun launch(block: suspend CoroutineScope.() -> Unit) {
		val job = GlobalScope.launch(block = block)
		pageSpecificTasks.add(job)
	}
}
