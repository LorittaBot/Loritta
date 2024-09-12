package net.perfectdreams.spicymorenitta

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import decodeURIComponent
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.hasClass
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import loadEmbeddedLocale
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.Language
import net.perfectdreams.i18nhelper.formatters.IntlMessageFormat
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.PocketLorittaSettings
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.loritta.serializable.messageeditor.LorittaDiscordMessageEditorSetupConfig
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.loritta.serializable.requests.LorittaRPCRequest
import net.perfectdreams.loritta.serializable.responses.LorittaRPCResponse
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.components.HtmlText
import net.perfectdreams.spicymorenitta.components.SimpleSelectMenu
import net.perfectdreams.spicymorenitta.components.SimpleSelectMenuEntry
import net.perfectdreams.spicymorenitta.components.messages.DiscordMessageEditor
import net.perfectdreams.spicymorenitta.components.messages.TargetChannelResult
import net.perfectdreams.spicymorenitta.game.GameState
import net.perfectdreams.spicymorenitta.modals.ModalManager
import net.perfectdreams.spicymorenitta.routes.*
import net.perfectdreams.spicymorenitta.routes.guilds.dashboard.*
import net.perfectdreams.spicymorenitta.routes.user.dashboard.AllBackgroundsListDashboardRoute
import net.perfectdreams.spicymorenitta.routes.user.dashboard.BackgroundsListDashboardRoute
import net.perfectdreams.spicymorenitta.routes.user.dashboard.ProfileDesignsListDashboardRoute
import net.perfectdreams.spicymorenitta.toasts.ToastManager
import net.perfectdreams.spicymorenitta.utils.*
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.parsing.DOMParser
import org.w3c.xhr.XMLHttpRequest
import kotlin.collections.set
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.Date
import kotlin.js.Json

var switchPageStart = 0.0
val pageCache = mutableMapOf<String, String>()
var ignoreCacheRequests = false
var navbarIsSetup = false

val http = HttpClient(Js) {
	expectSuccess = false // Não dar erro ao receber status codes 400-500
}
// Only used within the SpicyMorenitta instance
private val _http = http

lateinit var locale: BaseLocale
lateinit var i18nContext: I18nContext

class SpicyMorenitta : Logging {
	companion object {
		const val CACHE_ON_HOVER_DELAY = 75L // milliseconds
		lateinit var INSTANCE: SpicyMorenitta
	}

