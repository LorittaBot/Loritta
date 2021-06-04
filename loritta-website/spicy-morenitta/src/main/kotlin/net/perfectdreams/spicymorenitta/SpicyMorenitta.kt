package net.perfectdreams.spicymorenitta

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.hasClass
import kotlinx.dom.removeClass
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.hr
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import loadEmbeddedLocale
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.BaseRoute
import net.perfectdreams.spicymorenitta.routes.Birthday2020Route
import net.perfectdreams.spicymorenitta.routes.Birthday2020StatsRoute
import net.perfectdreams.spicymorenitta.routes.CommandsRoute
import net.perfectdreams.spicymorenitta.routes.DailyRoute
import net.perfectdreams.spicymorenitta.routes.DiscordBotBrasileiroRoute
import net.perfectdreams.spicymorenitta.routes.DonateRoute
import net.perfectdreams.spicymorenitta.routes.FanArtsRoute
import net.perfectdreams.spicymorenitta.routes.HomeRoute
import net.perfectdreams.spicymorenitta.routes.ReputationRoute
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.AuditLogRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.AutoroleConfigRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.BadgeRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.CustomCommandsRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.DailyMultiplierRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.FortniteConfigRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.GeneralConfigRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.LevelUpRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.MemberCounterRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.MiscellaneousConfigRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.ModerationConfigRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.PremiumKeyRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.TwitchRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.TwitterRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.WelcomerConfigRoute
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.YouTubeRoute
import net.perfectdreams.spicymorenitta.routes.user.dashboard.AllBackgroundsListDashboardRoute
import net.perfectdreams.spicymorenitta.routes.user.dashboard.AvailableBundlesDashboardRoute
import net.perfectdreams.spicymorenitta.routes.user.dashboard.BackgroundsListDashboardRoute
import net.perfectdreams.spicymorenitta.routes.user.dashboard.DailyShopDashboardRoute
import net.perfectdreams.spicymorenitta.routes.user.dashboard.ProfileDesignsListDashboardRoute
import net.perfectdreams.spicymorenitta.routes.user.dashboard.ShipEffectsDashboardRoute
import net.perfectdreams.spicymorenitta.utils.AdvertisementUtils
import net.perfectdreams.spicymorenitta.utils.AuthUtils
import net.perfectdreams.spicymorenitta.utils.ErrorTracker
import net.perfectdreams.spicymorenitta.utils.GoogleAdSense
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.Moment
import net.perfectdreams.spicymorenitta.utils.NitroPay
import net.perfectdreams.spicymorenitta.utils.TingleModal
import net.perfectdreams.spicymorenitta.utils.TingleOptions
import net.perfectdreams.spicymorenitta.utils.WebsiteUtils
import net.perfectdreams.spicymorenitta.utils.gtag
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.onDOMReady
import net.perfectdreams.spicymorenitta.utils.onMouseEnter
import net.perfectdreams.spicymorenitta.utils.onMouseLeave
import net.perfectdreams.spicymorenitta.utils.onScroll
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.utils.selectAll
import net.perfectdreams.spicymorenitta.utils.trackOverflowChanges
import net.perfectdreams.spicymorenitta.utils.visibleModal
import org.w3c.dom.Element
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import kotlin.collections.set
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
		const val CACHE_ON_HOVER_DELAY = 75L // milliseconds
		lateinit var INSTANCE: SpicyMorenitta
	}

	val pageLoadLock = Mutex()
	val routes = mutableListOf(
		HomeRoute(),
		DiscordBotBrasileiroRoute(),
		FanArtsRoute(this),
		UpdateNavbarSizePostRender("/support", false, false),
		UpdateNavbarSizePostRender("/blog", false, false),
		UpdateNavbarSizePostRender("/guidelines", false, false),
		AuditLogRoute(this),
		LevelUpRoute(this),
		TwitterRoute(this),
		CommandsRoute(this),
		GeneralConfigRoute(this),
		BadgeRoute(this),
		DailyMultiplierRoute(this),
		LevelUpRoute(this),
		PremiumKeyRoute(this),
		TwitterRoute(this),
		YouTubeRoute(this),
		TwitchRoute(this),
		DonateRoute(this),
		FortniteConfigRoute(this),
		ShipEffectsDashboardRoute(this),
		AvailableBundlesDashboardRoute(this),
		DailyRoute(this),
		BackgroundsListDashboardRoute(this),
		AllBackgroundsListDashboardRoute(this),
		ProfileDesignsListDashboardRoute(this),
		DailyShopDashboardRoute(this),
		Birthday2020Route(this),
		Birthday2020StatsRoute(this),
		ReputationRoute(),
		MiscellaneousConfigRoute(this),
		AutoroleConfigRoute(this),
		MemberCounterRoute(this),
		ModerationConfigRoute(this),
		WelcomerConfigRoute(this),
		CustomCommandsRoute(this)
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

	val hasLocaleIdInPath: Boolean
		get() {
			val localeIdFromPath = WebsiteUtils.getWebsiteLocaleIdViaPath()
			return localeIdFromPath in validWebsiteLocaleIds
		}

	var currentRoute: BaseRoute? = null

	var userIdentification: UserIdentification? = null
	val pageSpecificTasks = mutableListOf<Job>()
	var currentPath: String? = null

	val DEFAULT_COROUTINE_EXCEPTION_HANDLER = CoroutineExceptionHandler { _, exception ->
		error("Coroutine error! $exception")
		val dynamicException = exception.asDynamic()

		console.log("Message: ${dynamicException.message}")
		console.log("File Name: ${dynamicException.fileName}")
		console.log("Line Number: ${dynamicException.lineNumber}")
		console.log("Column Number: ${dynamicException.columnNumber}")
		console.log("Stack: ${dynamicException.stack}")

		ErrorTracker.processException(
			this,
			dynamicException.message as String,
			dynamicException.fileName as String,
			dynamicException.lineNumber as Int,
			dynamicException.columnNumber as Int,
			dynamicException
		)
		throw exception
	}

	fun start() {
		INSTANCE = this

		ErrorTracker.start(this)

		info("HELLO FROM KOTLIN 1.4.10!")
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
			debug("Loading deprecated locale from the body...")
			loadEmbeddedLocale()

			debug(window.location.pathname + " - " + WebsiteUtils.getPathWithoutLocale())

			debug("Setting spicyMorenittaLoaded variable to true on the window object")
			window.asDynamic().spicyMorenittaLoaded = true

			launch {
				val currentRoute = getPageRouteForCurrentPath()

				val currentPathName = currentPath?.let { it::class.simpleName } ?: "Unknown"
				debug("Route for the current path: $currentPathName")
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
					deferred[0].await()

					debug("Locale test: ${locale["commands.command.drawnword.description"]}")
					debug("Locale test: ${locale["commands.command.ship.bribeLove", ":3"]}")
				}
				if (currentRoute.requiresUserIdentification)
					deferred[1].await()

				onPageChange(window.location.pathname, null)

				GoogleAdSense.renderAds()
				NitroPay.renderAds()

				AdvertisementUtils.checkIfUserIsBlockingAds()

				debug("Done! Current route is $currentPathName")
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

	suspend fun loadLocale() {
		val payload = http.get<String>("${window.location.origin}/api/v1/loritta/locale/$localeId")
		locale = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(BaseLocale.serializer(), payload)

		// Atualizar o locale que o moment utiliza, já que ele usa uma instância global para tuuuuudo
		val momentLocaleId = when (locale.id) {
			"default", "pt-pt" -> "pt-br"
			"es-es" -> "es"
			else -> "en"
		}
		Moment.locale(momentLocaleId)
	}

	/**
	 * Requests the logged in user via "/api/v1/users/@me" and calls [updateLoggedInUser] if the request succeeds.
	 */
	suspend fun loadLoggedInUser() {
		val httpResponse = http.get<HttpResponse>("${window.location.origin}/api/v1/users/@me")
		val payload = httpResponse.readText()
		val jsonPayload = JSON.parse<Json>(payload)
		if (httpResponse.status != HttpStatusCode.OK/* jsonPayload["code"] != null */) {
			debug("Get User Request failed - ${jsonPayload["code"]}")
		} else {
			val userIdentification = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(UserIdentification.serializer(), payload)
			debug("Get User Request success! - ${userIdentification.username} (${userIdentification.id})")
			SpicyMorenitta.INSTANCE.updateLoggedInUser(userIdentification)
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
		userIdentification = newUser
		debug("New user is $newUser")
		val loginButton = document.select<Element?>("#login-button")
		if (loginButton != null) {
			val cloned = loginButton.cloneNode(true) // Nós precisamos remover os event listeners (onClick)
			cloned as Element

			loginButton.replaceWith(cloned)
			cloned.clear()
			cloned.setAttribute("href", "/$websiteLocaleId/dashboard")

			cloned.append {
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
		if (navbarIsSetup)
			checkAndFixNavbarOverflownEntries()
	}

	fun getPageRouteForCurrentPath(): BaseRoute {
		var route = routes.firstOrNull { it.matches(WebsiteUtils.getPathWithoutLocale()) }
		if (route == null) {
			warn("No route for ${WebsiteUtils.getPathWithoutLocale()} found! Bug? Defaulting to UpdateNavbarSizerPostRender!")
			route = UpdateNavbarSizePostRender(WebsiteUtils.getPathWithoutLocale())
		}
		return route
	}

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

		val deleteAccountButton = document.select<HTMLElement?>("#delete-account-button")
		deleteAccountButton?.let {
			if (!it.hasAttribute("data-delete-account-button-enabled")) {
				it.setAttribute("data-delete-account-button-enabled", "true")

				deleteAccountButton.onClick {
					val modal = TingleModal(
						TingleOptions(
							footer = true,
							cssClass = arrayOf("tingle-modal--overflow")
						)
					)

					var counterJob: Job? = null

					var counterCountingDown = 300

					modal.addFooterBtn("<i class=\"fas fa-redo\"></i> ${locale["website.dashboard.profile.deleteAccount.deleteMyAccount"]} (${counterCountingDown}s)", "button-discord button-discord-attention pure-button button-discord-modal delete-account-button disabled") {
						if (counterCountingDown == 0) {
							showLoadingScreen()
							launch {
								http.post<String>("${window.location.origin}/api/v1/users/@me/delete") {}
								window.location.href = "/"
							}
						}
					}

					modal.addFooterBtn("<i class=\"fas fa-times\"></i> ${locale["modules.levelUp.resetXp.cancel"]}", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
						modal.close()
						counterJob?.cancel()
					}

					modal.setContent(
						createHTML().div {
							div(classes = "category-name") {
								+ locale["modules.levelUp.resetXp.areYouSure"]
							}

							div {
								style = "text-align: center;"

								img(src = "https://loritta.website/assets/img/fanarts/l6.png") {
									width = "250"
								}

								locale.getList("website.dashboard.profile.deleteAccount.description").forEach {
									p {
										+ it
									}
								}

								p {
									style = "font-size: 2.0em; color: red;"

									+ locale["website.dashboard.profile.deleteAccount.yourAccountWillBeSuspendedWarning"]
								}

								locale.getList("website.dashboard.profile.deleteAccount.warningDescription").forEach {
									p {
										style = "font-size: 1.25em; color: red;"
										+ it
									}
								}
							}
						}
					)
					modal.open()

					modal.trackOverflowChanges(this)

					counterJob = launch {
						while (this.isActive && counterCountingDown > 0) {
							val visibleModalDeleteAccountButton = visibleModal.querySelector(".delete-account-button") ?: break
							counterCountingDown--
							visibleModalDeleteAccountButton.innerHTML = "<i class=\"fas fa-redo\"></i> ${locale["website.dashboard.profile.deleteAccount.deleteMyAccount"]} (${counterCountingDown}s)"

							if (counterCountingDown == 0) {
								visibleModalDeleteAccountButton.removeClass("disabled")
								visibleModalDeleteAccountButton.innerHTML = "<i class=\"fas fa-redo\"></i> ${locale["website.dashboard.profile.deleteAccount.deleteMyAccount"]}"
							}
							delay(1_000)
						}
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

		if (hasLocaleIdInPath) {
			val route = getPageRouteForCurrentPath()

			debug("Route for current path is ${route::class.simpleName}")

			val params = route.getPathParameters(pathWithoutLocale)
			debug("Parameters: ${params.entries}")
			val call = ApplicationCall(params, content)

			if (!route.keepLoadingScreen) // Utilizado para coisas que querem mais http requests após carregar (página de fan arts!)
				hideLoadingScreen()

			debug("Unloading current route...")
			this.currentRoute?.onUnload()
			this.currentRoute = route

			debug("Rendering ${route::class.simpleName}")
			route.onRender(call)
		} else {
			warn("Path doesn't have locale! We are not going to switch to JS routes...")
		}
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
		val navbar = document.select<Element?>("#navigation-bar")

		if (navbar != null) {
			debug("Navbar is $navbar")

			val loginButton = document.select<Element?>("#login-button")

			debug("Setting up login button events...")

			loginButton?.onClick {
				if (true) {
					window.location.href = "${window.location.origin}/dashboard"
				} else {
					if (userIdentification == null) {
						val popup = window.open("${window.location.origin}/auth", "popup", "height=700,width=400")
					}
				}
			}

			debug("Setting up theme changer button events...")

			val themeChangerButton = document.select<Element?>("#theme-changer-button")

			themeChangerButton?.onClick {
				val body = document.body!!

				if (body.hasClass("dark")) {
					WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteThemeUtils.WebsiteTheme.DEFAULT, false)
				} else {
					WebsiteThemeUtils.changeWebsiteThemeTo(WebsiteThemeUtils.WebsiteTheme.DARK_THEME, false)
				}
			}

			debug("Setting up hamburger button events...")

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

			if (hamburgerButton != null) {
				debug("Setting up resize handler...")

				window.addEventListener("resize", {
					checkAndFixNavbarOverflownEntries()
				}, true)

				checkAndFixNavbarOverflownEntries()

				debug("Resize handler successfully created!")
			}
		} else {
			warn("Navigation Bar does not exist! Ignorning...")
		}

		setUpLinkPreloader()
		setUpLazyLoad()

		debug("Redirect buttons added!")
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
			debug("Navbar entries are overflowing, let's unhide the hamburger button!")

			hamburgerButton.style.display = "block"
		} else {
			debug("Navbar entries are not overflowing, let's hide the hamburger button!")

			hamburgerButton.style.display = "none"
		}
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

		document.querySelectorAll("img[lazy-load-url]:not([lazy-load-activated=\"true\"])").asList().forEach {
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

		try {
			val config = object {
				val page_path: String = path
			}

			gtag("config", "UA-53518408-9", config)
		} catch (e: Error) {
			warn("Google Analytics not found or wasn't loaded!")
		}
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
		document.select<HTMLDivElement?>("#loading-screen")?.apply {
			select<HTMLDivElement>(".loading-text").apply {
				textContent = text
			}
			style.opacity = "1"
		}
	}

	fun hideLoadingScreen() {
		document.select<HTMLDivElement?>("#loading-screen")?.apply {
			style.opacity = "0"
		}
	}

	fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		val job = GlobalScope.launch(DEFAULT_COROUTINE_EXCEPTION_HANDLER, block = block)
		pageSpecificTasks.add(job)
		return job
	}

	fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
		val job = GlobalScope.async(DEFAULT_COROUTINE_EXCEPTION_HANDLER, block = block)
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
