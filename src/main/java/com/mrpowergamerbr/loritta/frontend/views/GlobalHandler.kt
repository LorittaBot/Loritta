package com.mrpowergamerbr.loritta.frontend.views

import com.github.salomonbrys.kotson.fromJson
import com.google.common.collect.Lists
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.frontend.views.subviews.*
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.*
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.APIGetApiKeyView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.APIGetServerConfigView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.APIUpdateServerConfigView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist.*
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.*
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.apache.commons.lang3.exception.ExceptionUtils
import org.jooby.Request
import org.jooby.Response
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.TimeUnit

object GlobalHandler {
	var views = mutableListOf<AbstractView>()
	var apiViews = mutableListOf<NoVarsView>()

	val logger = LoggerFactory.getLogger(AbstractCommand::class.java)

	fun render(req: Request, res: Response): String {
		logger.info(" ${req.header("X-Forwarded-For").value()}: ${req.path()}")

		if (req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage")) || req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)(save)"))) {
			val last = loritta.apiCooldown.getOrDefault(req.header("X-Forwarded-For").value(), 0L)

			val diff = System.currentTimeMillis() - last
			if (2500 >= diff) {
				return GSON.toJson(mapOf("api:code" to LoriWebCodes.RATE_LIMITED, "api:message" to "RATE_LIMITED"))
			}

			loritta.apiCooldown[req.header("X-Forwarded-For").value()] = System.currentTimeMillis()
		}

		apiViews.filter { it.handleRender(req, res, req.path()) }
				.forEach { return it.render(req, res, req.path()) }

		val variables = mutableMapOf<String, Any?>("discordAuth" to null)
		variables["epochMillis"] = System.currentTimeMillis()

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

		if (req.param("force_locale").isSet) {
			req.session()["forceLocale"] = req.param("force_locale").value()
		}

		if (req.param("logout").isSet) {
			req.session().destroy()
		}

		if (req.session().isSet("forceLocale")) {
			lorittaLocale  = LorittaLauncher.loritta.getLocaleById(req.session()["forceLocale"].value())
		}

		if (req.param("locale").isSet) {
			lorittaLocale = LorittaLauncher.loritta.getLocaleById(req.param("locale").value())
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

		variables["guildCount"] = loritta.guildCount
		variables["userCount"] = loritta.userCount
		variables["availableCommandsCount"] = loritta.commandManager.commandMap.size
		variables["commandMap"] = loritta.commandManager.commandMap
		variables["executedCommandsCount"] = LorittaUtilsKotlin.executedCommands
		variables["path"] = req.path()
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
				if (req.path() != "/dashboard" && req.path() != "/auth" && !req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)(save)")) && !req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage")) && !req.path().startsWith("/translation") /* DEPRECATED API */) {
					res.status(302) // temporary redirect / no page rank penalty (?)
					if (localeId == "default") {
						res.redirect("https://loritta.website/br${req.path()}")
					}
					if (localeId == "en-us") {
						res.redirect("https://loritta.website/us${req.path()}")
					}
				}
			}
		}

		variables["pathNL"] = pathNoLanguageCode // path no language code
		variables["loriUrl"] = LorittaWebsite.WEBSITE_URL + "${languageCode2 ?: "us"}/"

		val isPatreon = mutableMapOf<String, Boolean>()
		val isDonator = mutableMapOf<String, Boolean>()

		val lorittaGuild = com.mrpowergamerbr.loritta.utils.lorittaShards.getGuildById("297732013006389252")

		if (lorittaGuild != null) {
			val rolePatreons = lorittaGuild.getRoleById("364201981016801281") // Pagadores de Aluguel
			val roleDonators = lorittaGuild.getRoleById("334711262262853642") // Doadores

			val patreons = lorittaGuild.getMembersWithRoles(rolePatreons)
			val donators = lorittaGuild.getMembersWithRoles(roleDonators)

			patreons.forEach {
				isPatreon[it.user.id] = true
			}
			donators.forEach {
				isDonator[it.user.id] = true
			}
		}

		variables["clientId"] = Loritta.config.clientId
		variables["isPatreon"] = isPatreon
		variables["isDonator"] = isDonator
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
		variables["localeAsJson"] = GSON.toJson(lorittaLocale.strings)
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

		try {
			views.filter { it.handleRender(req, res, pathNoLanguageCode, variables) }
					.forEach { return it.render(req, res, pathNoLanguageCode, variables) }
		} catch (e: Exception) {
			logger.error("Erro ao processar conteúdo para ${req.header("X-Forwarded-For").value()}: ${req.path()}", e)
			throw e
		}

		res.status(404)
		return evaluate("404.html", variables)
	}

	fun generateViews()  {
		val views = mutableListOf<AbstractView>()
		val apiViews = mutableListOf<NoVarsView>()

		// ===[ APIS ]===
		apiViews.add(APIGetCommunityInfoView())
		apiViews.add(APIGetChannelInfoView())
		apiViews.add(APIGetRssFeedTitleView())
		apiViews.add(APIGetTwitchInfoView())
		apiViews.add(APILoriGetBalanceView())
		apiViews.add(APILoriGiveBalanceView())
		apiViews.add(APILoriSetBalanceView())
		apiViews.add(APILoriWithdrawBalanceView())
		apiViews.add(APIGetServerSampleView())
		apiViews.add(APIGetServerInformationView())
		apiViews.add(APIVoteServerView())
		apiViews.add(APIGetServerConfigView())
		apiViews.add(APIUpdateServerConfigView())
		apiViews.add(APIGetServersView())
		apiViews.add(APIGetLocaleView())
		apiViews.add(APILoriDailyRewardView())
		apiViews.add(APILoriDailyRewardStatusView())
		apiViews.add(APIJoinServerView())
		apiViews.add(APIGetApiKeyView())
		apiViews.add(APIGetServerVotesView())
		apiViews.add(APIGetCommandsView())

		views.add(HomeView())
		views.add(TranslationView())
		views.add(DashboardView())
		views.add(LorigotchiView())
		views.add(LoriPartnerView())
		views.add(ServersView())
		views.add(FanArtsView())
		views.add(DonateView())
		views.add(CommandsView())
		views.add(DailyView())
		views.add(SupportView())
		// views.add(NashornDocsView())
		views.add(PatreonCallbackView())
		views.add(AuthPathRedirectView())

		views.add(ConfigureServerView())
		views.add(ConfigureEventLogView())
		views.add(ConfigureInviteBlockerView())
		views.add(ConfigureAutoroleView())
		views.add(ConfigurePermissionsView())
		views.add(ConfigureWelcomerView())
		views.add(ConfigureStarboardView())
		views.add(ConfigureAminoView())
		views.add(ConfigureYouTubeView())
		views.add(ConfigureLivestreamView())
		views.add(ConfigureRSSFeedsView())
		views.add(ConfigureNashornCommandsView())
		views.add(ConfigureMusicView())
		views.add(ConfigureEventHandlersView())
		views.add(ConfigureCommandsView())
		views.add(ConfigureTextChannelsView())
		views.add(ConfigureModerationView())
		views.add(ConfigureLoriPartnerView())
		views.add(TestMessageView())

		this.views = views
		this.apiViews = apiViews
	}
}