	val http = _http
	val modalManager = ModalManager(this)
	val toastManager = ToastManager(this)
	val soundEffects = SoundEffects(this)
	val pageLoadLock = Mutex()
	// well don't call this if the game state isn't initialized
	val gameState = GameState()
	val routes = mutableListOf(
		HomeRoute(),
		DiscordBotBrasileiroRoute(),
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
		DailyRoute(this),
		BackgroundsListDashboardRoute(this),
		AllBackgroundsListDashboardRoute(this),
		ProfileDesignsListDashboardRoute(this),
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
	val websiteLocaleIdToLanguageId = mutableMapOf(
		"br" to "pt",
		"us" to "en"
	)

	val localeId: String
		get() {
			return websiteLocaleIdToLocaleId[websiteLocaleId] ?: "default"
		}

	val languageId: String
		get() {
			return websiteLocaleIdToLanguageId[websiteLocaleId] ?: "en"
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

	@OptIn(ExperimentalEncodingApi::class)
	fun start() {
		INSTANCE = this

		info("HELLO FROM KOTLIN ${KotlinVersion.CURRENT.toString()}! :3")
		info("SpicyMorenitta :3")
		info("Howdy, my name is Loritta!")
		info("I want to make the world a better place... making people happier and helping other people... changing their lives...")
		info("I hope I succeed...")
		// Chromium easter egg
		console.log("%c       ", "font-size: 64px; background: url(https://stuff.loritta.website/loritta-zz-heathecliff.png) no-repeat; background-size: 64px 64px;")

		info("Initializing _hyperscript...")
		browserInit()

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

		document.onDOMContentLoaded {
			debug("DOM is ready!")
			debug("Loading deprecated locale from the body...")
			loadEmbeddedLocale()

			debug(window.location.pathname + " - " + WebsiteUtils.getPathWithoutLocale())

			debug("Setting spicyMorenittaLoaded variable to true on the window object")
			window.asDynamic().spicyMorenittaLoaded = true

			// Register Jetpack Compose things
			val modalElement = document.select<HTMLDivElement?>("#modal-list")
			if (modalElement != null) {
				modalManager.setupModalRendering(modalElement)
			} else {
				warn("Missing #modal-list element!")
			}

			val toastElement = document.select<HTMLDivElement?>("#toast-list")
			if (toastElement != null) {
				toastManager.setupToastRendering(toastElement)
			} else {
				warn("Missing #toast-list element!")
			}

			document.addEventListener("htmx:load", { elt ->
				println("htmx:load")
				val targetElement = elt.asDynamic().target as HTMLElement

				processCustomComponents(targetElement)

				// Render NitroPay ads
				NitroPay.renderAds()
			})

			document.addEventListener("htmx:afterRequest", { evt ->
				println("htmx:afterRequest")
				val xmlHttpRequest = evt.asDynamic().detail.xhr as XMLHttpRequest
				// While we can send the modal within a HX-Trigger header, there's an issue with it: nginx limits the header size!
				// It is possible to increase it on nginx's side by increasing the proxy_buffer_size, proxy_buffers and proxy_busy_buffers_size
				// However we've decided to just let the JSON modal be embedded on the response itself
				val openEmbeddedModal = xmlHttpRequest.getResponseHeader("SpicyMorenitta-Open-Embedded-Spicy-Modal")?.toBoolean()

				if (openEmbeddedModal == true) {
					println("Received embedded spicy modal")
					val responseAsText = xmlHttpRequest.responseText
					val embeddedSpicyModal = kotlinx.serialization.json.Json.decodeFromString<EmbeddedSpicyModal>(responseAsText)
					modalManager.openModal(embeddedSpicyModal)
				}

				// The lack of dash is intentional, htmx checks if the header CONTAINS HX-Trigger not if it is equal to
				val useResponseAsHxTrigger = xmlHttpRequest.getResponseHeader("SpicyMorenitta-Use-Response-As-HXTrigger")?.toBoolean()

				if (useResponseAsHxTrigger == true) {
					println("Received use response as HX-Trigger")
					val responseAsText = xmlHttpRequest.responseText
					val json = kotlinx.serialization.json.Json.parseToJsonElement(responseAsText).jsonObject
					json.entries.forEach {
						val eventName = it.key
						val eventValue = it.value.jsonPrimitive.contentOrNull
						println("Triggering event $eventName with $eventValue")
						htmx.trigger(
							"body",
							it.key,
							jsObject {
								this.value = eventValue
							}
						)
					}
				}
			})

			document.addEventListener("htmx:beforeSwap", { evt ->
				println("htmx:beforeSwap")
				val xmlHttpRequest = evt.asDynamic().detail.xhr as XMLHttpRequest

				// The lack of dash is intentional, htmx checks if the header CONTAINS HX-Trigger not if it is equal to
				val useResponseAsHxTrigger = xmlHttpRequest.getResponseHeader("SpicyMorenitta-Use-Response-As-HXTrigger")?.toBoolean()

				if (useResponseAsHxTrigger == true) {
					// Don't attempt to swap if it is a SpicyMorenitta-Use-Response-As-HXTrigger response
					evt.asDynamic().detail.shouldSwap = false
				}
			})

			document.addEventListener("htmx:afterSettle", { evt ->
				println("htmx:afterSettle")

				val xmlHttpRequest = evt.asDynamic().detail.xhr as XMLHttpRequest
				println(xmlHttpRequest.getResponseHeader("Content-Type"))
				if (xmlHttpRequest.getResponseHeader("Content-Type")?.startsWith("text/html") == true) {
					// Sync attributes on the target elements
					val parser = DOMParser()
					// Parse the responseText into an HTML Document (because responseXML does not work)
					val responseDocument = parser.parseFromString(xmlHttpRequest.responseText, "text/html")

					responseDocument
						.querySelectorAll("[spicy-oob-attribute-swap]")
						.asList()
						.filterIsInstance<HTMLElement>()
						.forEach {
							val element = document.getElementById(it.id)
							if (element == null) {
								println("Could not find a element in the document with ID ${it.id} to do a oob attribute swap...")
								return@forEach
							}

							val attrsToBeSynced = it.getAttribute("spicy-oob-attribute-swap")!!.split(",")!!

							println("Attributes to be synced: $attrsToBeSynced")

							attrsToBeSynced.forEach { attributeName ->
								val newValue = it.getAttribute(attributeName)
								println("OOB attribute swap for $attributeName is $newValue")
								if (newValue != null)
									element.setAttribute(attributeName, newValue)
								else
									element.removeAttribute(attributeName)
							}

							// And then reprocess the element to make htmx actually understand the new things
							// YES IT NEEDS TO BE INSIDE A SET TIMEOUT
							window.setTimeout({
								htmx.process(element)
							})
						}
				}
			})

			document.addEventListener("showSpicyModal", { evt ->
				val eventValue = evt.asDynamic().detail.value as String

				// We use decodeURIComponent because headers cannot have non-ASCII characters
				// We also use decodeURIComponent instead of Base64 because technically headers in HTTP/2.0 are compressed, and URI components compress better than Base64
				val embeddedSpicyModal = kotlinx.serialization.json.Json.decodeFromString<EmbeddedSpicyModal>(decodeURIComponent(eventValue))
				modalManager.openModal(embeddedSpicyModal)
			})

			document.addEventListener("closeSpicyModal", { evt ->
				modalManager.closeModal()
			})

			document.addEventListener("showSpicyToast", { evt ->
				val eventValue = evt.asDynamic().detail.value as String

				// We use decodeURIComponent because headers cannot have non-ASCII characters
				// We also use decodeURIComponent instead of Base64 because technically headers in HTTP/2.0 are compressed, and URI components compress better than Base64
				val embeddedSpicyToast = kotlinx.serialization.json.Json.decodeFromString<EmbeddedSpicyToast>(decodeURIComponent(eventValue))
				toastManager.showToast(embeddedSpicyToast)
			})

			document.addEventListener("playSoundEffect", { evt ->
				playSoundEffect(evt.asDynamic().detail.value)
			})

			document.addEventListener("pocketLorittaSettingsSync", { evt ->
				val eventValue = evt.asDynamic().detail.value
				val pocketLorittaSettings = kotlinx.serialization.json.Json.decodeFromString<PocketLorittaSettings>(eventValue)
				gameState.syncStateWithSettings(pocketLorittaSettings)
			})

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
					debug("i18nContext test: ${i18nContext.get(I18nKeysData.Commands.Command.Drawnmask.Description)}")
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
		val payload = http.get("${window.location.origin}/api/v1/loritta/locale/$localeId")
			.bodyAsText()
		locale = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(BaseLocale.serializer(), payload)

		val i18nPayload = http.get("${window.location.origin}/api/v1/languages/$languageId")
			.bodyAsText()

		val language = kotlinx.serialization.json.Json.decodeFromString<Language>(i18nPayload)

		val loadedI18nContext = I18nContext(
			IntlMFFormatter(language.info.formattingLanguageId),
			language
		)
		i18nContext = loadedI18nContext

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
		val httpResponse = http.get("${window.location.origin}/api/v1/users/@me")
		val payload = httpResponse.bodyAsText()
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
			debug("Login button is not null! Updating it...")
			val cloned = loginButton.cloneNode(true) // Nós precisamos remover os event listeners (onClick)
			cloned as Element

			debug("Replacing login button with cloned element...")
			loginButton.replaceWith(cloned)
			cloned.clear()
			cloned.setAttribute("href", "/$websiteLocaleId/dashboard")

			debug("Appending the user's effective avatar URL")
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
			debug("Successfully updated the login button!")
		} else {
			debug("Login Button is not present, so we aren't going to update it...")
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
						http.post("${window.location.origin}/api/v1/users/@me/logout") {}
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
						jsObject<TingleOptions> {
							footer = true
							cssClass = arrayOf("tingle-modal--overflow")
							closeMethods = arrayOf()
						}
					)

					var counterJob: Job? = null

					var counterCountingDown = 300

					modal.addFooterBtn("<i class=\"fas fa-redo\"></i> ${locale["website.dashboard.profile.deleteAccount.deleteMyAccount"]} (${counterCountingDown}s)", "button-discord button-discord-attention pure-button button-discord-modal delete-account-button disabled") {
						if (counterCountingDown == 0) {
							showLoadingScreen()
							launch {
								http.post("${window.location.origin}/api/v1/users/@me/delete") {}
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

								img(src = "https://stuff.loritta.website/loritta-stop-heathecliff.png") {
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
				window.location.href = "${window.location.origin}/dashboard"
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
		val result = http.get {
			url("${window.location.origin}$path")
			header("Preloading-Page", true)
		}.bodyAsText()
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

	/**
	 * Sends a SpicyMorenitta RPC request
	 */
	suspend inline fun <reified T> sendRPCRequest(request: LorittaRPCRequest): T {
		val httpResponse = http.post("${window.location.origin}/api/v1/loritta/rpc") {
			setBody(
				kotlinx.serialization.json.Json.encodeToString<LorittaRPCRequest>(request)
			)
		}

		val rpcResponse = kotlinx.serialization.json.Json.decodeFromString<LorittaRPCResponse>(httpResponse.bodyAsText())

		if (rpcResponse !is T)
			kotlin.error("RPC response does not match expected type! Received type $rpcResponse")

		return rpcResponse
	}

	fun playSoundEffect(eventValue: String, onEnd: () -> (Unit) = {}) {
		when (eventValue) {
			"config-saved" -> {
				soundEffects.configSaved.play(0.4, onEnd = onEnd) // for some reason this sfx is LOUD
			}
			"config-error" -> {
				soundEffects.configError.play(0.4, onEnd = onEnd) // for some reason this sfx is LOUD
			}
			"cash" -> {
				val cash = Audio("${loriUrl}assets/snd/css1_cash.wav")
				cash.play()
			}
			"recycle-bin" -> {
				soundEffects.recycleBin.play(1.0, onEnd = onEnd)
			}
			"xarola-ratinho" -> {
				soundEffects.xarolaRatinho.play(0.1, onEnd = onEnd)
			}
			else -> {
				warn("Unknown sound effect \"$eventValue\"")
			}
		}
	}

	fun processCustomComponents(targetElement: HTMLElement) {
		processCustomComponent(targetElement)

		// Also process all childs
		targetElement.children.asList()
			.forEach {
				if (it is HTMLElement)
					processCustomComponents(it)
			}
	}

	fun processCustomComponent(it: HTMLElement) {
		// TODO: Refactor this!
		run {
			if (it.getAttribute("loritta-item-shop-timer") != null) {
				if (it.getAttribute("loritta-powered-up") != null)
					return@run

				it.setAttribute("loritta-powered-up", "")
				val i18nHours = it.getAttribute("loritta-item-shop-i18n-hours")!!
				val i18nMinutes = it.getAttribute("loritta-item-shop-i18n-minutes")!!
				val i18nSeconds = it.getAttribute("loritta-item-shop-i18n-seconds")!!
				val messageFormatHours = IntlMessageFormat(i18nHours, "pt")
				val messageFormatMinutes = IntlMessageFormat(i18nMinutes, "pt")
				val messageFormatSeconds = IntlMessageFormat(i18nSeconds, "pt")

				val scope = CoroutineScope(Job())
				val observer = MutationObserver { _, observer ->
					if (!document.contains(it)) {
						debug("Cancelling element's coroutine scope because it was removed from the DOM...")
						console.log(it)
						scope.cancel() // Cancel coroutine scope when element is removed
						observer.disconnect() // Disconnect the observer to avoid leaks
					}
				}
				observer.observe(
					document.body ?: throw IllegalStateException("Document has no body"),
					MutationObserverInit(childList = true, subtree = true)
				)

				val resetsAt = it.getAttribute("loritta-item-shop-resets-at")!!.toLong()

				// TODO - htmx-adventures: Don't use GlobalScope!
				//  (technically we are scoping this to the element nowadays...)
				var previousText = ""
				scope.launch {
					while (isActive) {
						val diff = resetsAt - (Date().getTime().toLong())
						if (0 >= diff) {
							// Trigger Item Shop refresh if the time is 0
							htmx.trigger("body", "refreshItemShop", null)
							return@launch
						}

						val timeInSeconds = diff / 1_000

						val s = timeInSeconds % 60
						val m = (timeInSeconds / 60) % 60
						val h = (timeInSeconds / (60 * 60)) % 24

						val newText = buildString {
							if (h != 0L) {
								append(
									messageFormatHours.format(
										jsObject {
											this.unit = h + 1
										}
									)
								)
							} else if (m != 0L) {
								append(
									messageFormatMinutes.format(
										jsObject {
											this.unit = m
										}
									)
								)
							} else if (s != 0L) {
								append(
									messageFormatSeconds.format(
										jsObject {
											this.unit = s
										}
									)
								)
							}
						}
						if (newText != previousText) {
							debug("Updating timer text to $newText")
							it.innerText = newText
							previousText = newText
						}

						delay(1_000)
					}
				}
			}
		}

		run {
			// Technically only one loritta-game-canvas instance should exist
			if (it.id == "loritta-game-canvas" && it is HTMLCanvasElement) {
				if (it.getAttribute("loritta-powered-up") != null)
					return@run

				it.setAttribute("loritta-powered-up", "")

				val pocketLorittaSettings =
					kotlinx.serialization.json.Json.decodeFromString<PocketLorittaSettings>(it.getAttribute("pocket-loritta-settings")!!)
				gameState.setCanvas(it)
				gameState.updateCanvasSize()
				gameState.syncStateWithSettings(pocketLorittaSettings)

				gameState.addedToTheDOM = true

				window.addEventListener(
					"resize",
					{
						gameState.updateCanvasSize()
					}
				)

				gameState.start()
			}
		}

		run {
			if (it is HTMLFormElement && it.getAttribute("loritta-synchronize-with-save-bar") != null) {
				if (it.getAttribute("loritta-powered-up") != null)
					return@run

				it.setAttribute("loritta-powered-up", "")
				val lorittaSaveBarAttribute = it.getAttribute("loritta-synchronize-with-save-bar")!!

				val saveBarElement = htmx.find(lorittaSaveBarAttribute) as HTMLDivElement
				val initialState = JSON.stringify(htmx.values(it))

				println("Form save bar setup, initial state is: $initialState")

				it.addEventListener(
					"input",
					{ event ->
						// If the input does not have a name attribute, let's ignore the input...
						if ((event.target as Element?)?.getAttribute("name") == null)
							return@addEventListener

						// Save the current state
						val newState = JSON.stringify(htmx.values(it))

						println("INITIAL STATE: $initialState")
						println("NEW STATE: $newState")

						if (newState != initialState) {
							// This is a HACK to avoid the save bar showing up when it is inserted into the DOM
							// We can't keep this in a hyperscript when the animation ends because that breaks if we are deferring the script
							saveBarElement.removeClass("initial-state")

							saveBarElement.addClass("has-changes")
							saveBarElement.removeClass("no-changes")
						} else {
							// This is a HACK to avoid the save bar showing up when it is inserted into the DOM
							// We can't keep this in a hyperscript when the animation ends because that breaks if we are deferring the script
							saveBarElement.removeClass("initial-state")

							saveBarElement.addClass("no-changes")
							saveBarElement.removeClass("has-changes")
						}
					}
				)
			}
		}

		run {
			if (it.getAttribute("loritta-save-bar") != null) {
				if (it.getAttribute("loritta-powered-up") != null)
					return@run

				it.setAttribute("loritta-powered-up", "")

				debug("Loritta Save Bar")
				val observer = MutationObserver { _, observer ->
					debug("DOM mutation")
					if (!document.contains(it)) {
						debug("Cancelling element's save bar scope because it was removed from the DOM...")
						document.select<HTMLDivElement?>(".toast-list")?.removeClass("save-bar-active")
						observer.disconnect() // Disconnect the observer to avoid leaks
					} else {
						if (it.classList.contains("has-changes")) {
							debug("I have changes!")
							document.select<HTMLDivElement?>(".toast-list")?.addClass("save-bar-active")
						} else if (it.classList.contains("no-changes")) {
							debug("I don't have changes...")
							document.select<HTMLDivElement?>(".toast-list")?.removeClass("save-bar-active")
						}
					}
				}
				observer.observe(
					document.body ?: throw IllegalStateException("Document has no body"),
					MutationObserverInit(childList = true, subtree = true, attributes = true)
				)
			}
		}

		run {
			if (it is HTMLSelectElement && it.getAttribute("loritta-select-menu") != null) {
				val originalSelectMenuElement = it
				if (originalSelectMenuElement.getAttribute("loritta-powered-up") != null)
					return@run

				originalSelectMenuElement.setAttribute("loritta-powered-up", "")
				val originalStyle = originalSelectMenuElement.getAttribute("style")

				// Hide the original select menu
				originalSelectMenuElement.style.display = "none"

				// val selectMenuName = originalSelectMenuElement.getAttribute("name")
				// originalSelectMenuElement.removeAttribute("name")

				val htmlOptions = originalSelectMenuElement.selectAll<HTMLOptionElement>("option")

				val originalEntries = htmlOptions.map {
					val openEmbeddedModalOnSelect = it.getAttribute("loritta-select-menu-open-embedded-modal-on-select")
					val embeddedSpicyModal = openEmbeddedModalOnSelect?.let { kotlinx.serialization.json.Json.decodeFromString<EmbeddedSpicyModal>(decodeURIComponent(it)) }

					SimpleSelectMenuEntry(
						{
							val textHTML = it.getAttribute("loritta-select-menu-text")
							if (textHTML != null) {
								HtmlText(textHTML)
							} else {
								Text(it.innerHTML)
							}
						},
						it.value,
						it.selected,
						// We don't use HTML "disabled" attribute because that doesn't let the entry be serialized to a form
						it.getAttribute("loritta-select-menu-disabled")?.toBoolean() == true,
						embeddedSpicyModal
					)
				}

				val selectMenuWrapperElement = document.createElement("div")
				if (originalStyle != null)
					selectMenuWrapperElement.setAttribute("style", originalStyle)

				originalSelectMenuElement.parentElement!!.insertBefore(selectMenuWrapperElement, originalSelectMenuElement)

				renderComposable(selectMenuWrapperElement) {
					var modifiedEntries by remember { mutableStateOf(originalEntries) }

					// For some reason we need to key it by the modifiedEntries list
					// Because if a entry uses custom HTML it borks and the list is never updated
					SimpleSelectMenu(
						"Click Here!",
						if (originalSelectMenuElement.multiple) null else 1,
						modifiedEntries
					) {
						debug("owo!!!")
						debug("Selected $it")
						debug("Original: ${modifiedEntries.toList()}")
						val selectedEntries = originalEntries.filter { entry ->
							entry.value in it
						}

						for (selectedEntry in selectedEntries) {
							if (selectedEntry.disabled) {
								// Selecting a disabled entry!
								if (selectedEntry.embeddedSpicyModalToBeOpenedIfDisabled != null) {
									// The entry has a embedded modal!
									modalManager.openModal(selectedEntry.embeddedSpicyModalToBeOpenedIfDisabled)
								}
								return@SimpleSelectMenu
							}
						}

						modifiedEntries = originalEntries.map { copiedEntry ->
							copiedEntry.copy(selected = copiedEntry in selectedEntries)
						}
						debug("Modified: ${modifiedEntries.toList()}")

						// We are actually going to edit the original elements
						// originalSelectMenuElement.value = it.first()
						htmlOptions.forEach { htmlOption ->
							htmlOption.selected = htmlOption.value in it
						}

						originalSelectMenuElement.dispatchEvent(Event("input", EventInit(bubbles = true)))

						originalSelectMenuElement.dispatchEvent(Event("change", EventInit(bubbles = true)))
					}
				}
			}
		}

		run {
			if (it is HTMLTextAreaElement && it.getAttribute("loritta-discord-message-editor") != null) {
				val originalSelectMenuElement = it
				if (originalSelectMenuElement.getAttribute("loritta-powered-up") != null)
					return@run

				originalSelectMenuElement.setAttribute("loritta-powered-up", "")
				val setupJson = kotlinx.serialization.json.Json.decodeFromString<LorittaDiscordMessageEditorSetupConfig>(originalSelectMenuElement.getAttribute("loritta-discord-message-editor-config")!!)

				// Hide the original text area
				originalSelectMenuElement.style.display = "none"

				val selectMenuWrapperElement = document.createElement("div")

				originalSelectMenuElement.parentElement!!.insertBefore(selectMenuWrapperElement, originalSelectMenuElement)

				var rawMessage by mutableStateOf(originalSelectMenuElement.value)
				var targetChannelId by mutableStateOf<String?>(null)

				when (val query = setupJson.testMessageTargetChannelQuery) {
					is TestMessageTargetChannelQuery.QuerySelector -> {
						val targetQuery = document.select<HTMLElement>(query.querySelector)

						fun updateSelect() {
							val value = targetQuery.asDynamic().value
							targetChannelId = value as String?
						}

						targetQuery.addEventListener(
							"input",
							{
								updateSelect()
							}
						)

						updateSelect()
					}

					TestMessageTargetChannelQuery.SendDirectMessage -> targetChannelId = "dm" // wow this is a hack
				}

				renderComposable(selectMenuWrapperElement) {
					val _targetChannelId = targetChannelId

					DiscordMessageEditor(
						this@SpicyMorenitta,
						setupJson.templates,
						setupJson.placeholderSectionType,
						setupJson.placeholders,
						setupJson.guild,
						setupJson.testMessageEndpointUrl,
						if (_targetChannelId != null) {
							if (_targetChannelId == "dm") {
								TargetChannelResult.DirectMessageTarget
							} else {
								TargetChannelResult.GuildMessageChannelTarget(_targetChannelId.toLong())
							}
						} else {
							TargetChannelResult.ChannelNotSelected
						},
						setupJson.selfLorittaUser,
						listOf(),
						listOf(),
						rawMessage
					) {
						// Update our variable
						rawMessage = it

						// And update the backing textarea
						originalSelectMenuElement.value = rawMessage

						// And dispatch an input event for anyone that's listening to it
						originalSelectMenuElement.dispatchEvent(Event("input", EventInit(bubbles = true, cancelable = true)))
					}
				}
			}
		}
	}
}
