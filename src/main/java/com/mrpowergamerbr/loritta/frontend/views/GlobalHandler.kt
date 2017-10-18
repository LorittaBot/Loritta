package com.mrpowergamerbr.loritta.frontend.views

import com.github.salomonbrys.kotson.fromJson
import com.google.common.collect.Lists
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.frontend.views.subviews.AbstractView
import com.mrpowergamerbr.loritta.frontend.views.subviews.AuthPathRedirectView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureAminoView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureAutoroleView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureEventLogView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureInviteBlockerView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureMusicView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureNashornCommandsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigurePermissionsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureRSSFeedsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureServerView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureStarboardView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureWelcomerView
import com.mrpowergamerbr.loritta.frontend.views.subviews.ConfigureYouTubeView
import com.mrpowergamerbr.loritta.frontend.views.subviews.DashboardView
import com.mrpowergamerbr.loritta.frontend.views.subviews.HomeView
import com.mrpowergamerbr.loritta.frontend.views.subviews.PatreonCallbackView
import com.mrpowergamerbr.loritta.frontend.views.subviews.TranslationView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.APIGetChannelInfoView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.APIGetCommunityIconView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.APIGetRssFeedTitleView
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Request
import org.jooby.Response
import java.lang.management.ManagementFactory
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.TimeUnit

object GlobalHandler {
	fun render(req: Request, res: Response): String {
		val views = getViews()

		val variables = mutableMapOf<String, Any?>("discordAuth" to null)

		variables["epochMillis"] = System.currentTimeMillis()

		val acceptLanguage = req.header("Accept-Language").value("en-US")

		// Vamos parsear!
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))

		val defaultLocale = LorittaLauncher.loritta.getLocaleById("default")
		var lorittaLocale = LorittaLauncher.loritta.getLocaleById("default")

		for (range in ranges) {
			var localeId = range.getRange().toLowerCase()
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

		if (req.param("locale").isSet) {
			lorittaLocale = LorittaLauncher.loritta.getLocaleById(req.param("locale").value())
		}

		for (locale in lorittaLocale.strings) {
			variables[locale.key] = MessageFormat.format(locale.value)
		}

		val guilds = lorittaShards.getGuilds()
		variables["guilds"] = guilds
		variables["userCount"] = lorittaShards.getUsers().size
		variables["availableCommandsCount"] = loritta.commandManager.commandMap.size
		variables["executedCommandsCount"] = LorittaUtilsKotlin.executedCommands
		variables["serversFanClub"] = loritta.serversFanClub.sortedByDescending { it.guild.members.size }
		variables["clientId"] = Loritta.config.clientId

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

		val famousGuilds = guilds.sortedByDescending { it.members.size - it.members.filter { it.user.isBot }.count() }.subList(0, 36)

		variables["famousGuilds"] = famousGuilds
		Collections.shuffle(famousGuilds)
		variables["randomFamousGuilds"] = famousGuilds

		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.gson.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				variables["discordAuth"] = discordAuth
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		views.filter { it.handleRender(req, res, variables) }
			.forEach { return it.render(req, res, variables) }

		res.status(404)
		return "404"
	}

	private fun getViews(): List<AbstractView> {
		val views = mutableListOf<AbstractView>()
		// ===[ APIS ]===
		views.add(APIGetCommunityIconView())
		views.add(APIGetChannelInfoView())
		views.add(APIGetRssFeedTitleView())

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
		views.add(ConfigureRSSFeedsView())
		views.add(ConfigureNashornCommandsView())
		views.add(ConfigureMusicView())
		views.add(PatreonCallbackView())
		views.add(AuthPathRedirectView())
		return views
	}
}