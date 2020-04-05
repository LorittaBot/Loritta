package net.perfectdreams.spicymorenitta

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.*
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.*
import net.perfectdreams.spicymorenitta.routes.user.dashboard.*
import net.perfectdreams.spicymorenitta.trunfo.TrunfoGame
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.locale.BaseLocale
import oldMain
import org.w3c.dom.*
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

val http = HttpClient(Js) {
	expectSuccess = false // Não dar erro ao receber status codes 400-500
}

lateinit var locale: BaseLocale

class SpicyMorenitta : Logging {
	companion object {
		const val CACHE_ON_HOVER_DELAY = 125L // milliseconds
		lateinit var INSTANCE: SpicyMorenitta
	}

	val pageLoadLock = Mutex()
	val routes = mutableListOf(
			HomeRoute(),
			DiscordBotBrasileiroRoute(),
			FanArtsRoute(this),
			UpdateNavbarSizePostRender("/support", false, false),
			UpdateNavbarSizePostRender("/blog", false, false),
			UpdateNavbarSizePostRender("/extended", false, false),
			UpdateNavbarSizePostRender("/guidelines", false, false),
			AuditLogRoute(this),
			LevelUpRoute(this),
			TwitterRoute(this),
			RssFeedsRoute(this),
			CommandsRoute(this),
			TranslateRoute(this),
			GeneralConfigRoute(this),
			BadgeRoute(this),
			DailyMultiplierRoute(this),
			LevelUpRoute(this),
			PremiumKeyRoute(this),
			RssFeedsRoute(this),
			TwitterRoute(this),
			YouTubeRoute(this),
			TwitchRoute(this),
			MusicConfigRoute(this),
			DonateRoute(this),
			FortniteConfigRoute(this),
			ProfileListDashboardRoute(this),
			ShipEffectsDashboardRoute(this),
			AvailableBundlesDashboardRoute(this),
			DailyRoute(this),
			BackgroundsListDashboardRoute(this),
			AllBackgroundsListDashboardRoute(this),
			DailyShopDashboardRoute(this),
			Birthday2020Route(this),
			Birthday2020StatsRoute(this)
	)

	val validWebsiteLocaleIds = mutableListOf(
			"br",
			"us",
			"es",
			"pt-furry",
			"en-furry",
			"br-debug",
			"en-debug"
	)
	val websiteLocaleIdToLocaleId = mutableMapOf(
			"br" to "default",
			"us" to "en-us",
			"es" to "es-es",
			"pt-furry" to "pt-furry",
			"en-furry" to "en-furry",
			"br-debug" to "pt-debug",
			"en-debug" to "en-debug"
	)

	val localeId: String
		get() {
			return websiteLocaleIdToLocaleId[websiteLocaleId] ?: "default"
		}

	val websiteLocaleId: String
		get() {
			val localeIdFromPath = WebsiteUtils.getWebsiteLocaleIdViaPath()
			debug("Locale ID from Path is $localeIdFromPath")
			return if (localeIdFromPath in validWebsiteLocaleIds)
				localeIdFromPath
			else
				"us"
		}

	var currentRoute: BaseRoute? = null

	var userIdentification: UserIdentification? = null
	val pageSpecificTasks = mutableListOf<Job>()
	var currentPath: String? = null

