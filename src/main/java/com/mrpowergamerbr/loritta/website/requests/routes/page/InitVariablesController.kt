package com.mrpowergamerbr.loritta.website.requests.routes.page

import org.jooby.Request
import org.jooby.Response
import org.jooby.Route
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("**")
open class InitVariablesController {
	@GET
	fun initVariables(req: Request, res: Response, chain: Route.Chain) {
		println("Initializing variables")
		req.set("something", mapOf("foo" to "bar"))
		/* if (true) {
			val variables = mutableMapOf<String, Any?>("test" to "hello", "abc" to "def")
			req.set("variables", variables)
			chain.next(req, res)
			return
		}
		val queryString = req.urlQueryString

		val variables = mutableMapOf(
				"discordAuth" to null,
				"epochMillis" to System.currentTimeMillis(),
				"guildCount" to loritta.guildCount,
				"userCount" to loritta.userCount,
				"availableCommandsCount" to loritta.commandManager.commandMap.size,
				"commandMap" to loritta.commandManager.commandMap,
				"executedCommandsCount" to LorittaUtilsKotlin.executedCommands,
				"path" to req.path(),
				"clientId" to Loritta.config.clientId
		)

		req.set("variables", variables)

		// TODO: Deprecated
		val acceptLanguage = req.header("Accept-Language").value("en-US")

		// Vamos parsear!
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))

		val defaultLocale = LorittaLauncher.loritta.getLocaleById("default")
		var lorittaLocale = LorittaLauncher.loritta.getLocaleById("default")

		var localeId: String? = null

		for (range in ranges) {
			localeId = range.range.toLowerCase()
			var bypassCheck = false
			if (localeId == "pt-br" || localeId == "pt") {
				localeId = "default"
				bypassCheck = true
			}
			if (localeId == "en") {
				localeId = "en-us"
			}
			val parsedLocale = LorittaLauncher.loritta.getLocaleById(localeId)
			if (bypassCheck || defaultLocale !== parsedLocale) {
				lorittaLocale = parsedLocale
			}
		}

		if (req.param("logout").isSet) {
			req.session().destroy()
		}

		// Para deixar tudo organizadinho (o Google não gosta de locales que usem query strings ou cookies), nós iremos usar subdomínios!
		val languageCode = req.path().split("/").getOrNull(1)

		if (languageCode != null) {
			lorittaLocale = when (languageCode) {
				"br" -> LorittaLauncher.loritta.getLocaleById("default")
				"pt" -> LorittaLauncher.loritta.getLocaleById("pt-pt")
				"us" -> LorittaLauncher.loritta.getLocaleById("en-us")
				"es" -> LorittaLauncher.loritta.getLocaleById("es-es")
				else -> lorittaLocale
			}
		}

		for (locale in lorittaLocale.strings) {
			variables[locale.key] = MessageFormat.format(locale.value)
		}

		var pathNoLanguageCode = req.path()
		val split = pathNoLanguageCode.split("/").toMutableList()
		val languageCode2 = split.getOrNull(1)

		val hasLangCode = languageCode2 == "br" || languageCode2 == "es" || languageCode2 == "us" || languageCode2 == "pt"
		if (hasLangCode) {
			split.removeAt(0)
			split.removeAt(0)
			pathNoLanguageCode = "/" + split.joinToString("/")
		} else {
			// Nós iremos redirecionar o usuário para a versão correta para ele, caso esteja acessando o "website errado"
			if (localeId != null) {
				if ((req.path() != "/dashboard" && !req.param("discordAuth").isSet) && req.path() != "/auth" && !req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)(save)")) && !req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage")) && !req.path().startsWith("/translation") /* DEPRECATED API */) {
					res.status(302) // temporary redirect / no page rank penalty (?)
					if (localeId == "default") {
						res.redirect("https://loritta.website/br${req.path()}${queryString}")
					}
					if (localeId == "pt-pt") {
						res.redirect("https://loritta.website/pt${req.path()}${queryString}")
					}
					if (localeId == "es-es") {
						res.redirect("https://loritta.website/es${req.path()}${queryString}")
					}
					res.redirect("https://loritta.website/us${req.path()}${queryString}")
					return
				}
			}
		}

		variables["pathNL"] = pathNoLanguageCode // path no language code
		variables["loriUrl"] = LorittaWebsite.WEBSITE_URL + "${languageCode2 ?: "us"}/"

		variables["isPatreon"] = loritta.isPatreon
		variables["isDonator"] = loritta.isDonator

		var jvmUpTime = ManagementFactory.getRuntimeMXBean().uptime

		val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
		jvmUpTime -= TimeUnit.DAYS.toMillis(days)
		val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
		jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
		jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
		val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

		val correctUrl = LorittaWebsite.WEBSITE_URL.replace("https://", "https://$languageCode.")
		variables["uptimeDays"] = days
		variables["uptimeHours"] = hours
		variables["uptimeMinutes"] = minutes
		variables["uptimeSeconds"] = seconds
		variables["currentUrl"] = correctUrl + req.path().substring(1)
		variables["localeAsJson"] = Loritta.GSON.toJson(lorittaLocale.strings)
		variables["websiteUrl"] = LorittaWebsite.WEBSITE_URL

		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				val userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
				variables["discordAuth"] = discordAuth
				variables["userIdentification"] = userIdentification
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		chain.next(req, res) */
	}
}