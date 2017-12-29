package com.mrpowergamerbr.loritta.frontend.views

import com.github.salomonbrys.kotson.fromJson
import com.google.common.collect.Lists
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import com.mrpowergamerbr.loritta.frontend.views.subviews.AuthPathRedirectView
import com.mrpowergamerbr.loritta.frontend.views.subviews.DashboardView
import com.mrpowergamerbr.loritta.frontend.views.subviews.DonateView
import com.mrpowergamerbr.loritta.frontend.views.subviews.FanArtsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.HomeView
import com.mrpowergamerbr.loritta.frontend.views.subviews.NashornDocsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.PatreonCallbackView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ServersFanClubView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ServersView
import com.mrpowergamerbr.loritta.frontend.views.subviews.TranslationView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.APIGetChannelInfoView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.APIGetCommunityInfoView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.APIGetRssFeedTitleView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.APIGetTwitchInfoView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.CommandsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureAminoView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureAutoroleView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureCommandsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureEventHandlersView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureEventLogView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureInviteBlockerView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureLivestreamView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureMusicView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureNashornCommandsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigurePermissionsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureRSSFeedsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureServerView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureStarboardView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureTextChannelsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureWelcomerView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.ConfigureYouTubeView
import com.mrpowergamerbr.loritta.frontend.views.subviews.configure.TestMessageView
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Request
import org.jooby.Response
import java.lang.management.ManagementFactory
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.TimeUnit

object GlobalHandler {
	val views = generateViews()

	fun render(req: Request, res: Response): String {
		println("${req.ip()} ~ ${req.header("X-Forwarded-For").value()}: ${req.path()}")
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

		variables["guilds"] = loritta.cachedGuilds
		variables["userCount"] = loritta.cachedUsers
		variables["availableCommandsCount"] = loritta.commandManager.commandMap.size
		variables["commandMap"] = loritta.commandManager.commandMap
		variables["executedCommandsCount"] = LorittaUtilsKotlin.executedCommands

		variables["serversFanClub"] = loritta.serversFanClub
		variables["clientId"] = Loritta.config.clientId
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

		variables["uptimeDays"] = days
		variables["uptimeHours"] = hours
		variables["uptimeMinutes"] = minutes
		variables["uptimeSeconds"] = seconds
		variables["currentUrl"] = LorittaWebsite.WEBSITE_URL + req.path().substring(1)

		variables["famousGuilds"] = loritta.famousGuilds
		variables["randomFamousGuilds"] = loritta.randomFamousGuilds

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

		views.filter { it.handleRender(req, res, variables) }
			.forEach { return it.render(req, res, variables) }

		res.status(404)
		return evaluate("404.html", variables)
	}

	fun generateViews(): MutableList<AbstractView> {
		val views = mutableListOf<AbstractView>()
		// ===[ APIS ]===
		views.add(APIGetCommunityInfoView())
		views.add(APIGetChannelInfoView())
		views.add(APIGetRssFeedTitleView())
		views.add(APIGetTwitchInfoView())

		views.add(HomeView())
		views.add(TranslationView())
		views.add(DashboardView())
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
		views.add(TestMessageView())
		views.add(FanArtsView())
		views.add(DonateView())
		views.add(CommandsView())
		views.add(ServersView())
		views.add(ServersFanClubView())
		views.add(NashornDocsView())
		views.add(PatreonCallbackView())
		views.add(AuthPathRedirectView())

		return views
	}
}