	@UseExperimental(ImplicitReflectionSerializer::class)
	fun start() {
		INSTANCE = this

		ErrorTracker.start(this)

		// Workaround for KotlinJS's DCE
		DoNotRemoveDeadCodeWorkaround.methodRefs

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

		if (false) {
			TrunfoGame.start()
			return
		}

		oldMain(arrayOf())

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

		document.onDOMReady {
			debug("DOM is ready!")

			debug(window.location.pathname + " - " + WebsiteUtils.getPathWithoutLocale())

			debug("Setting spicyMorenittaLoaded variable to true on the window object")
			window.asDynamic().spicyMorenittaLoaded = true

			launch {
				val currentRoute = getPageRouteForCurrentPath()

				debug("Route for the current path: $currentRoute")
				debug("Does the route need locale data? ${currentRoute.requiresLocales}")
				debug("Does the route need user identification data? ${currentRoute.requiresUserIdentification}")
				val deferred = listOf(
						async {
							loadLocale()
						},
						async {
							loadLoggedInUser()
						}
				)

				if (currentRoute.requiresLocales) {
					deferred[0].join()

					debug("Locale test: ${locale["commands.images.drawnword.description"]}")
					debug("Locale test: ${locale["commands.fun.ship.bribeLove", ":3"]}")
				}
				if (currentRoute.requiresUserIdentification)
					deferred[1].join()

				GoogleAdSense.renderAds()

				AdvertisementUtils.checkIfUserIsBlockingAds()

				onPageChange(window.location.pathname, null)
			}
		}

		currentPath = window.location.pathname

		window.onpopstate = {
			if (currentPath == window.location.pathname) {
				debug("History changed but seems to be a hash change (maybe?), ignoring onpopstate event...")
			} else {
				debug("History changed! Trying to load the new page... New pathname is ${window.location.pathname}")
				currentPath = window.location.pathname
				launch {
					sendSwitchPageRequest(window.location.pathname)
				}
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

		cloned.append {
			val userId = newUser.id.toLong()

			val avatarUrl = if (newUser.avatar != null) {
				val extension = if (newUser.avatar.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
					"gif"
				} else { "png" }

				"https://cdn.discordapp.com/avatars/${userId}/${newUser.avatar}.${extension}?size=256"
			} else {
				val avatarId = userId % 5

				"https://cdn.discordapp.com/embed/avatars/$avatarId.png?size=256"
			}

			img(src = avatarUrl) {
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

	fun getPageRouteForCurrentPath(): BaseRoute {
		var route = routes.firstOrNull { it.matches(WebsiteUtils.getPathWithoutLocale()) }
		if (route == null) {
			warn("No route for ${WebsiteUtils.getPathWithoutLocale()} found! Bug? Defaulting to UpdateNavbarSizerPostRender!")
			route = UpdateNavbarSizePostRender(WebsiteUtils.getPathWithoutLocale())
		}
		return route
	}

	@UseExperimental(ImplicitReflectionSerializer::class)
	fun onPageChange(path: String, content: Element?) {
		if (!navbarIsSetup) {
			addNavbarOptions()
			navbarIsSetup = true
		}

		val pathWithoutLocale = WebsiteUtils.getPathWithoutLocale()
		debug("Current path is $path")
		debug("Path without locale is ${pathWithoutLocale}")

		val logoutButton = document.select<HTMLElement?>("#logout-button")
		logoutButton?.let {
			if (!it.hasAttribute("data-login-button-enabled")) {
				it.setAttribute("data-login-button-enabled", "true")

				logoutButton.onClick {
					showLoadingScreen()
					launch {
						http.post<String>("${window.location.origin}/api/v1/users/@me/logout") {}
						window.location.href = "/"
					}
				}
			}
		}

		document.select<Element?>("#languages")?.let {
			it.clear()
			it.append {
				div {
					a(href = "/br$pathWithoutLocale") {
						+ "Português"
					}
				}
				hr {}
				div {
					a(href = "/us$pathWithoutLocale") {
						+ "English"
					}
				}
			}
		}

		val route = getPageRouteForCurrentPath()

		val params = route.getPathParameters(pathWithoutLocale)
		debug("Parameters: ${params.entries}")
		val call = ApplicationCall(params, content)

		if (!route.keepLoadingScreen) // Utilizado para coisas que querem mais http requests após carregar (página de fan arts!)
			hideLoadingScreen()

		this.currentRoute?.onUnload()
		this.currentRoute = route

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
				sendSwitchPageRequest(path)
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

	fun addNavbarOptions() {
		debug("Adding navbar options!")
		val navbar = document.select<Element>("#navigation-bar")

		val loginButton = document.select<Element?>("#login-button")

		loginButton?.onClick {
			if (true) {
				window.location.href = "${window.location.origin}/dashboard"
			} else {
				if (userIdentification == null) {
					val popup = window.open("${window.location.origin}/auth", "popup", "height=700,width=400")
				}
			}
		}

		val themeChangerButton = document.select<Element?>("#theme-changer-button")

		themeChangerButton?.onClick {
			val body = document.body!!

			if (body.hasClass("dark")) {
				WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteThemeUtils.WebsiteTheme.DEFAULT, false)
			} else {
				WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteThemeUtils.WebsiteTheme.DARK_THEME, false)
			}
		}

		val hamburgerButton = document.select<Element?>("#hamburger-menu-button")

		hamburgerButton?.onClick {
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
			if (it is Element) {
				debug("Setting up page switcher for $it")

				var pageUrl = it.getAttribute("href")!!

				if (pageUrl.startsWith("http")) {
					if (!pageUrl.startsWith(window.location.origin)) // Mesmo que seja no mesmo domínio, existe as políticas de CORS
						return@forEach

					val location = document.createElement("a") as HTMLAnchorElement
					location.href = pageUrl
					pageUrl = location.pathname
				}

				setUpPageSwitcher(it, pageUrl)
			}
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

	suspend fun sendSwitchPageRequest(path: String) {
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

		val temporaryBody = temporary.select<HTMLDivElement?>("#content")

		val title = temporary.select<HTMLElement?>("title")?.innerHTML
		debug("New title is $title")

		if (title != null)
			document.title = title

		// document.select<HTMLDivElement>("#content").remove()
		// document.body?.appendChild(temporaryBody)
		currentPath = path
		window.history.pushState(null, "", path)

		onPageChange(path, temporaryBody)

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

	fun showLoadingScreen(text: String = "${locale["loritta.loading"]}...") {
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

	fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		val job = GlobalScope.launch(block = block)
		pageSpecificTasks.add(job)
		return job
	}

	fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
		val job = GlobalScope.async(block = block)
		pageSpecificTasks.add(job)
		return job
	}

	/**
	 * Fixes the left sidebar after the content is switched
	 */
	fun fixLeftSidebarScroll(call: () -> (Unit)) {
		val leftSidebar = document.select<HTMLDivElement>("#left-sidebar")
		val oldScroll = leftSidebar.scrollTop
		call.invoke()
		val newLeftSidebar = document.select<HTMLDivElement>("#left-sidebar")
		newLeftSidebar.scrollTop = oldScroll
	}
}
