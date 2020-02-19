package net.perfectdreams.loritta.website.routes.dashboard.configure

import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class ConfigureNashornCommandsRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/nashorn") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild) {
		loritta as Loritta
		val serverConfig = loritta.getServerConfigForGuild(guild.id)

		val variables = call.legacyVariables(locale)

		variables["saveType"] = "nashorn_commands"

		val feeds = JsonArray()
		serverConfig.nashornCommands.forEach {
			val json = Loritta.GSON.toJsonTree(it)
			feeds.add(json)
		}

		variables["commands"] = feeds.toString()

		call.respondHtml(evaluate("configure_nashorn.html", variables))
	}
}