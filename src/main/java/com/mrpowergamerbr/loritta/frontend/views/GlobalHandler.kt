package com.mrpowergamerbr.loritta.frontend.views

import com.github.salomonbrys.kotson.fromJson
import com.google.common.collect.Lists
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.frontend.views.subviews.*
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.*
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.APIGetServerConfigView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.APIUpdateServerConfigView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist.APIGetServerInformationView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist.APIGetServerSampleView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist.APIGetServersView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.serverlist.APIVoteServerView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.*
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.debug.DebugType
import com.mrpowergamerbr.loritta.utils.debug.debug
import com.mrpowergamerbr.loritta.utils.log
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.apache.commons.lang3.exception.ExceptionUtils
import org.jooby.Request
import org.jooby.Response
import java.lang.management.ManagementFactory
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.TimeUnit

object GlobalHandler {
	var views = mutableListOf<AbstractView>()
	var apiViews = mutableListOf<NoVarsView>()

	fun render(req: Request, res: Response): String {
		// println("${req.ip()} ~ ${req.header("X-Forwarded-For").value()}: ${req.path()}")
		// log("[WEBSITE] ${req.header("X-Forwarded-For").value()}: ${req.path()}")
		debug(DebugType.WEBSITE, "${req.header("X-Forwarded-For").value()}: ${req.path()}")


		if (req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage")) || req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)(save)"))) {
			val last = loritta.apiCooldown.getOrDefault(req.header("X-Forwarded-For").value(), 0L)

			val diff = System.currentTimeMillis() - last
			if (2500 >= diff) {
				return GSON.toJson(mapOf("api:code" to LoriWebCodes.RATE_LIMITED, "api:message" to "RATE_LIMITED"))
			}

			loritta.apiCooldown[req.header("X-Forwarded-For").value()] = System.currentTimeMillis()
		}

		apiViews.filter { it.handleRender(req, res) }
				.forEach { return it.render(req, res) }

		val variables = mutableMapOf<String, Any?>("discordAuth" to null)
		variables["epochMillis"] = System.currentTimeMillis()

		val acceptLanguage = req.header("Accept-Language").value("en-US")

		// Vamos parsear!
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))

		val defaultLocale = LorittaLauncher.loritta.getLocaleById("default")
		var lorittaLocale = LorittaLauncher.loritta.getLocaleById("default")

		for (range in ranges) {
			var localeId = range.range.toLowerCase()
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
			req.session().unset("discordAuth")
		}

		if (req.session().isSet("forceLocale")) {
			lorittaLocale  = LorittaLauncher.loritta.getLocaleById(req.session()["forceLocale"].value())
		}

		if (req.param("locale").isSet) {
			lorittaLocale = LorittaLauncher.loritta.getLocaleById(req.param("locale").value())
		}

		for (locale in lorittaLocale.strings) {
			variables[locale.key] = MessageFormat.format(locale.value)
		}

		variables["guildCount"] = loritta.guildCount
		variables["userCount"] = loritta.userCount
		variables["availableCommandsCount"] = loritta.commandManager.commandMap.size
		variables["commandMap"] = loritta.commandManager.commandMap
		variables["executedCommandsCount"] = LorittaUtilsKotlin.executedCommands

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

		variables["serversFanClub"] = loritta.serversFanClub
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

		variables["uptimeDays"] = days
		variables["uptimeHours"] = hours
		variables["uptimeMinutes"] = minutes
		variables["uptimeSeconds"] = seconds
		variables["currentUrl"] = LorittaWebsite.WEBSITE_URL + req.path().substring(1)
		variables["localeAsJson"] = GSON.toJson(lorittaLocale.strings)

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
			views.filter { it.handleRender(req, res, variables) }
					.forEach { return it.render(req, res, variables) }
		} catch (e: Exception) {
			val stacktraceAsString = ExceptionUtils.getStackTrace(e)
			debug(DebugType.STACKTRACES, stacktraceAsString)
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

		views.add(HomeView())
		views.add(TranslationView())
		views.add(DashboardView())
		views.add(LorigotchiView())
		views.add(LoriPartnerView())
		views.add(ServersView())
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
		views.add(FanArtsView())
		views.add(DonateView())
		views.add(CommandsView())
		// views.add(ServersView())
		views.add(ServersFanClubView())
		views.add(NashornDocsView())
		views.add(PatreonCallbackView())
		views.add(AuthPathRedirectView())

		this.views = views
		this.apiViews = apiViews
	}
}